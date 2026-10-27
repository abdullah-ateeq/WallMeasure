package com.example.wallmeasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AllowanceSelector(
    allowancePercent: Double,
    onAllowanceChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(0.0, 5.0, 10.0, 15.0)
    val isPreset = options.contains(allowancePercent)
    var showCustomInput by remember { mutableStateOf(!isPreset && allowancePercent > 0.0) }
    var customText by remember { mutableStateOf(if (!isPreset && allowancePercent > 0.0) allowancePercent.toString() else "20") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Text(
                    text = "MATERIAL ALLOWANCE / WASTE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (allowancePercent <= 0.0) "OFF" else "+${allowancePercent}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (allowancePercent > 0.0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val labels = listOf("Off", "5%", "10%", "15%", "Custom")
                val values = listOf(0.0, 5.0, 10.0, 15.0, -1.0)

                values.forEachIndexed { index, value ->
                    val isSelected = if (value == -1.0) {
                        showCustomInput || (!isPreset && allowancePercent > 0.0)
                    } else {
                        !showCustomInput && allowancePercent == value
                    }

                    SegmentedButton(
                        selected = isSelected,
                        onClick = {
                            if (value == -1.0) {
                                showCustomInput = true
                                val customVal = customText.toDoubleOrNull() ?: 20.0
                                onAllowanceChange(customVal)
                            } else {
                                showCustomInput = false
                                onAllowanceChange(value)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = values.size)
                    ) {
                        Text(labels[index], style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (showCustomInput) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed >= 0.0) {
                                onAllowanceChange(parsed)
                            }
                        },
                        label = { Text("Custom Allowance (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Planning Area: adds safety margin for cuts, tiles, or wallpaper. Measured net wall area remains unmodified.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
