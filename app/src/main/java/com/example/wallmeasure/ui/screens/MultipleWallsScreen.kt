package com.example.wallmeasure.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.data.ExportHelper
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallHeightMode
import com.example.wallmeasure.viewmodel.WallMeasureUiState
import com.example.wallmeasure.viewmodel.WallMeasureViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleWallsScreen(
    viewModel: WallMeasureViewModel,
    uiState: WallMeasureUiState,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddWallDialog by remember { mutableStateOf(false) }
    var editingWall by remember { mutableStateOf<Wall?>(null) }
    var showQuickRoomDialog by remember { mutableStateOf(false) }

    // Export launcher
    var pendingExportType by remember { mutableStateOf("txt") }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val content = when (pendingExportType) {
                "csv" -> ExportHelper.generateCsvExport(
                    uiState.multipleWalls,
                    uiState.unit,
                    uiState.outputUnit,
                    uiState.allowancePercent,
                    uiState.settings.decimalPlaces
                )
                "json" -> ExportHelper.generateJsonExport(
                    uiState.multipleWalls,
                    "Multiple Walls Project",
                    uiState.unit,
                    uiState.outputUnit,
                    uiState.allowancePercent,
                    uiState.settings.decimalPlaces
                )
                else -> ExportHelper.generateTxtExport(
                    uiState.multipleWalls,
                    "Multiple Walls Project",
                    uiState.unit,
                    uiState.outputUnit,
                    uiState.allowancePercent,
                    uiState.settings.decimalPlaces
                )
            }
            val success = ExportHelper.writeTextToUri(context, uri, content)
            if (success) {
                Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiple Walls", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var exportMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { exportMenuExpanded = true }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Export")
                    }
                    DropdownMenu(
                        expanded = exportMenuExpanded,
                        onDismissRequest = { exportMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export as TXT") },
                            onClick = {
                                exportMenuExpanded = false
                                pendingExportType = "txt"
                                exportLauncher.launch("wallmeasure_walls.txt")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as CSV") },
                            onClick = {
                                exportMenuExpanded = false
                                pendingExportType = "csv"
                                exportLauncher.launch("wallmeasure_walls.csv")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as JSON") },
                            onClick = {
                                exportMenuExpanded = false
                                pendingExportType = "json"
                                exportLauncher.launch("wallmeasure_walls.json")
                            }
                        )
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
            // Summary Card
            uiState.multipleWallsResult?.let { totalResult ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "WALL SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Walls Count", style = MaterialTheme.typography.bodySmall)
                            Text("${uiState.multipleWalls.size}", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Gross Area", style = MaterialTheme.typography.bodySmall)
                            Text(
                                UnitConverter.formatArea(totalResult.grossAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Openings Area", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "- ${UnitConverter.formatArea(totalResult.openingAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL NET WALL AREA", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                UnitConverter.formatArea(totalResult.netAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (uiState.allowancePercent > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Planning Area (${UnitConverter.formatNumber(uiState.allowancePercent, 1)}% Allowance):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Text(
                                    UnitConverter.formatArea(totalResult.planningAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Save to history & Share buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.saveCurrentCalculation() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAVE")
                            }
                            OutlinedButton(
                                onClick = {
                                    val txt = ExportHelper.generateTxtExport(
                                        uiState.multipleWalls,
                                        "Multiple Walls Project",
                                        uiState.unit,
                                        uiState.outputUnit,
                                        uiState.allowancePercent,
                                        uiState.settings.decimalPlaces
                                    )
                                    ExportHelper.shareCalculation(context, txt, "Multiple Walls Project")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SHARE")
                            }
                        }
                    }
                }
            }

            // Height Modes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HEIGHT MODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.heightMode == WallHeightMode.SAME_HEIGHT,
                            onClick = { viewModel.setHeightMode(WallHeightMode.SAME_HEIGHT) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Same height for all walls")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.heightMode == WallHeightMode.INDIVIDUAL_HEIGHT,
                            onClick = { viewModel.setHeightMode(WallHeightMode.INDIVIDUAL_HEIGHT) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Individual height")
                    }

                    if (uiState.heightMode == WallHeightMode.SAME_HEIGHT) {
                        var sharedHeightText by remember(uiState.sharedHeightMm, uiState.unit) {
                            mutableStateOf(
                                UnitConverter.formatNumber(
                                    UnitConverter.fromMillimeters(uiState.sharedHeightMm, uiState.unit),
                                    uiState.settings.decimalPlaces
                                )
                            )
                        }
                        OutlinedTextField(
                            value = sharedHeightText,
                            onValueChange = {
                                sharedHeightText = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    viewModel.setSharedHeight(UnitConverter.toMillimeters(parsed, uiState.unit))
                                }
                            },
                            label = { Text("Shared Wall Height (${uiState.unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Action Buttons: [+ ADD WALL] & [CALCULATE 4 WALLS]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        editingWall = null
                        showAddWallDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD WALL")
                }

                OutlinedButton(
                    onClick = { showQuickRoomDialog = true },
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CALCULATE 4 WALLS")
                }
            }

            // Wall Cards List
            Text(
                text = "WALLS (${uiState.multipleWalls.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.multipleWalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No walls added yet.\nTap 'ADD WALL' or 'CALCULATE 4 WALLS' to begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                uiState.multipleWalls.forEach { wall ->
                    val res = WallMeasureCalculator.calculateWallResult(wall, uiState.allowancePercent)
                    val wFmt = UnitConverter.formatDimension(wall.widthMm, uiState.unit, uiState.settings.decimalPlaces, false)
                    val hFmt = UnitConverter.formatDimension(wall.heightMm, uiState.unit, uiState.settings.decimalPlaces, false)
                    val grossFmt = UnitConverter.formatArea(res.grossAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                    val openFmt = UnitConverter.formatArea(res.openingAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                    val netFmt = UnitConverter.formatArea(res.netAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = wall.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$wFmt × $hFmt ${uiState.unit.symbol}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(onClick = {
                                        editingWall = wall
                                        showAddWallDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.duplicateWall(wall.id) }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                                    }
                                    IconButton(onClick = { viewModel.removeWall(wall.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gross: $grossFmt", style = MaterialTheme.typography.bodySmall)
                                Text("Openings: $openFmt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                Text("Net: $netFmt", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Wall Dialog
    if (showAddWallDialog) {
        AddEditWallDialog(
            initialWall = editingWall,
            defaultHeightMm = if (uiState.heightMode == WallHeightMode.SAME_HEIGHT) uiState.sharedHeightMm else 3000.0,
            isHeightFixed = uiState.heightMode == WallHeightMode.SAME_HEIGHT,
            unit = uiState.unit,
            decimalPlaces = uiState.settings.decimalPlaces,
            onDismiss = { showAddWallDialog = false },
            onConfirm = { finalWall ->
                if (editingWall != null) {
                    viewModel.updateWall(finalWall)
                } else {
                    viewModel.addWall(finalWall)
                }
                showAddWallDialog = false
            }
        )
    }

    // Quick 4 Walls Generator Dialog
    if (showQuickRoomDialog) {
        QuickRoomGeneratorDialog(
            unit = uiState.unit,
            decimalPlaces = uiState.settings.decimalPlaces,
            onDismiss = { showQuickRoomDialog = false },
            onConfirm = { lengthMm, widthMm, heightMm ->
                viewModel.generateFourWallsFromRoom(lengthMm, widthMm, heightMm)
                showQuickRoomDialog = false
            }
        )
    }
}

@Composable
private fun AddEditWallDialog(
    initialWall: Wall?,
    defaultHeightMm: Double,
    isHeightFixed: Boolean,
    unit: UnitType,
    decimalPlaces: Int,
    onDismiss: () -> Unit,
    onConfirm: (Wall) -> Unit
) {
    var name by remember { mutableStateOf(initialWall?.name ?: "Wall") }
    var widthText by remember {
        mutableStateOf(
            initialWall?.let {
                UnitConverter.formatNumber(UnitConverter.fromMillimeters(it.widthMm, unit), decimalPlaces)
            } ?: "500"
        )
    }
    var heightText by remember {
        mutableStateOf(
            initialWall?.let {
                UnitConverter.formatNumber(UnitConverter.fromMillimeters(it.heightMm, unit), decimalPlaces)
            } ?: UnitConverter.formatNumber(UnitConverter.fromMillimeters(defaultHeightMm, unit), decimalPlaces)
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialWall == null) "Add Wall" else "Edit Wall", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wall Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = it; error = null },
                    label = { Text("Width (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isHeightFixed) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it; error = null },
                        label = { Text("Height (${unit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Height is fixed to ${UnitConverter.formatDimension(defaultHeightMm, unit, decimalPlaces)} across all walls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wVal = widthText.toDoubleOrNull()
                    val hVal = if (isHeightFixed) UnitConverter.fromMillimeters(defaultHeightMm, unit) else heightText.toDoubleOrNull()
                    if (wVal == null || wVal <= 0.0) {
                        error = "Please enter a valid positive width."
                        return@Button
                    }
                    if (hVal == null || hVal <= 0.0) {
                        error = "Please enter a valid positive height."
                        return@Button
                    }
                    val wall = Wall(
                        id = initialWall?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "Wall" },
                        widthMm = UnitConverter.toMillimeters(wVal, unit),
                        heightMm = UnitConverter.toMillimeters(hVal, unit),
                        openings = initialWall?.openings ?: emptyList()
                    )
                    onConfirm(wall)
                }
            ) {
                Text(if (initialWall == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun QuickRoomGeneratorDialog(
    unit: UnitType,
    decimalPlaces: Int,
    onDismiss: () -> Unit,
    onConfirm: (lengthMm: Double, widthMm: Double, heightMm: Double) -> Unit
) {
    var lengthText by remember { mutableStateOf("500") }
    var widthText by remember { mutableStateOf("400") }
    var heightText by remember { mutableStateOf("300") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calculate 4 Walls (Room)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Generates 4 walls automatically: 2 for length and 2 for width.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it; error = null },
                    label = { Text("Room Length (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = it; error = null },
                    label = { Text("Room Width (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it; error = null },
                    label = { Text("Wall Height (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lVal = lengthText.toDoubleOrNull()
                    val wVal = widthText.toDoubleOrNull()
                    val hVal = heightText.toDoubleOrNull()
                    if (lVal == null || lVal <= 0.0 || wVal == null || wVal <= 0.0 || hVal == null || hVal <= 0.0) {
                        error = "Please enter valid positive dimensions."
                        return@Button
                    }
                    onConfirm(
                        UnitConverter.toMillimeters(lVal, unit),
                        UnitConverter.toMillimeters(wVal, unit),
                        UnitConverter.toMillimeters(hVal, unit)
                    )
                }
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
