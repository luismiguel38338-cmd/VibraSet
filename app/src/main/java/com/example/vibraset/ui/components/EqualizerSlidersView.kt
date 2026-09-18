package com.example.vibraset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.vibraset.model.BandMode
import com.example.vibraset.model.EqBand
import com.example.vibraset.model.FrequencyRange

@Composable
fun EqualizerSlidersView(
    bandMode: BandMode,
    bands: List<EqBand>,
    isEqEnabled: Boolean,
    onBandModeSelected: (BandMode) -> Unit,
    onBandGainChanged: (Int, Float) -> Unit,
    onToggleEq: () -> Unit,
    onResetFlat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .testTag("equalizer_rack_card")
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RackSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BezelBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Bar: Band Selector (10, 15, 31), Power/Bypass Button, Flat Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Band Mode Selector Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BandMode.values().forEach { mode ->
                        val selected = mode == bandMode
                        FilterChip(
                            selected = selected,
                            onClick = { onBandModeSelected(mode) },
                            label = {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.5.sp
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = CarbonDark,
                                containerColor = RackSurfaceVariant,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) NeonCyan else BezelBorder
                            ),
                            modifier = Modifier.testTag("band_mode_chip_${mode.bandCount}")
                        )
                    }
                }

                // Action Controls: Power (Bypass) and Reset (Flat)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Flat
                    FilledTonalButton(
                        onClick = onResetFlat,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RackSurfaceVariant,
                            contentColor = TextBright
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .testTag("reset_flat_button")
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer plano",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Plano",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // EQ Bypass / On Toggle
                    Button(
                        onClick = onToggleEq,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEqEnabled) NeonCyan else Color(0xFF26334D),
                            contentColor = if (isEqEnabled) CarbonDark else TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .testTag("eq_bypass_button")
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = if (isEqEnabled) "EQ Activado" else "EQ Bypass",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isEqEnabled) "EQ ON" else "BYPASS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Sliders Rack Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF090D15))
                    .border(1.dp, BezelBorder, RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 6.dp)
            ) {
                // Horizontal scrollable bank of faders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(if (bandMode == BandMode.BANDS_31) 4.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bands.forEach { band ->
                        SingleBandFader(
                            band = band,
                            isEnabled = isEqEnabled,
                            isCompact = bandMode == BandMode.BANDS_31,
                            onGainChanged = { gain -> onBandGainChanged(band.index, gain) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleBandFader(
    band: EqBand,
    isEnabled: Boolean,
    isCompact: Boolean,
    onGainChanged: (Float) -> Unit
) {
    val categoryColor = when (band.rangeCategory) {
        FrequencyRange.SUBGRAVES -> NeonRed
        FrequencyRange.GRAVES -> NeonAmber
        FrequencyRange.MEDIOS -> NeonGreen
        FrequencyRange.AGUDOS -> NeonCyan
    }

    val faderWidth = if (isCompact) 44.dp else 56.dp

    Column(
        modifier = Modifier
            .width(faderWidth)
            .testTag("fader_band_${band.index}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain dB readout at top
        val gainText = if (band.gainDb > 0) "+${"%.1f".format(band.gainDb)}" else "${"%.1f".format(band.gainDb)}"
        Text(
            text = gainText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (isCompact) 9.sp else 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = if (isEnabled) {
                if (band.gainDb != 0f) categoryColor else TextMuted
            } else TextDim,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Vertical Slider Control using custom Box + Slider layout
        Box(
            modifier = Modifier
                .height(160.dp)
                .width(faderWidth),
            contentAlignment = Alignment.Center
        ) {
            // Vertical Slot Background (Fader travel line)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF161F2E))
            )

            // Center 0 dB Detent line
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(Color(0x66FFFFFF))
            )

            // +6dB and -6dB tick marks
            Box(
                modifier = Modifier
                    .offset(y = (-35).dp)
                    .width(8.dp)
                    .height(1.dp)
                    .background(Color(0x33FFFFFF))
            )
            Box(
                modifier = Modifier
                    .offset(y = 35.dp)
                    .width(8.dp)
                    .height(1.dp)
                    .background(Color(0x33FFFFFF))
            )

            // Rotated Compose Slider for vertical touch travel
            // Sliders in Compose are horizontal by default, we can rotate with graphicsLayer or custom vertical drag.
            // Using custom touch drag is much more reliable and responsive!
            VerticalFaderKnob(
                value = band.gainDb,
                min = -12f,
                max = 12f,
                isEnabled = isEnabled,
                accentColor = categoryColor,
                onValueChanged = onGainChanged,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency label at bottom with color dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) categoryColor else TextDim)
            )
            Text(
                text = band.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isCompact) 8.5.sp else 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (isEnabled) TextBright else TextDim,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VerticalFaderKnob(
    value: Float,
    min: Float,
    max: Float,
    isEnabled: Boolean,
    accentColor: Color,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Fraction: 0.0 at bottom (min), 1.0 at top (max)
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(isEnabled) {
                if (!isEnabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val totalHeight = size.height.toFloat()
                    val touchY = change.position.y.coerceIn(0f, totalHeight)
                    val newFraction = 1f - (touchY / totalHeight)
                    val newValue = min + newFraction * (max - min)
                    onValueChanged(newValue)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight - 26.dp
        // Inverted: top is max (+12dB), bottom is min (-12dB)
        val yOffset = (availableHeight * (0.5f - fraction))

        // Fader Metallic Handle (Cap)
        Box(
            modifier = Modifier
                .offset(y = yOffset)
                .width(32.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isEnabled) Color(0xFF222D42) else Color(0xFF161F2E)
                )
                .border(
                    1.dp,
                    if (isEnabled) Color(0xFF3F4F6E) else Color(0xFF1F293D),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal metallic grip lines and glowing center indicator line
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(1.dp)
                        .background(Color(0x44FFFFFF))
                )
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isEnabled) accentColor else TextDim)
                )
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(1.dp)
                        .background(Color(0x44FFFFFF))
                )
            }
        }
    }
}
