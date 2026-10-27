package com.example.wallmeasure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.ui.components.AddEditOpeningDialog
import com.example.wallmeasure.ui.components.OpeningItemCard
import com.example.wallmeasure.viewmodel.WallMeasureUiState
import com.example.wallmeasure.viewmodel.WallMeasureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningsScreen(
    viewModel: WallMeasureViewModel,
    uiState: WallMeasureUiState,
    onNavigateBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingOpening by remember { mutableStateOf<Opening?>(null) }
    var defaultType by remember { mutableStateOf(OpeningType.DOOR) }

    val grossAreaMm2 = WallMeasureCalculator.calculateGrossArea(uiState.widthMm, uiState.heightMm)
    val totalOpeningsMm2 = WallMeasureCalculator.calculateTotalOpeningArea(uiState.openings)
    val isExceeding = totalOpeningsMm2 > grossAreaMm2

    // Category sums
    val doorAreaMm2 = uiState.openings.filter { it.type == OpeningType.DOOR }
        .sumOf { WallMeasureCalculator.calculateOpeningArea(it) }
    val windowAreaMm2 = uiState.openings.filter { it.type == OpeningType.WINDOW }
        .sumOf { WallMeasureCalculator.calculateOpeningArea(it) }
    val otherAreaMm2 = uiState.openings.filter { it.type == OpeningType.OTHER }
        .sumOf { WallMeasureCalculator.calculateOpeningArea(it) }

    val netAreaMm2 = if (grossAreaMm2 - totalOpeningsMm2 < 0.0) 0.0 else grossAreaMm2 - totalOpeningsMm2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Openings", fontWeight = FontWeight.Bold) },
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
            // Target Wall Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TARGET WALL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.wallName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val wFmt = UnitConverter.formatDimension(uiState.widthMm, uiState.unit, uiState.settings.decimalPlaces)
                    val hFmt = UnitConverter.formatDimension(uiState.heightMm, uiState.unit, uiState.settings.decimalPlaces)
                    val gFmt = UnitConverter.formatArea(grossAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                    Text(
                        text = "Dimensions: $wFmt × $hFmt • Gross Area: $gFmt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Exceeds Warning Banner
            if (isExceeding) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "⚠ OPENING AREA EXCEEDS WALL AREA",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Openings (${UnitConverter.formatArea(totalOpeningsMm2, uiState.outputUnit, uiState.settings.decimalPlaces)}) exceed gross area (${UnitConverter.formatArea(grossAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)}). Net usable area is clamped to 0.00.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }

            // Openings Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OPENINGS SUMMARY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SummaryLine("Doors", doorAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                    SummaryLine("Windows", windowAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                    SummaryLine("Other Rectangular Openings", otherAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL OPENINGS", fontWeight = FontWeight.Bold)
                        Text(
                            UnitConverter.formatArea(totalOpeningsMm2, uiState.outputUnit, uiState.settings.decimalPlaces),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("NET WALL AREA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            UnitConverter.formatArea(netAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Add Opening Action Buttons
            Text(
                text = "ADD SUBTRACTION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            editingOpening = null
                            defaultType = OpeningType.DOOR
                            showDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DoorFront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ ADD DOOR")
                    }

                    Button(
                        onClick = {
                            editingOpening = null
                            defaultType = OpeningType.WINDOW
                            showDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Window, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ ADD WINDOW")
                    }
                }

                OutlinedButton(
                    onClick = {
                        editingOpening = null
                        defaultType = OpeningType.OTHER
                        showDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ ADD OTHER OPENING")
                }
            }

            // Openings List
            Text(
                text = "CONFIGURED OPENINGS (${uiState.openings.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.openings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No openings added yet.\nTap above to subtract doors, windows, or niches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                uiState.openings.forEach { op ->
                    OpeningItemCard(
                        opening = op,
                        unit = uiState.unit,
                        outputUnit = uiState.outputUnit,
                        decimalPlaces = uiState.settings.decimalPlaces,
                        onEdit = {
                            editingOpening = op
                            defaultType = op.type
                            showDialog = true
                        },
                        onDuplicate = {
                            viewModel.duplicateOpening(op.id)
                        },
                        onDelete = {
                            viewModel.removeOpening(op.id)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddEditOpeningDialog(
            initialOpening = editingOpening,
            defaultType = defaultType,
            wallWidthMm = uiState.widthMm,
            wallHeightMm = uiState.heightMm,
            unit = uiState.unit,
            decimalPlaces = uiState.settings.decimalPlaces,
            onDismiss = { showDialog = false },
            onConfirm = { updated ->
                if (editingOpening != null) {
                    viewModel.updateOpening(updated)
                } else {
                    viewModel.addOpening(updated)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun SummaryLine(label: String, areaMm2: Double, outputUnit: com.example.wallmeasure.model.AreaUnitType, decimalPlaces: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = UnitConverter.formatArea(areaMm2, outputUnit, decimalPlaces),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
