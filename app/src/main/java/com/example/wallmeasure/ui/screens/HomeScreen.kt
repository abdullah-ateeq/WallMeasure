package com.example.wallmeasure.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.data.ExportHelper
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.CalculationMode
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallOrientation
import com.example.wallmeasure.ui.components.AddEditOpeningDialog
import com.example.wallmeasure.ui.components.AllowanceSelector
import com.example.wallmeasure.ui.components.DimensionConverterCard
import com.example.wallmeasure.ui.components.ResultSummaryCard
import com.example.wallmeasure.ui.components.WallDiagram
import com.example.wallmeasure.viewmodel.WallMeasureUiState
import com.example.wallmeasure.viewmodel.WallMeasureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WallMeasureViewModel,
    uiState: WallMeasureUiState,
    onNavigateToOpenings: () -> Unit,
    onNavigateToMultipleWalls: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var dialogOpeningType by remember { mutableStateOf(OpeningType.DOOR) }

    // Input text states for Single Wall
    var widthInputText by remember(uiState.widthMm, uiState.unit) {
        mutableStateOf(
            UnitConverter.formatNumber(
                UnitConverter.fromMillimeters(uiState.widthMm, uiState.unit),
                uiState.settings.decimalPlaces
            )
        )
    }
    var heightInputText by remember(uiState.heightMm, uiState.unit) {
        mutableStateOf(
            UnitConverter.formatNumber(
                UnitConverter.fromMillimeters(uiState.heightMm, uiState.unit),
                uiState.settings.decimalPlaces
            )
        )
    }

    // Input text states for Four-Wall Room
    var roomLengthInputText by remember(uiState.roomLengthMm, uiState.unit) {
        mutableStateOf(
            UnitConverter.formatNumber(
                UnitConverter.fromMillimeters(uiState.roomLengthMm, uiState.unit),
                uiState.settings.decimalPlaces
            )
        )
    }
    var roomWidthInputText by remember(uiState.roomWidthMm, uiState.unit) {
        mutableStateOf(
            UnitConverter.formatNumber(
                UnitConverter.fromMillimeters(uiState.roomWidthMm, uiState.unit),
                uiState.settings.decimalPlaces
            )
        )
    }
    var roomHeightInputText by remember(uiState.roomHeightMm, uiState.unit) {
        mutableStateOf(
            UnitConverter.formatNumber(
                UnitConverter.fromMillimeters(uiState.roomHeightMm, uiState.unit),
                uiState.settings.decimalPlaces
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WallMeasure",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Calculate wall surface area quickly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMultipleWalls) {
                        Icon(imageVector = Icons.Default.ViewAgenda, contentDescription = "Multiple Walls")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calculation Mode Selector
            Text(
                text = "CALCULATION MODE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CalculationMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = uiState.calculationMode == mode,
                        onClick = {
                            if (mode == CalculationMode.MULTIPLE_WALLS) {
                                onNavigateToMultipleWalls()
                            } else {
                                viewModel.setCalculationMode(mode)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CalculationMode.entries.size)
                    ) {
                        Text(mode.title, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Global Unit Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INPUT UNIT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    var unitMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { unitMenuExpanded = true }) {
                            Text("${uiState.unit.displayName} (${uiState.unit.symbol})")
                        }
                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            UnitType.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.symbol})") },
                                    onClick = {
                                        viewModel.setUnit(u)
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // MODE 1: SINGLE WALL
            // =================================================================
            if (uiState.calculationMode == CalculationMode.SINGLE_WALL) {
                // Wall Name and Orientation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.wallName,
                        onValueChange = { viewModel.setWallName(it) },
                        label = { Text("Wall Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    var orientationExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { orientationExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(uiState.orientation.title)
                        }
                        DropdownMenu(
                            expanded = orientationExpanded,
                            onDismissRequest = { orientationExpanded = false }
                        ) {
                            WallOrientation.entries.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o.title) },
                                    onClick = {
                                        viewModel.setOrientation(o)
                                        orientationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Wall Dimensions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "WALL DIMENSIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = widthInputText,
                                onValueChange = {
                                    widthInputText = it
                                    val parsed = it.toDoubleOrNull()
                                    if (parsed != null && parsed > 0.0) {
                                        viewModel.setWallWidthMm(UnitConverter.toMillimeters(parsed, uiState.unit))
                                    }
                                },
                                label = { Text("Width (${uiState.unit.symbol})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = heightInputText,
                                onValueChange = {
                                    heightInputText = it
                                    val parsed = it.toDoubleOrNull()
                                    if (parsed != null && parsed > 0.0) {
                                        viewModel.setWallHeightMm(UnitConverter.toMillimeters(parsed, uiState.unit))
                                    }
                                },
                                label = { Text("Height (${uiState.unit.symbol})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Dimension Diagram
                if (uiState.settings.showDimensionDiagram) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIMENSION DIAGRAM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = uiState.showOpeningsOnDiagram,
                                    onCheckedChange = { viewModel.setShowOpeningsOnDiagram(it) }
                                )
                                Text(
                                    text = "Show openings",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        WallDiagram(
                            widthMm = uiState.widthMm,
                            heightMm = uiState.heightMm,
                            openings = uiState.openings,
                            unit = uiState.unit,
                            outputUnit = uiState.outputUnit,
                            orientation = uiState.orientation,
                            showOpenings = uiState.showOpeningsOnDiagram,
                            decimalPlaces = uiState.settings.decimalPlaces
                        )
                    }
                }

                // Dimension Converter Card
                DimensionConverterCard(
                    widthMm = uiState.widthMm,
                    heightMm = uiState.heightMm,
                    currentUnit = uiState.unit,
                    decimalPlaces = uiState.settings.decimalPlaces
                )

                // Dedicated Openings Quick Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OPENINGS (SUBTRACTED)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val totalOpeningArea = WallMeasureCalculator.calculateTotalOpeningArea(uiState.openings)
                                Text(
                                    text = "${uiState.openings.size} item(s) • ${UnitConverter.formatArea(totalOpeningArea, uiState.outputUnit, uiState.settings.decimalPlaces)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onNavigateToOpenings) {
                                Text("VIEW ALL")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Quick Add Buttons: [+ ADD DOOR], [+ ADD WINDOW], [+ ADD OTHER OPENING]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    dialogOpeningType = OpeningType.DOOR
                                    showAddOpeningDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DoorFront, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ DOOR", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    dialogOpeningType = OpeningType.WINDOW
                                    showAddOpeningDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Window, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ WINDOW", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    dialogOpeningType = OpeningType.OTHER
                                    showAddOpeningDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ OTHER", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Waste / Allowance Selector
                AllowanceSelector(
                    allowancePercent = uiState.allowancePercent,
                    onAllowanceChange = { viewModel.setAllowancePercent(it) }
                )

                // Quick Room Mode Shortcut
                OutlinedButton(
                    onClick = { viewModel.setCalculationMode(CalculationMode.FOUR_WALL_ROOM) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ROOM WALL CALCULATOR (4 WALLS)")
                }

                // Result Summary Card
                uiState.result?.let { res ->
                    val dimText = "${UnitConverter.formatDimension(uiState.widthMm, uiState.unit, uiState.settings.decimalPlaces, false)} × ${UnitConverter.formatDimension(uiState.heightMm, uiState.unit, uiState.settings.decimalPlaces, false)} ${uiState.unit.symbol}"
                    val activeWall = Wall(
                        id = "active_single",
                        name = uiState.wallName,
                        widthMm = uiState.widthMm,
                        heightMm = uiState.heightMm,
                        openings = uiState.openings
                    )

                    ResultSummaryCard(
                        result = res,
                        wallDimensionsText = dimText,
                        primaryUnit = uiState.outputUnit,
                        allowancePercent = uiState.allowancePercent,
                        decimalPlaces = uiState.settings.decimalPlaces,
                        showGross = uiState.settings.showGrossArea,
                        showOpenings = uiState.settings.showOpenings,
                        showNet = uiState.settings.showNetArea,
                        showPlanning = uiState.settings.showPlanningArea,
                        onOutputUnitChange = { viewModel.setOutputUnit(it) },
                        onCopyResult = {
                            val text = ExportHelper.buildCopyResultText(
                                wall = activeWall,
                                unit = uiState.unit,
                                outputUnit = uiState.outputUnit,
                                allowancePercent = uiState.allowancePercent,
                                decimalPlaces = uiState.settings.decimalPlaces
                            )
                            copyToClipboard(context, text, "Wall calculation copied")
                        },
                        onCopyArea = {
                            val areaText = UnitConverter.formatArea(res.netAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                            copyToClipboard(context, areaText, "Net area copied ($areaText)")
                        },
                        onShare = {
                            val text = ExportHelper.buildShareText(
                                wall = activeWall,
                                unit = uiState.unit,
                                outputUnit = uiState.outputUnit,
                                allowancePercent = uiState.allowancePercent,
                                decimalPlaces = uiState.settings.decimalPlaces
                            )
                            ExportHelper.shareCalculation(context, text, "WallMeasure — ${activeWall.name}")
                        },
                        onSave = {
                            viewModel.saveCurrentCalculation()
                        }
                    )
                }
            }

            // =================================================================
            // MODE 2: FOUR-WALL ROOM
            // =================================================================
            if (uiState.calculationMode == CalculationMode.FOUR_WALL_ROOM) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "FOUR-WALL ROOM DIMENSIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = roomLengthInputText,
                            onValueChange = {
                                roomLengthInputText = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    viewModel.setRoomLengthMm(UnitConverter.toMillimeters(parsed, uiState.unit))
                                }
                            },
                            label = { Text("Room Length (${uiState.unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = roomWidthInputText,
                            onValueChange = {
                                roomWidthInputText = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    viewModel.setRoomWidthMm(UnitConverter.toMillimeters(parsed, uiState.unit))
                                }
                            },
                            label = { Text("Room Width (${uiState.unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = roomHeightInputText,
                            onValueChange = {
                                roomHeightInputText = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    viewModel.setRoomHeightMm(UnitConverter.toMillimeters(parsed, uiState.unit))
                                }
                            },
                            label = { Text("Wall Height (${uiState.unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 4 walls breakdown
                        HorizontalDivider()
                        Text(
                            text = "GENERATED WALLS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val w1Area = uiState.roomLengthMm * uiState.roomHeightMm
                        val w2Area = uiState.roomWidthMm * uiState.roomHeightMm
                        val grossRoomArea = 2.0 * (uiState.roomLengthMm + uiState.roomWidthMm) * uiState.roomHeightMm

                        RoomWallRow("Wall 1 (Length)", uiState.roomLengthMm, uiState.roomHeightMm, w1Area, uiState.unit, uiState.outputUnit, uiState.settings.decimalPlaces)
                        RoomWallRow("Wall 2 (Width)", uiState.roomWidthMm, uiState.roomHeightMm, w2Area, uiState.unit, uiState.outputUnit, uiState.settings.decimalPlaces)
                        RoomWallRow("Wall 3 (Length)", uiState.roomLengthMm, uiState.roomHeightMm, w1Area, uiState.unit, uiState.outputUnit, uiState.settings.decimalPlaces)
                        RoomWallRow("Wall 4 (Width)", uiState.roomWidthMm, uiState.roomHeightMm, w2Area, uiState.unit, uiState.outputUnit, uiState.settings.decimalPlaces)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL GROSS WALL AREA", fontWeight = FontWeight.Bold)
                            Text(
                                UnitConverter.formatArea(grossRoomArea, uiState.outputUnit, uiState.settings.decimalPlaces),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Ceiling Calculator (Kept strictly separate!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.roomIncludeCeiling,
                                onCheckedChange = { viewModel.setRoomIncludeCeiling(it) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Include Ceiling Area (Separate Surface)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.roomIncludeCeiling) {
                            val ceilingArea = uiState.roomLengthMm * uiState.roomWidthMm
                            val grossWallArea = 2.0 * (uiState.roomLengthMm + uiState.roomWidthMm) * uiState.roomHeightMm
                            val combinedArea = grossWallArea + ceilingArea

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ceiling Area (Length × Width):", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        UnitConverter.formatArea(ceilingArea, uiState.outputUnit, uiState.settings.decimalPlaces),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Wall Area:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        UnitConverter.formatArea(grossWallArea, uiState.outputUnit, uiState.settings.decimalPlaces),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Combined Surface Area:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        UnitConverter.formatArea(combinedArea, uiState.outputUnit, uiState.settings.decimalPlaces),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Waste / Allowance Selector
                AllowanceSelector(
                    allowancePercent = uiState.allowancePercent,
                    onAllowanceChange = { viewModel.setAllowancePercent(it) }
                )

                // Room Mode Result Summary
                uiState.roomResult?.let { res ->
                    val dimText = "Room ${UnitConverter.formatDimension(uiState.roomLengthMm, uiState.unit, uiState.settings.decimalPlaces, false)} × ${UnitConverter.formatDimension(uiState.roomWidthMm, uiState.unit, uiState.settings.decimalPlaces, false)} • H ${UnitConverter.formatDimension(uiState.roomHeightMm, uiState.unit, uiState.settings.decimalPlaces, false)} ${uiState.unit.symbol}"

                    ResultSummaryCard(
                        result = res,
                        wallDimensionsText = dimText,
                        primaryUnit = uiState.outputUnit,
                        allowancePercent = uiState.allowancePercent,
                        decimalPlaces = uiState.settings.decimalPlaces,
                        showGross = uiState.settings.showGrossArea,
                        showOpenings = uiState.settings.showOpenings,
                        showNet = uiState.settings.showNetArea,
                        showPlanning = uiState.settings.showPlanningArea,
                        onOutputUnitChange = { viewModel.setOutputUnit(it) },
                        onCopyResult = {
                            val dummyWall = Wall("room_all", "Four-Wall Room", uiState.roomLengthMm, uiState.roomHeightMm)
                            val text = ExportHelper.buildCopyResultText(dummyWall, uiState.unit, uiState.outputUnit, uiState.allowancePercent, uiState.settings.decimalPlaces)
                            copyToClipboard(context, text, "Room calculation copied")
                        },
                        onCopyArea = {
                            val areaText = UnitConverter.formatArea(res.netAreaMm2, uiState.outputUnit, uiState.settings.decimalPlaces)
                            copyToClipboard(context, areaText, "Net area copied ($areaText)")
                        },
                        onShare = {
                            val dummyWall = Wall("room_all", "Four-Wall Room", uiState.roomLengthMm, uiState.roomHeightMm)
                            val text = ExportHelper.buildShareText(dummyWall, uiState.unit, uiState.outputUnit, uiState.allowancePercent, uiState.settings.decimalPlaces)
                            ExportHelper.shareCalculation(context, text, "WallMeasure — Four-Wall Room")
                        },
                        onSave = {
                            viewModel.saveCurrentCalculation()
                        }
                    )
                }
            }
        }
    }

    // Add Opening Dialog
    if (showAddOpeningDialog) {
        AddEditOpeningDialog(
            defaultType = dialogOpeningType,
            wallWidthMm = uiState.widthMm,
            wallHeightMm = uiState.heightMm,
            unit = uiState.unit,
            decimalPlaces = uiState.settings.decimalPlaces,
            onDismiss = { showAddOpeningDialog = false },
            onConfirm = { newOpening ->
                viewModel.addOpening(newOpening)
                showAddOpeningDialog = false
            }
        )
    }
}

@Composable
private fun RoomWallRow(
    name: String,
    widthMm: Double,
    heightMm: Double,
    areaMm2: Double,
    unit: UnitType,
    outputUnit: AreaUnitType,
    decimalPlaces: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$name (${UnitConverter.formatDimension(widthMm, unit, decimalPlaces, false)} × ${UnitConverter.formatDimension(heightMm, unit, decimalPlaces, false)})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = UnitConverter.formatArea(areaMm2, outputUnit, decimalPlaces),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("WallMeasure", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
