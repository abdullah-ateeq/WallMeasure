package com.example.wallmeasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import java.util.UUID

@Composable
fun AddEditOpeningDialog(
    initialOpening: Opening? = null,
    defaultType: OpeningType = OpeningType.DOOR,
    wallWidthMm: Double,
    wallHeightMm: Double,
    unit: UnitType,
    decimalPlaces: Int = 2,
    onDismiss: () -> Unit,
    onConfirm: (Opening) -> Unit
) {
    var type by remember { mutableStateOf(initialOpening?.type ?: defaultType) }
    var name by remember {
        mutableStateOf(
            initialOpening?.name ?: when (type) {
                OpeningType.DOOR -> "Main Door"
                OpeningType.WINDOW -> "Window"
                OpeningType.OTHER -> "Niche / Opening"
            }
        )
    }

    val initialWidthVal = initialOpening?.let {
        UnitConverter.formatNumber(UnitConverter.fromMillimeters(it.widthMm, unit), decimalPlaces)
    } ?: when (type) {
        OpeningType.DOOR -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(90.0, UnitType.CM), unit), decimalPlaces)
        OpeningType.WINDOW -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(150.0, UnitType.CM), unit), decimalPlaces)
        OpeningType.OTHER -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(80.0, UnitType.CM), unit), decimalPlaces)
    }

    val initialHeightVal = initialOpening?.let {
        UnitConverter.formatNumber(UnitConverter.fromMillimeters(it.heightMm, unit), decimalPlaces)
    } ?: when (type) {
        OpeningType.DOOR -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(210.0, UnitType.CM), unit), decimalPlaces)
        OpeningType.WINDOW -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(120.0, UnitType.CM), unit), decimalPlaces)
        OpeningType.OTHER -> UnitConverter.formatNumber(UnitConverter.fromMillimeters(UnitConverter.toMillimeters(60.0, UnitType.CM), unit), decimalPlaces)
    }

    var widthText by remember { mutableStateOf(initialWidthVal) }
    var heightText by remember { mutableStateOf(initialHeightVal) }
    var quantity by remember { mutableIntStateOf(initialOpening?.quantity ?: 1) }

    var specifyPosition by remember { mutableStateOf(initialOpening?.positionMm != null) }
    var posXText by remember {
        mutableStateOf(
            initialOpening?.positionMm?.let {
                UnitConverter.formatNumber(UnitConverter.fromMillimeters(it, unit), decimalPlaces)
            } ?: "0"
        )
    }
    var posYText by remember {
        mutableStateOf(
            initialOpening?.verticalPositionMm?.let {
                UnitConverter.formatNumber(UnitConverter.fromMillimeters(it, unit), decimalPlaces)
            } ?: "0"
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialOpening == null) "Add Opening" else "Edit Opening",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opening Type Selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    OpeningType.entries.forEachIndexed { index, opType ->
                        SegmentedButton(
                            selected = type == opType,
                            onClick = {
                                type = opType
                                if (initialOpening == null) {
                                    name = when (opType) {
                                        OpeningType.DOOR -> "Door"
                                        OpeningType.WINDOW -> "Window"
                                        OpeningType.OTHER -> "Opening"
                                    }
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = OpeningType.entries.size)
                        ) {
                            Text(opType.label)
                        }
                    }
                }

                if (type == OpeningType.OTHER) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rectangular opening estimation (niche, vent, service panel).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Opening Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Opening Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dimensions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = {
                            widthText = it
                            validationError = null
                        },
                        label = { Text("Width (${unit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = heightText,
                        onValueChange = {
                            heightText = it
                            validationError = null
                        },
                        label = { Text("Height (${unit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quantity (>= 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { quantity++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Optional Visual Placement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = specifyPosition,
                        onCheckedChange = { specifyPosition = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Specify diagram position",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (specifyPosition) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = posXText,
                            onValueChange = { posXText = it },
                            label = { Text("From Left (${unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = posYText,
                            onValueChange = { posYText = it },
                            label = { Text("From Floor (${unit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Validation error feedback
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wVal = widthText.toDoubleOrNull()
                    val hVal = heightText.toDoubleOrNull()
                    if (wVal == null || wVal <= 0.0) {
                        validationError = "Please enter a valid positive width."
                        return@Button
                    }
                    if (hVal == null || hVal <= 0.0) {
                        validationError = "Please enter a valid positive height."
                        return@Button
                    }

                    val widthMm = UnitConverter.toMillimeters(wVal, unit)
                    val heightMm = UnitConverter.toMillimeters(hVal, unit)

                    if (widthMm > wallWidthMm) {
                        validationError = "${type.label} width cannot exceed wall width."
                        return@Button
                    }
                    if (heightMm > wallHeightMm) {
                        validationError = "${type.label} height cannot exceed wall height."
                        return@Button
                    }

                    var posXMm: Double? = null
                    var posYMm: Double? = null
                    if (specifyPosition) {
                        val posXVal = posXText.toDoubleOrNull() ?: 0.0
                        val posYVal = posYText.toDoubleOrNull() ?: 0.0
                        posXMm = UnitConverter.toMillimeters(posXVal, unit)
                        posYMm = UnitConverter.toMillimeters(posYVal, unit)

                        if (posXMm < 0.0 || (posXMm + widthMm) > wallWidthMm) {
                            validationError = "${type.label} horizontal position exceeds wall width."
                            return@Button
                        }
                        if (posYMm < 0.0 || (posYMm + heightMm) > wallHeightMm) {
                            validationError = "${type.label} vertical position exceeds wall height."
                            return@Button
                        }
                    }

                    val finalOpening = Opening(
                        id = initialOpening?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { type.label },
                        type = type,
                        widthMm = widthMm,
                        heightMm = heightMm,
                        quantity = quantity,
                        positionMm = posXMm,
                        verticalPositionMm = posYMm
                    )
                    onConfirm(finalOpening)
                }
            ) {
                Text(if (initialOpening == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
