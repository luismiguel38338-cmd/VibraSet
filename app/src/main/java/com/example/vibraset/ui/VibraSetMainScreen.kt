package com.example.vibraset.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.vibraset.model.SoundPreset
import com.example.vibraset.model.SystemProfile
import com.example.vibraset.ui.components.*

@Composable
fun VibraSetMainScreen(
    viewModel: VibraSetViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val spectrumData by viewModel.spectrumData.collectAsState()
    val leftPeak by viewModel.leftPeak.collectAsState()
    val rightPeak by viewModel.rightPeak.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("vibraset_main_screen"),
        containerColor = CarbonDark,
        topBar = {
            ConsoleTopBar(
                currentPresetName = uiState.currentPreset.name,
                systemProfile = uiState.systemProfile,
                isHardwareAttached = uiState.isHardwareEqAttached,
                onOpenAutoSet = { viewModel.setShowAutoSetDialog(true) },
                onOpenSavePreset = { viewModel.setShowSavePresetDialog(true) },
                onOpenSystemProfile = { viewModel.setShowSystemProfileDialog(true) }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 840.dp

            if (isWideScreen) {
                // Dual Column Pro Console Layout for Tablets and Large Screens
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Spectrum Analyzer, VU Meters, Sound Controls Rack
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PresetSelectorStrip(
                            presets = uiState.presets,
                            currentPresetId = uiState.currentPreset.id,
                            onPresetSelected = { viewModel.selectPreset(it) },
                            onDeletePreset = { viewModel.deleteUserPreset(it) }
                        )

                        SpectrumAnalyzerView(
                            spectrumData = spectrumData,
                            isPlaying = uiState.currentTrack.isPlaying
                        )

                        VuMeterView(leftPeak = leftPeak, rightPeak = rightPeak)

                        SoundControlKnobsView(
                            bass = uiState.bass,
                            mid = uiState.mid,
                            treble = uiState.treble,
                            bassBoost = uiState.bassBoost,
                            loudness = uiState.loudness,
                            subwooferLevel = uiState.subwooferLevel,
                            subwooferCrossoverHz = uiState.subwooferCrossoverHz,
                            gain = uiState.gain,
                            balance = uiState.balance,
                            fader = uiState.fader,
                            volume = uiState.volume,
                            onBassChanged = { viewModel.setBass(it) },
                            onMidChanged = { viewModel.setMid(it) },
                            onTrebleChanged = { viewModel.setTreble(it) },
                            onBassBoostChanged = { viewModel.setBassBoost(it) },
                            onLoudnessChanged = { viewModel.setLoudness(it) },
                            onSubwooferChanged = { lvl, cross -> viewModel.setSubwoofer(lvl, cross) },
                            onGainChanged = { viewModel.setGain(it) },
                            onBalanceChanged = { viewModel.setBalance(it) },
                            onFaderChanged = { viewModel.setFader(it) },
                            onVolumeChanged = { viewModel.setVolume(it) }
                        )
                    }

                    // Right Column: Graphic Equalizer & Music Player
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EqualizerSlidersView(
                            bandMode = uiState.bandMode,
                            bands = uiState.eqBands,
                            isEqEnabled = uiState.isEqEnabled,
                            onBandModeSelected = { viewModel.setBandMode(it) },
                            onBandGainChanged = { idx, g -> viewModel.onBandGainChanged(idx, g) },
                            onToggleEq = { viewModel.toggleEq() },
                            onResetFlat = { viewModel.resetEqFlat() }
                        )

                        MusicPlayerConsole(
                            trackInfo = uiState.currentTrack,
                            onPlayPause = { viewModel.playPause() },
                            onNext = { viewModel.nextTrack() },
                            onPrevious = { viewModel.previousTrack() },
                            onSeek = { viewModel.seekTo(it) },
                            onSelectAudioFile = { viewModel.loadCustomAudio(it) },
                            onSelectDemoTrack = { viewModel.selectDemoTrack(it) }
                        )
                    }
                }
            } else {
                // Mobile Vertical Scroll Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Preset Chips Carousel
                    PresetSelectorStrip(
                        presets = uiState.presets,
                        currentPresetId = uiState.currentPreset.id,
                        onPresetSelected = { viewModel.selectPreset(it) },
                        onDeletePreset = { viewModel.deleteUserPreset(it) }
                    )

                    // Spectrum Analyzer (Visualizer)
                    SpectrumAnalyzerView(
                        spectrumData = spectrumData,
                        isPlaying = uiState.currentTrack.isPlaying
                    )

                    // Dual VU Meter (L & R Peak Levels)
                    VuMeterView(leftPeak = leftPeak, rightPeak = rightPeak)

                    // Graphic Equalizer (10, 15, 31 Bands)
                    EqualizerSlidersView(
                        bandMode = uiState.bandMode,
                        bands = uiState.eqBands,
                        isEqEnabled = uiState.isEqEnabled,
                        onBandModeSelected = { viewModel.setBandMode(it) },
                        onBandGainChanged = { idx, g -> viewModel.onBandGainChanged(idx, g) },
                        onToggleEq = { viewModel.toggleEq() },
                        onResetFlat = { viewModel.resetEqFlat() }
                    )

                    // Sound Controls Rack (Bass, Mid, Treble, Subwoofer, Loudness, Volume, Balance, Fader)
                    SoundControlKnobsView(
                        bass = uiState.bass,
                        mid = uiState.mid,
                        treble = uiState.treble,
                        bassBoost = uiState.bassBoost,
                        loudness = uiState.loudness,
                        subwooferLevel = uiState.subwooferLevel,
                        subwooferCrossoverHz = uiState.subwooferCrossoverHz,
                        gain = uiState.gain,
                        balance = uiState.balance,
                        fader = uiState.fader,
                        volume = uiState.volume,
                        onBassChanged = { viewModel.setBass(it) },
                        onMidChanged = { viewModel.setMid(it) },
                        onTrebleChanged = { viewModel.setTreble(it) },
                        onBassBoostChanged = { viewModel.setBassBoost(it) },
                        onLoudnessChanged = { viewModel.setLoudness(it) },
                        onSubwooferChanged = { lvl, cross -> viewModel.setSubwoofer(lvl, cross) },
                        onGainChanged = { viewModel.setGain(it) },
                        onBalanceChanged = { viewModel.setBalance(it) },
                        onFaderChanged = { viewModel.setFader(it) },
                        onVolumeChanged = { viewModel.setVolume(it) }
                    )

                    // Music Player Console Deck
                    MusicPlayerConsole(
                        trackInfo = uiState.currentTrack,
                        onPlayPause = { viewModel.playPause() },
                        onNext = { viewModel.nextTrack() },
                        onPrevious = { viewModel.previousTrack() },
                        onSeek = { viewModel.seekTo(it) },
                        onSelectAudioFile = { viewModel.loadCustomAudio(it) },
                        onSelectDemoTrack = { viewModel.selectDemoTrack(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Notification / Status Banner
            AnimatedVisibility(
                visible = uiState.statusBanner != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                uiState.statusBanner?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xE6161F2E))
                            .border(1.dp, NeonCyan, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.showAutoSetDialog) {
        AutoSetDialog(
            isCalibrating = uiState.isAutoSetting,
            stepMessage = uiState.autoSetStepMessage,
            onTargetSelected = { viewModel.executeAutoSet(it) },
            onDismiss = { viewModel.setShowAutoSetDialog(false) }
        )
    }

    if (uiState.showSavePresetDialog) {
        SavePresetDialog(
            initialName = uiState.currentPreset.name,
            onSave = { viewModel.saveCustomPreset(it) },
            onDismiss = { viewModel.setShowSavePresetDialog(false) }
        )
    }

    if (uiState.showSystemProfileDialog) {
        SystemProfileDialog(
            currentProfile = uiState.systemProfile,
            onProfileSelected = { viewModel.setSystemProfile(it) },
            onDismiss = { viewModel.setShowSystemProfileDialog(false) }
        )
    }
}

@Composable
private fun ConsoleTopBar(
    currentPresetName: String,
    systemProfile: SystemProfile,
    isHardwareAttached: Boolean,
    onOpenAutoSet: () -> Unit,
    onOpenSavePreset: () -> Unit,
    onOpenSystemProfile: () -> Unit
) {
    Surface(
        color = RackSurface,
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BezelBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Console Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.vibraset_logo),
                        contentDescription = "VibraSet Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VIBRA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextBright
                            )
                            Text(
                                text = "SET",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NeonCyan
                            )
                        }
                        Text(
                            text = "DSP AUDIO CONSOLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextDim
                        )
                    }
                }

                // Quick Action Buttons: Auto-Set, Save, System Profile
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto-Set Button (Glowing Action)
                    Button(
                        onClick = onOpenAutoSet,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = NeonCyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("auto_set_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Seteo Automático",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Auto-Set",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    // Save Preset Button
                    IconButton(
                        onClick = onOpenSavePreset,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RackSurfaceVariant)
                            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
                            .testTag("save_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Guardar Preset",
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // System Profile Button
                    IconButton(
                        onClick = onOpenSystemProfile,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RackSurfaceVariant)
                            .border(1.dp, BezelBorder, RoundedCornerShape(8.dp))
                            .testTag("system_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = "Perfil de Sistema",
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-status line: Current Preset Badge & System Profile Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "PRESET:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextDim
                    )
                    Text(
                        text = currentPresetName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = NeonAmber
                    )
                }

                // Profile badge
                Row(
                    modifier = Modifier.clickable { onOpenSystemProfile() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue)
                    )
                    Text(
                        text = systemProfile.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetSelectorStrip(
    presets: List<SoundPreset>,
    currentPresetId: String,
    onPresetSelected: (SoundPreset) -> Unit,
    onDeletePreset: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preset_selector_strip"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(presets, key = { it.id }) { preset ->
            val isSelected = preset.id == currentPresetId
            Surface(
                onClick = { onPresetSelected(preset) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) NeonAmber else RackSurfaceVariant,
                contentColor = if (isSelected) CarbonDark else TextMuted,
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) NeonAmber else BezelBorder)
                ),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("preset_chip_${preset.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    if (preset.isUserCreated) {
                        IconButton(
                            onClick = { onDeletePreset(preset.id) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar preset",
                                tint = if (isSelected) CarbonDark else TextDim,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
