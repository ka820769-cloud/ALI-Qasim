package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WearableDevice
import com.example.ui.FitnessViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.wearable.SimulationEffortMode

@Composable
fun WearableSyncScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val biometrics by viewModel.biometrics.collectAsStateWithLifecycle()
    val simulationMode by viewModel.simulationMode.collectAsStateWithLifecycle()
    val isScanningBle by viewModel.isScanningBle.collectAsStateWithLifecycle()
    val userWeightKg by viewModel.userWeightKg.collectAsStateWithLifecycle()
    val dailyCalorieGoal by viewModel.dailyCalorieGoal.collectAsStateWithLifecycle()
    val dailyDurationGoal by viewModel.dailyDurationGoal.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Wearable Sync",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Bluetooth Low Energy & Virtual Sensor Telemetry",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Active Connected Device Card
        item {
            Text(
                text = "CURRENT WEARABLE DEVICE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (connectedDevice != null) {
                val dev = connectedDevice!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkSurface)
                        .border(1.5.dp, PulseCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("connected_device_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(PulseCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = dev.type.iconEmoji, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dev.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Connected",
                                            tint = PulseMint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "${dev.type.label} • ${dev.macAddress}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Battery Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Battery5Bar,
                                        contentDescription = "Battery",
                                        tint = if (dev.batteryLevel > 20) PulseMint else PulseCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${dev.batteryLevel}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Telemetry status row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (biometrics.isLiveSyncing) PulseMint else TextSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (biometrics.isLiveSyncing) "Live Telemetry Streaming" else "Sync Paused",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (biometrics.isLiveSyncing) PulseMint else TextSecondary
                                )
                            }
                            Text(
                                text = "RSSI ${dev.rssiDbm} dBm",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Force Sync & Disconnect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerWearableSync() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PulseCyan,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.disconnectWearable() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PulseCoral
                                )
                            ) {
                                Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                // No device connected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Wearable Connected",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select one of the paired devices below to start real-time health telemetry",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    }
                }
            }
        }

        // Live Telemetry Simulation Control (Rest vs Aerobic vs Sprint)
        item {
            Text(
                text = "SIMULATED WEARABLE EFFORT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Text(
                text = "Adjust the sensor effort mode to observe real-time heart rate & zone responses:",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimulationEffortMode.entries.forEach { mode ->
                    val isSelected = simulationMode == mode
                    val modeColor = when (mode) {
                        SimulationEffortMode.REST -> PulseMint
                        SimulationEffortMode.MODERATE -> PulseCyan
                        SimulationEffortMode.PEAK -> PulseCoral
                        SimulationEffortMode.RECOVERY -> PulseAmber
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) modeColor.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) modeColor else DarkSurfaceVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setSimulationEffort(mode) }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) modeColor else TextPrimary
                            )
                            Text(
                                text = "~${mode.baseBpm} bpm",
                                fontSize = 10.sp,
                                color = if (isSelected) modeColor else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Available Wearable Devices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE SENSORS & DEVICES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )

                TextButton(
                    onClick = { viewModel.startBleScan() },
                    enabled = !isScanningBle
                ) {
                    if (isScanningBle) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = PulseCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scanning...", fontSize = 12.sp, color = PulseCyan)
                    } else {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = "Scan",
                            tint = PulseCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan BLE", fontSize = 12.sp, color = PulseCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(availableDevices, key = { it.id }) { device ->
            DeviceRowItem(
                device = device,
                onConnect = { viewModel.connectWearable(device) }
            )
        }

        // Profile & Calculation Settings (Body weight & Calorie goal)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CALORIE ESTIMATION SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    // Body Weight slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = "Weight",
                                tint = PulseCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Body Weight (for ACSM formula)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${userWeightKg.toInt()} kg",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCyan
                        )
                    }

                    Slider(
                        value = userWeightKg,
                        onValueChange = { viewModel.updateUserWeight(it) },
                        valueRange = 40f..150f,
                        steps = 110,
                        colors = SliderDefaults.colors(
                            thumbColor = PulseCyan,
                            activeTrackColor = PulseCyan,
                            inactiveTrackColor = DarkSurfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Calorie Goal slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Calorie Goal",
                                tint = PulseAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Calorie Burn Target",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "$dailyCalorieGoal kcal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseAmber
                        )
                    }

                    Slider(
                        value = dailyCalorieGoal.toFloat(),
                        onValueChange = { viewModel.updateCalorieGoal(it.toInt()) },
                        valueRange = 100f..2000f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = PulseAmber,
                            activeTrackColor = PulseAmber,
                            inactiveTrackColor = DarkSurfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Duration Goal slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Duration Goal",
                                tint = PulseCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Exercise Duration Target",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "$dailyDurationGoal mins",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCyan
                        )
                    }

                    Slider(
                        value = dailyDurationGoal.toFloat(),
                        onValueChange = { viewModel.updateDurationGoal(it.toInt()) },
                        valueRange = 10f..180f,
                        steps = 33,
                        colors = SliderDefaults.colors(
                            thumbColor = PulseCyan,
                            activeTrackColor = PulseCyan,
                            inactiveTrackColor = DarkSurfaceElevated
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceRowItem(
    device: WearableDevice,
    onConnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (device.isConnected) PulseCyan else DarkSurfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = device.type.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${device.type.label} • Battery: ${device.batteryLevel}%",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (device.isConnected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PulseCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PAIRED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PulseCyan
                    )
                }
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
