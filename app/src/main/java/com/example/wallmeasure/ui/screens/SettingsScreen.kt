package com.example.wallmeasure.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wallmeasure.model.AppThemeSetting
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.WallMeasureSettings
import com.example.wallmeasure.viewmodel.WallMeasureUiState
import com.example.wallmeasure.viewmodel.WallMeasureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WallMeasureViewModel,
    uiState: WallMeasureUiState,
    onNavigateBack: () -> Unit
) {
    val settings = uiState.settings
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. APPEARANCE
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "APPEARANCE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppThemeSetting.entries.forEachIndexed { index, theme ->
                            SegmentedButton(
                                selected = settings.theme == theme,
                                onClick = { viewModel.updateSettings(settings.copy(theme = theme)) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppThemeSetting.entries.size)
                            ) {
                                Text(theme.title)
                            }
                        }
                    }

                    HorizontalDivider()

                    SettingsToggleRow(
                        title = "Dynamic Color",
                        subtitle = "Use Material You wallpaper colors (Android 12+)",
                        checked = settings.dynamicColor,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                    )
                }
            }

            // 2. CALCULATOR SETTINGS
            SettingsSectionHeader(icon = Icons.Default.Tune, title = "CALCULATOR DEFAULTS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Default Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Unit", fontWeight = FontWeight.Medium)
                            Text("Initial dimension input unit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var unitMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(onClick = { unitMenuExpanded = true }) {
                            Text(settings.defaultUnit.displayName)
                        }
                        DropdownMenu(expanded = unitMenuExpanded, onDismissRequest = { unitMenuExpanded = false }) {
                            UnitType.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u.displayName) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(defaultUnit = u))
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Default Output Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Output Unit", fontWeight = FontWeight.Medium)
                            Text("Initial area calculation display", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var outMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(onClick = { outMenuExpanded = true }) {
                            Text(settings.defaultOutputUnit.symbol)
                        }
                        DropdownMenu(expanded = outMenuExpanded, onDismissRequest = { outMenuExpanded = false }) {
                            AreaUnitType.entries.forEach { out ->
                                DropdownMenuItem(
                                    text = { Text("${out.symbol} (${out.displayName})") },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(defaultOutputUnit = out))
                                        outMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Decimal Places: 0, 1, 2, 3, 4
                    Text("Decimal Places", fontWeight = FontWeight.Medium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val places = listOf(0, 1, 2, 3, 4)
                        places.forEachIndexed { index, p ->
                            SegmentedButton(
                                selected = settings.decimalPlaces == p,
                                onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = p)) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = places.size)
                            ) {
                                Text("$p")
                            }
                        }
                    }
                }
            }

            // 3. DISPLAY SETTINGS
            SettingsSectionHeader(icon = Icons.Default.Visibility, title = "DISPLAY SETTINGS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsToggleRow(
                        title = "Show Dimension Diagram",
                        subtitle = "Display auto-scaled proportional wall box",
                        checked = settings.showDimensionDiagram,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showDimensionDiagram = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Show Gross Area",
                        subtitle = "Width × Height surface area row",
                        checked = settings.showGrossArea,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showGrossArea = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Show Openings Subtractions",
                        subtitle = "Display opening deduction row",
                        checked = settings.showOpenings,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showOpenings = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Show Net Area",
                        subtitle = "Final usable wall surface area",
                        checked = settings.showNetArea,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showNetArea = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Show Planning Area",
                        subtitle = "Material allowance with waste safety factor",
                        checked = settings.showPlanningArea,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showPlanningArea = it)) }
                    )
                }
            }

            // 4. BEHAVIOR
            SettingsSectionHeader(icon = Icons.Default.TouchApp, title = "BEHAVIOR")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsToggleRow(
                        title = "Live Calculation",
                        subtitle = "Recalculate immediately as inputs change",
                        checked = settings.liveCalculation,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(liveCalculation = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Haptics & Feedback",
                        subtitle = "Subtle tactile cues on actions",
                        checked = settings.haptics,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(haptics = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Animations",
                        subtitle = "Smooth dimension and expand transitions",
                        checked = settings.animations,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(animations = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Confirm Delete",
                        subtitle = "Show warning before clearing history items",
                        checked = settings.confirmDelete,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Restore Last Calculation",
                        subtitle = "Remember previous dimensions when reopening app",
                        checked = settings.restoreLastCalculation,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(restoreLastCalculation = it)) }
                    )
                }
            }

            // 5. RESET SETTINGS
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Settings")
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings", fontWeight = FontWeight.Bold) },
            text = { Text("Reset all preferences to default values? Your saved history will NOT be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
