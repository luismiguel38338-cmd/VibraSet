package com.example.vibraset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SoundControlKnobsView(
    bass: Float,
    mid: Float,
    treble: Float,
    bassBoost: Float,
    loudness: Float,
    subwooferLevel: Float,
    subwooferCrossoverHz: Int,
    gain: Float,
    balance: Float,
    fader: Float,
    volume: Float,
    onBassChanged: (Float) -> Unit,
    onMidChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
    onBassBoostChanged: (Float) -> Unit,
    onLoudnessChanged: (Float) -> Unit,
    onSubwooferChanged: (Float, Int) -> Unit,
    onGainChanged: (Float) -> Unit,
    onBalanceChanged: (Float) -> Unit,
    onFaderChanged: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("sound_controls_section")
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Module 1: Tone Stack (Bass, Mid, Treble) & Output Dynamics (Bass Boost, Loudness)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RackSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BezelBorder))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionHeader(title = "CONTROLES DE TONO Y DINÁMICA", icon = Icons.Default.GraphicEq)

                Spacer(modifier = Modifier.height(8.dp))

                // Row with 3 primary tone sliders: Bass, Mid, Treble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ToneFaderItem(
                        title = "BASS",
                        value = bass,
                        unit = "dB",
                        min = -10f,
                        max = 10f,
                        accentColor = NeonAmber,
                        onValueChanged = onBassChanged,
                        modifier = Modifier.weight(1f)
                    )
                    ToneFaderItem(
                        title = "MID",
                        value = mid,
                        unit = "dB",
                        min = -10f,
                        max = 10f,
                        accentColor = NeonGreen,
                        onValueChanged = onMidChanged,
                        modifier = Modifier.weight(1f)
                    )
                    ToneFaderItem(
                        title = "TREBLE",
                        value = treble,
                        unit = "dB",
                        min = -10f,
                        max = 10f,
                        accentColor = NeonCyan,
                        onValueChanged = onTrebleChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bass Boost & Loudness Enhancer Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PercentSliderItem(
                        title = "BASS BOOST",
                        subtitle = "Mega Sub Impact",
                        value = bassBoost,
                        accentColor = NeonRed,
                        icon = Icons.Default.SurroundSound,
                        onValueChanged = onBassBoostChanged,
                        modifier = Modifier.weight(1f)
                    )
                    PercentSliderItem(
                        title = "LOUDNESS",
                        subtitle = "Dynamic Range Comp",
                        value = loudness,
                        accentColor = NeonYellow,
                        icon = Icons.Default.Equalizer,
                        onValueChanged = onLoudnessChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Module 2: Subwoofer Management, Staging (Balance / Fader) & Master Volume
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RackSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BezelBorder))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionHeader(title = "SUBWOOFER, STAGING Y SALIDA", icon = Icons.Default.Speaker)

                Spacer(modifier = Modifier.height(10.dp))

                // Subwoofer Level + Crossover selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SUBWOOFER LEVEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NeonRed
                            )
                            Text(
                                "${subwooferLevel.toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NeonRed
                            )
                        }

                        Slider(
                            value = subwooferLevel,
                            onValueChange = { onSubwooferChanged(it, subwooferCrossoverHz) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonRed,
                                activeTrackColor = NeonRed,
                                inactiveTrackColor = Color(0xFF26334D)
                            ),
                            modifier = Modifier.testTag("subwoofer_level_slider")
                        )
                    }

                    // Crossover selector (50Hz, 80Hz, 120Hz)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            "CROSSOVER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(50, 80, 120).forEach { freq ->
                                val isSel = freq == subwooferCrossoverHz
                                FilterChip(
                                    selected = isSel,
                                    onClick = { onSubwooferChanged(subwooferLevel, freq) },
                                    label = {
                                        Text(
                                            "${freq}Hz",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonRed,
                                        selectedLabelColor = CarbonDark,
                                        containerColor = RackSurfaceVariant,
                                        labelColor = TextMuted
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSel,
                                        borderColor = if (isSel) NeonRed else BezelBorder
                                    ),
                                    modifier = Modifier.testTag("crossover_chip_$freq")
                                )
                            }
                        }
                    }
                }

                Divider(color = BezelBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Balance, Fader & Gain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CenteredPanSlider(
                        title = "BALANCE",
                        leftLabel = "L",
                        rightLabel = "R",
                        value = balance,
                        onValueChanged = onBalanceChanged,
                        modifier = Modifier.weight(1f)
                    )
                    CenteredPanSlider(
                        title = "FADER",
                        leftLabel = "FRONT",
                        rightLabel = "REAR",
                        value = fader,
                        onValueChanged = onFaderChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gain & Master Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gain (-12dB to +12dB)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "GAIN (PRE-AMP)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextMuted
                            )
                            Text(
                                if (gain > 0) "+${"%.1f".format(gain)} dB" else "${"%.1f".format(gain)} dB",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                color = NeonYellow
                            )
                        }
                        Slider(
                            value = gain,
                            onValueChange = onGainChanged,
                            valueRange = -12f..12f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonYellow,
                                activeTrackColor = NeonYellow,
                                inactiveTrackColor = Color(0xFF26334D)
                            ),
                            modifier = Modifier.testTag("gain_slider")
                        )
                    }

                    // Master Volume (0 to 100%)
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "MASTER VOLUME",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = NeonCyan
                            )
                            Text(
                                "${volume.toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NeonCyan
                            )
                        }
                        Slider(
                            value = volume,
                            onValueChange = onVolumeChanged,
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF26334D)
                            ),
                            modifier = Modifier.testTag("master_volume_slider")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = TextMuted
        )
    }
}

@Composable
private fun ToneFaderItem(
    title: String,
    value: Float,
    unit: String,
    min: Float,
    max: Float,
    accentColor: Color,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RackSurfaceVariant)
            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = accentColor
                )
                Text(
                    text = if (value > 0) "+${"%.1f".format(value)} $unit" else "${"%.1f".format(value)} $unit",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextBright
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChanged,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF26334D)
                ),
                modifier = Modifier.testTag("tone_${title.lowercase()}_slider")
            )
        }
    }
}

@Composable
private fun PercentSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    accentColor: Color,
    icon: ImageVector,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RackSurfaceVariant)
            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = accentColor
                    )
                }
                Text(
                    text = "${value.toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = TextBright
                )
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.5.sp),
                color = TextDim
            )

            Slider(
                value = value,
                onValueChange = onValueChanged,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF26334D)
                ),
                modifier = Modifier.testTag("${title.lowercase().replace(" ", "_")}_slider")
            )
        }
    }
}

@Composable
private fun CenteredPanSlider(
    title: String,
    leftLabel: String,
    rightLabel: String,
    value: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RackSurfaceVariant)
            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                val statusText = when {
                    value < -5 -> "$leftLabel ${(-value).toInt()}"
                    value > 5 -> "$rightLabel ${value.toInt()}"
                    else -> "CENTER"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = NeonCyan
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChanged,
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = Color(0xFF26334D)
                ),
                modifier = Modifier.testTag("pan_${title.lowercase()}_slider")
            )
        }
    }
}
