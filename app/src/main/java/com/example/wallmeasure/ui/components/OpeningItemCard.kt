package com.example.wallmeasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.ui.theme.DoorColor
import com.example.wallmeasure.ui.theme.OtherOpeningColor
import com.example.wallmeasure.ui.theme.WindowColor

@Composable
fun OpeningItemCard(
    opening: Opening,
    unit: UnitType,
    outputUnit: AreaUnitType,
    decimalPlaces: Int = 2,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAreaMm2 = WallMeasureCalculator.calculateOpeningArea(opening)
    val areaFormatted = UnitConverter.formatArea(totalAreaMm2, outputUnit, decimalPlaces)
    val widthFormatted = UnitConverter.formatDimension(opening.widthMm, unit, decimalPlaces)
    val heightFormatted = UnitConverter.formatDimension(opening.heightMm, unit, decimalPlaces)

    val (icon, tint) = when (opening.type) {
        OpeningType.DOOR -> Icons.Default.DoorFront to DoorColor
        OpeningType.WINDOW -> Icons.Default.Window to WindowColor
        OpeningType.OTHER -> Icons.Default.CropSquare to OtherOpeningColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = opening.type.label,
                    tint = tint,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = opening.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (opening.quantity > 1) {
                            Text(
                                text = "  × ${opening.quantity}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = "$widthFormatted × $heightFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "= $areaFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = tint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Actions: Edit, Duplicate, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
