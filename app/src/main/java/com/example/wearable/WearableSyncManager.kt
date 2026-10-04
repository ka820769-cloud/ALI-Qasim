package com.example.wearable

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import com.example.data.model.DeviceType
import com.example.data.model.HealthBiometrics
import com.example.data.model.HeartRateZone
import com.example.data.model.WearableDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

enum class SimulationEffortMode(val label: String, val baseBpm: Int, val description: String) {
    REST("Resting", 72, "Sedentary / Relaxed biometrics"),
    MODERATE("Aerobic", 132, "Jogging / cycling effort"),
    PEAK("Sprint / Peak", 168, "High-intensity max output"),
    RECOVERY("Cool Down", 98, "Post-workout active recovery")
}

class WearableSyncManager(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    companion object {
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    // Available devices
    private val defaultDevices = listOf(
        WearableDevice(
            id = "virtual_pulse_watch",
            name = "PulseWatch Ultra",
            type = DeviceType.SMARTWATCH,
            batteryLevel = 88,
            isConnected = true,
            isVirtual = true,
            rssiDbm = -42,
            macAddress = "E4:5F:01:A2:3B:90",
            firmwareVersion = "v3.6.2"
        ),
        WearableDevice(
            id = "virtual_apex_band",
            name = "Apex Health Band Pro",
            type = DeviceType.FITNESS_BAND,
            batteryLevel = 64,
            isConnected = false,
            isVirtual = true,
            rssiDbm = -65,
            macAddress = "D1:9A:88:44:BC:12",
            firmwareVersion = "v2.1.0"
        ),
        WearableDevice(
            id = "virtual_polar_h10",
            name = "Polar H10 ECG Strap",
            type = DeviceType.CHEST_STRAP,
            batteryLevel = 95,
            isConnected = false,
            isVirtual = true,
            rssiDbm = -52,
            macAddress = "A0:87:CD:55:01:FE",
            firmwareVersion = "v4.0.1"
        ),
        WearableDevice(
            id = "virtual_smart_ring",
            name = "BioRing Horizon Gen3",
            type = DeviceType.SMART_RING,
            batteryLevel = 72,
            isConnected = false,
            isVirtual = true,
            rssiDbm = -70,
            macAddress = "C8:33:41:90:E2:AA",
            firmwareVersion = "v1.8.4"
        )
    )

    private val _availableDevices = MutableStateFlow<List<WearableDevice>>(defaultDevices)
    val availableDevices: StateFlow<List<WearableDevice>> = _availableDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<WearableDevice?>(defaultDevices[0])
    val connectedDevice: StateFlow<WearableDevice?> = _connectedDevice.asStateFlow()

    private val _biometrics = MutableStateFlow(HealthBiometrics())
    val biometrics: StateFlow<HealthBiometrics> = _biometrics.asStateFlow()

    // Real-time ECG points stream for live canvas visualization (normalized 0f to 1f)
    private val _ecgWave = MutableStateFlow<List<Float>>(generateInitialEcgPoints())
    val ecgWave: StateFlow<List<Float>> = _ecgWave.asStateFlow()

    private val _simulationMode = MutableStateFlow(SimulationEffortMode.REST)
    val simulationMode: StateFlow<SimulationEffortMode> = _simulationMode.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var telemetryJob: Job? = null
    private var ecgJob: Job? = null
    private var currentGatt: BluetoothGatt? = null

    init {
        startTelemetryStreaming()
        startEcgStreaming()
    }

    fun setSimulationEffort(mode: SimulationEffortMode) {
        _simulationMode.value = mode
    }

    fun connectDevice(device: WearableDevice) {
        externalScope.launch {
            _biometrics.update { it.copy(isLiveSyncing = true) }
            _availableDevices.update { list ->
                list.map {
                    if (it.id == device.id) it.copy(isConnected = true) else it.copy(isConnected = false)
                }
            }
            _connectedDevice.value = device.copy(isConnected = true)
        }
    }

    fun disconnectDevice() {
        externalScope.launch {
            val current = _connectedDevice.value ?: return@launch
            _availableDevices.update { list ->
                list.map { if (it.id == current.id) it.copy(isConnected = false) else it }
            }
            _connectedDevice.value = null
            _biometrics.update { it.copy(isLiveSyncing = false) }
            currentGatt?.disconnect()
            currentGatt?.close()
            currentGatt = null
        }
    }

    fun triggerManualSync() {
        externalScope.launch {
            _biometrics.update { it.copy(isLiveSyncing = true) }
            delay(1200)
            _biometrics.update {
                it.copy(
                    isLiveSyncing = false,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            }
            delay(400)
            _biometrics.update { it.copy(isLiveSyncing = true) }
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // BLE unavailable or disabled; keep virtual devices active
            return
        }
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        _isScanning.value = true

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { bleDev ->
                    val devName = bleDev.name ?: "BLE Sensor ${bleDev.address.takeLast(5)}"
                    val existing = _availableDevices.value.any { it.id == bleDev.address }
                    if (!existing) {
                        val newDev = WearableDevice(
                            id = bleDev.address,
                            name = devName,
                            type = DeviceType.UNKNOWN,
                            batteryLevel = 100,
                            isConnected = false,
                            isVirtual = false,
                            rssiDbm = result.rssi,
                            macAddress = bleDev.address
                        )
                        _availableDevices.update { it + newDev }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                _isScanning.value = false
            }
        }

        externalScope.launch {
            try {
                scanner.startScan(scanCallback)
                delay(10000)
                scanner.stopScan(scanCallback)
            } catch (_: SecurityException) {
            } finally {
                _isScanning.value = false
            }
        }
    }

    private fun startTelemetryStreaming() {
        telemetryJob?.cancel()
        telemetryJob = externalScope.launch(Dispatchers.Default) {
            var stepAccumulator = 6420
            var cadenceAccumulator = 0
            var tick = 0

            while (isActive) {
                delay(1000) // update biometrics once every second
                if (_connectedDevice.value == null) continue

                tick++
                val effort = _simulationMode.value
                val base = effort.baseBpm

                // Smooth organic micro-variation in pulse
                val variation = (sin(tick * 0.2) * 3 + Random.nextInt(-2, 3)).toInt()
                val currentBpm = (base + variation).coerceIn(45, 215)
                val zone = HeartRateZone.fromBpm(currentBpm)

                // Cadence & steps depending on effort
                val (spm, stepsStep) = when (effort) {
                    SimulationEffortMode.REST -> 0 to if (tick % 8 == 0) Random.nextInt(1, 3) else 0
                    SimulationEffortMode.MODERATE -> Random.nextInt(140, 160) to Random.nextInt(2, 4)
                    SimulationEffortMode.PEAK -> Random.nextInt(170, 195) to Random.nextInt(4, 6)
                    SimulationEffortMode.RECOVERY -> Random.nextInt(90, 110) to Random.nextInt(1, 3)
                }

                stepAccumulator += stepsStep
                cadenceAccumulator = spm

                val stress = when (effort) {
                    SimulationEffortMode.REST -> (20 + (sin(tick * 0.1) * 6).toInt()).coerceIn(10, 35)
                    SimulationEffortMode.MODERATE -> (52 + (sin(tick * 0.1) * 8).toInt()).coerceIn(40, 65)
                    SimulationEffortMode.PEAK -> (84 + (sin(tick * 0.1) * 6).toInt()).coerceIn(75, 96)
                    SimulationEffortMode.RECOVERY -> (45 + (sin(tick * 0.1) * 5).toInt()).coerceIn(35, 55)
                }

                val spO2 = when (effort) {
                    SimulationEffortMode.PEAK -> 96 + Random.nextInt(0, 2)
                    else -> 98 + Random.nextInt(0, 2)
                }

                _biometrics.update { current ->
                    current.copy(
                        heartRate = currentBpm,
                        hrZone = zone,
                        maxHeartRateToday = maxOf(current.maxHeartRateToday, currentBpm),
                        minHeartRateToday = minOf(current.minHeartRateToday, currentBpm),
                        spO2Percentage = spO2,
                        stressLevel = stress,
                        activeStepsToday = stepAccumulator,
                        cadenceSpm = cadenceAccumulator,
                        isLiveSyncing = true,
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    private fun startEcgStreaming() {
        ecgJob?.cancel()
        ecgJob = externalScope.launch(Dispatchers.Default) {
            val totalPoints = 60
            val buffer = generateInitialEcgPoints().toMutableList()
            var phase = 0

            while (isActive) {
                delay(50) // ~20 fps smooth ECG stream
                if (_connectedDevice.value == null) {
                    // Flatline when disconnected
                    buffer.removeAt(0)
                    buffer.add(0.5f)
                    _ecgWave.value = buffer.toList()
                    continue
                }

                val currentBpm = _biometrics.value.heartRate
                // Period calculated from BPM
                val beatIntervalTicks = ((60f / currentBpm) * 20).toInt().coerceIn(7, 24)
                phase = (phase + 1) % beatIntervalTicks

                // Generate P-Q-R-S-T wave point based on relative phase in current heartbeat
                val sample = calculateEcgSample(phase, beatIntervalTicks)
                buffer.removeAt(0)
                buffer.add(sample)
                _ecgWave.value = buffer.toList()
            }
        }
    }

    private fun calculateEcgSample(phase: Int, period: Int): Float {
        val rel = phase.toFloat() / period.toFloat()
        return when {
            // P wave
            rel in 0.10f..0.20f -> {
                val sub = (rel - 0.15f) / 0.05f
                0.5f + (0.12f * (1f - sub * sub).coerceAtLeast(0f))
            }
            // Q dip
            rel in 0.28f..0.32f -> {
                0.5f - 0.15f
            }
            // R spike (peak)
            rel in 0.32f..0.38f -> {
                val sub = (rel - 0.35f) / 0.03f
                0.5f + (0.45f * (1f - sub * sub).coerceAtLeast(0f))
            }
            // S dip
            rel in 0.38f..0.43f -> {
                0.5f - 0.22f
            }
            // T wave
            rel in 0.55f..0.72f -> {
                val sub = (rel - 0.635f) / 0.085f
                0.5f + (0.18f * (1f - sub * sub).coerceAtLeast(0f))
            }
            // Baseline with tiny realistic sensor noise
            else -> 0.5f + (Random.nextFloat() * 0.03f - 0.015f)
        }
    }

    private fun generateInitialEcgPoints(): List<Float> {
        return List(60) { 0.5f }
    }
}
