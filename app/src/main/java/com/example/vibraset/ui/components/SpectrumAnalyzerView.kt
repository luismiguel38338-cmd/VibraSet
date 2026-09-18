package com.example.vibraset.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun SpectrumAnalyzerView(
    spectrumData: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Keep track of falling peak values for smooth decay
    val peakValues = remember { FloatArray(spectrumData.size) { 0f } }

    LaunchedEffect(spectrumData) {
        for (i in spectrumData.indices) {
            val current = spectrumData[i]
            if (current > peakValues[i]) {
                peakValues[i] = current
            } else {
                peakValues[i] = max(0f, peakValues[i] - 0.025f)
            }
        }
    }

    Box(
        modifier = modifier
            .testTag("spectrum_analyzer_card")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RackSurface)
            .border(1.dp, BezelBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            // Header: Display Title & Real-Time indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isPlaying) NeonCyan else TextDim)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ANALIZADOR DE ESPECTRO RTA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isPlaying) NeonCyan else TextMuted
                    )
                }

                // Range legends (Graves, Medios, Agudos)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpectrumLegendPill(name = "SUB/BASS", color = NeonRed)
                    SpectrumLegendPill(name = "GRAVES", color = NeonAmber)
                    SpectrumLegendPill(name = "MEDIOS", color = NeonGreen)
                    SpectrumLegendPill(name = "AGUDOS", color = NeonCyan)
                }
            }

            // Main Canvas
            Canvas(
                modifier = Modifier
                    .testTag("spectrum_canvas")
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070B12))
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw background frequency grid lines
                val gridLines = 4
                for (g in 1..gridLines) {
                    val y = canvasHeight * (g.toFloat() / (gridLines + 1))
                    drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                val barCount = spectrumData.size
                val barSpacing = 3.dp.toPx()
                val totalSpacing = barSpacing * (barCount + 1)
                val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(2f)

                for (i in 0 until barCount) {
                    val x = barSpacing + i * (barWidth + barSpacing)
                    val rawMag = spectrumData[i].coerceIn(0.02f, 1f)
                    val barHeight = (canvasHeight * rawMag * 0.92f).coerceAtLeast(3f)
                    val y = canvasHeight - barHeight

                    // Color selection based on spectrum zone
                    // 0..4 = Subgraves (NeonRed), 5..10 = Graves (NeonAmber), 11..22 = Medios (NeonGreen), 23..31 = Agudos (NeonCyan)
                    val barBrush = when {
                        i < 5 -> Brush.verticalGradient(
                            colors = listOf(NeonRed, Color(0xFF991B1B)),
                            startY = y,
                            endY = canvasHeight
                        )
                        i < 11 -> Brush.verticalGradient(
                            colors = listOf(NeonAmber, Color(0xFFB45309)),
                            startY = y,
                            endY = canvasHeight
                        )
                        i < 22 -> Brush.verticalGradient(
                            colors = listOf(NeonGreen, Color(0xFF047857)),
                            startY = y,
                            endY = canvasHeight
                        )
                        else -> Brush.verticalGradient(
                            colors = listOf(NeonCyan, Color(0xFF0284C7)),
                            startY = y,
                            endY = canvasHeight
                        )
                    }

                    // Draw main bar
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Draw peak hold line
                    val peakMag = peakValues[i].coerceIn(0f, 1f)
                    val peakY = (canvasHeight - (canvasHeight * peakMag * 0.92f) - 4f).coerceAtLeast(2f)
                    val peakColor = when {
                        i < 5 -> Color(0xFFFF6B81)
                        i < 11 -> Color(0xFFFFB866)
                        i < 22 -> Color(0xFF6EE7B7)
                        else -> Color(0xFF7DD3FC)
                    }

                    drawRoundRect(
                        color = peakColor,
                        topLeft = Offset(x, peakY),
                        size = Size(barWidth, 2.5.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }

            // Frequency Labels at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("20Hz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonRed)
                Text("60Hz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonAmber)
                Text("250Hz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonAmber)
                Text("1kHz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonGreen)
                Text("4kHz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonCyan)
                Text("10kHz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonCyan)
                Text("20kHz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = NeonCyan)
            }
        }
    }
}

@Composable
private fun SpectrumLegendPill(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = TextMuted
        )
    }
}
