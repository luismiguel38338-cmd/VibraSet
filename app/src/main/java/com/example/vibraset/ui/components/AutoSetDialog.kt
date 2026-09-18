package com.example.vibraset.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.vibraset.model.AutoSetTarget

@Composable
fun AutoSetDialog(
    isCalibrating: Boolean,
    stepMessage: String,
    onTargetSelected: (AutoSetTarget) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isCalibrating) onDismiss() }) {
        Card(
            modifier = Modifier
                .testTag("auto_set_dialog")
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RackSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "SETEO AUTOMÁTICO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextBright
                        )
                    }

                    if (!isCalibrating) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }
                }

                Text(
                    text = "Selecciona el resultado acústico que buscas. VibraSet generará automáticamente la curva y dinámica recomendada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Calibration In-Progress Screen
                AnimatedVisibility(visible = isCalibrating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D15))
                            .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = stepMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NeonCyan
                            )
                            Text(
                                text = "Calibrando DSP y ecualizador...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Targets List (visible when not calibrating)
                if (!isCalibrating) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoSetTarget.values().forEach { target ->
                            AutoSetOptionCard(
                                target = target,
                                onClick = { onTargetSelected(target) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoSetOptionCard(
    target: AutoSetTarget,
    onClick: () -> Unit
) {
    val accentColor = when (target) {
        AutoSetTarget.GRAVES_PROFUNDOS -> NeonRed
        AutoSetTarget.GRAVES_FUERTES -> NeonAmber
        AutoSetTarget.VOCES_CLARAS -> NeonGreen
        AutoSetTarget.SONIDO_EQUILIBRADO -> NeonCyan
        AutoSetTarget.MAS_VOLUMEN -> NeonYellow
        AutoSetTarget.SONIDO_VEHICULO -> ElectricBlue
        AutoSetTarget.SONIDO_MINI_COMPONENTE -> Color(0xFFC084FC)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RackSurfaceVariant)
            .border(1.dp, BezelBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("auto_set_option_${target.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(accentColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextBright
                    )
                    Text(
                        text = target.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = target.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
