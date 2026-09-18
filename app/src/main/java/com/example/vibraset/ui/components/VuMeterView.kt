package com.example.vibraset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VuMeterView(
    leftPeak: Float,
    rightPeak: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("vu_meter_dual")
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RackSurfaceVariant)
            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChannelVuBar(label = "CH-L", peak = leftPeak, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        ChannelVuBar(label = "CH-R", peak = rightPeak, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChannelVuBar(
    label: String,
    peak: Float,
    modifier: Modifier = Modifier
) {
    val totalSegments = 16
    val activeSegments = (peak * totalSegments).toInt().coerceIn(0, totalSegments)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = NeonCyan
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF070B12))
                .padding(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            for (i in 0 until totalSegments) {
                val isActive = i < activeSegments
                val segmentColor = when {
                    i >= 14 -> if (isActive) MeterRed else Color(0x33EF4444)
                    i >= 11 -> if (isActive) MeterYellow else Color(0x33FACC15)
                    else -> if (isActive) MeterGreen else Color(0x2210B981)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(segmentColor)
                )
            }
        }

        // Peak Clip LED
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(3.5.dp))
                .background(if (peak >= 0.92f) MeterRed else Color(0x33EF4444))
        )
    }
}
