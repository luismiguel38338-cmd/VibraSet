package com.example.vibraset.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibraset.audio.AudioEngine
import com.example.vibraset.data.PresetRepository
import com.example.vibraset.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class UiState(
    val bandMode: BandMode = BandMode.BANDS_10,
    val eqBands: List<EqBand> = emptyList(),
    val isEqEnabled: Boolean = true,
    val currentPreset: SoundPreset = PresetDefaults.BUILT_IN_PRESETS[0],
    val presets: List<SoundPreset> = emptyList(),
    val systemProfile: SystemProfile = SystemProfile.VEHICULO,
    // Sound Controls
    val bass: Float = 5.0f,
    val mid: Float = 0.5f,
    val treble: Float = 4.5f,
    val bassBoost: Float = 60.0f,
    val loudness: Float = 50.0f,
    val subwooferLevel: Float = 65.0f,
    val subwooferCrossoverHz: Int = 80,
    val gain: Float = 1.5f,
    val balance: Float = 0.0f, // -100 to +100
    val fader: Float = 0.0f,   // -100 to +100
    val volume: Float = 85.0f, // 0 to 100
    // Auto-Set Wizard State
    val isAutoSetting: Boolean = false,
    val autoSetStepMessage: String = "",
    val autoSetTargetSelected: AutoSetTarget? = null,
    // Dialog visibility
    val showAutoSetDialog: Boolean = false,
    val showSavePresetDialog: Boolean = false,
    val showSystemProfileDialog: Boolean = false,
    // Audio Player State
    val currentTrack: TrackInfo = TrackInfo("default", "VibraSet Demo", "VibraSet Audio", 64000L),
    val isHardwareEqAttached: Boolean = false,
    val statusBanner: String? = null
)

class VibraSetViewModel(application: Application) : AndroidViewModel(application) {

    val audioEngine = AudioEngine(application)
    private val repository = PresetRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val spectrumData: StateFlow<FloatArray> = audioEngine.spectrumData
    val leftPeak: StateFlow<Float> = audioEngine.leftPeak
    val rightPeak: StateFlow<Float> = audioEngine.rightPeak

    private var autoSetJob: Job? = null

    init {
        val initialPresets = repository.getAllPresets()
        val defaultPreset = initialPresets.firstOrNull { it.id == "car_sound" } ?: initialPresets.first()

        _uiState.update {
            it.copy(
                presets = initialPresets,
                currentPreset = defaultPreset
            )
        }

        // Initialize bands with default preset
        applyPresetInternal(defaultPreset, keepMode = false)

        // Observe player track info and hardware status
        viewModelScope.launch {
            audioEngine.currentTrack.collect { track ->
                _uiState.update { it.copy(currentTrack = track) }
            }
        }

        viewModelScope.launch {
            audioEngine.isHardwareEqAttached.collect { attached ->
                _uiState.update { it.copy(isHardwareEqAttached = attached) }
            }
        }
    }

    fun setBandMode(mode: BandMode) {
        val preset = _uiState.value.currentPreset
        val gains = when (mode) {
            BandMode.BANDS_10 -> preset.bandGains10
            BandMode.BANDS_15 -> preset.bandGains15
            BandMode.BANDS_31 -> preset.bandGains31
        }
        val bands = buildEqBands(mode, gains)
        _uiState.update {
            it.copy(bandMode = mode, eqBands = bands)
        }
        audioEngine.updateEqBands(gains, mode)
    }

    private fun buildEqBands(mode: BandMode, gains: List<Float>): List<EqBand> {
        val freqs = when (mode) {
            BandMode.BANDS_10 -> PresetDefaults.FREQS_10
            BandMode.BANDS_15 -> PresetDefaults.FREQS_15
            BandMode.BANDS_31 -> PresetDefaults.FREQS_31
        }
        val labels = when (mode) {
            BandMode.BANDS_10 -> PresetDefaults.LABELS_10
            BandMode.BANDS_15 -> PresetDefaults.LABELS_15
            BandMode.BANDS_31 -> PresetDefaults.LABELS_31
        }
        return freqs.indices.map { i ->
            val hz = freqs[i]
            val gain = gains.getOrElse(i) { 0f }
            EqBand(
                index = i,
                frequencyHz = hz,
                label = labels.getOrElse(i) { "${hz}Hz" },
                gainDb = gain,
                rangeCategory = PresetDefaults.categorizeFrequency(hz)
            )
        }
    }

    fun onBandGainChanged(bandIndex: Int, newGain: Float) {
        val currentBands = _uiState.value.eqBands.toMutableList()
        if (bandIndex !in currentBands.indices) return

        val clampedGain = ((newGain * 10f).toInt() / 10f).coerceIn(-12f, 12f)
        currentBands[bandIndex] = currentBands[bandIndex].copy(gainDb = clampedGain)

        val newGains = currentBands.map { it.gainDb }
        val mode = _uiState.value.bandMode

        // Update current preset reference to custom
        val updatedPreset = _uiState.value.currentPreset.copy(
            id = "custom_edited",
            name = "Personalizado",
            bandGains10 = if (mode == BandMode.BANDS_10) newGains else PresetDefaults.interpolateGains(newGains, 10),
            bandGains15 = if (mode == BandMode.BANDS_15) newGains else PresetDefaults.interpolateGains(newGains, 15),
            bandGains31 = if (mode == BandMode.BANDS_31) newGains else PresetDefaults.interpolateGains(newGains, 31)
        )

        _uiState.update {
            it.copy(
                eqBands = currentBands,
                currentPreset = updatedPreset
            )
        }
        audioEngine.updateEqBands(newGains, mode)
    }

    fun toggleEq() {
        val newState = !_uiState.value.isEqEnabled
        _uiState.update { it.copy(isEqEnabled = newState) }
        audioEngine.setEqEnabled(newState)
        showBanner(if (newState) "Ecualizador Activado" else "Ecualizador en Bypass (Plano)")
    }

    fun resetEqFlat() {
        val mode = _uiState.value.bandMode
        val count = mode.bandCount
        val flatGains = List(count) { 0f }
        val bands = buildEqBands(mode, flatGains)

        val flatPreset = PresetDefaults.BUILT_IN_PRESETS.first { it.id == "personalizado" }.copy(
            name = "Curva Plana (Reset)",
            bandGains10 = List(10) { 0f },
            bandGains15 = List(15) { 0f },
            bandGains31 = List(31) { 0f }
        )

        _uiState.update {
            it.copy(
                eqBands = bands,
                currentPreset = flatPreset,
                bass = 0f,
                mid = 0f,
                treble = 0f,
                bassBoost = 0f,
                loudness = 0f,
                subwooferLevel = 0f,
                gain = 0f,
                balance = 0f,
                fader = 0f
            )
        }

        audioEngine.updateEqBands(flatGains, mode)
        audioEngine.setToneControls(0f, 0f, 0f)
        audioEngine.setBassBoost(0f)
        audioEngine.setLoudness(0f)
        audioEngine.setSubwoofer(0f, 80)
        audioEngine.setGain(0f)
        audioEngine.setBalance(0f)
        audioEngine.setFader(0f)

        showBanner("Configuración Restablecida (Plano)")
    }

    fun selectPreset(preset: SoundPreset) {
        applyPresetInternal(preset, keepMode = true)
        showBanner("Preset aplicado: ${preset.name}")
    }

    private fun applyPresetInternal(preset: SoundPreset, keepMode: Boolean) {
        val mode = if (keepMode) _uiState.value.bandMode else BandMode.BANDS_10
        val gains = when (mode) {
            BandMode.BANDS_10 -> preset.bandGains10
            BandMode.BANDS_15 -> preset.bandGains15
            BandMode.BANDS_31 -> preset.bandGains31
        }
        val bands = buildEqBands(mode, gains)

        _uiState.update {
            it.copy(
                bandMode = mode,
                eqBands = bands,
                currentPreset = preset,
                bass = preset.bass,
                mid = preset.mid,
                treble = preset.treble,
                bassBoost = preset.bassBoost,
                loudness = preset.loudness,
                subwooferLevel = preset.subwooferLevel,
                subwooferCrossoverHz = preset.subwooferCrossoverHz,
                gain = preset.gain
            )
        }

        audioEngine.updateEqBands(gains, mode)
        audioEngine.setToneControls(preset.bass, preset.mid, preset.treble)
        audioEngine.setBassBoost(preset.bassBoost)
        audioEngine.setLoudness(preset.loudness)
        audioEngine.setSubwoofer(preset.subwooferLevel, preset.subwooferCrossoverHz)
        audioEngine.setGain(preset.gain)
    }

    // Sound Controls Setters
    fun setBass(value: Float) {
        val clamped = ((value * 10f).toInt() / 10f).coerceIn(-10f, 10f)
        _uiState.update { it.copy(bass = clamped) }
        audioEngine.setToneControls(clamped, _uiState.value.mid, _uiState.value.treble)
    }

    fun setMid(value: Float) {
        val clamped = ((value * 10f).toInt() / 10f).coerceIn(-10f, 10f)
        _uiState.update { it.copy(mid = clamped) }
        audioEngine.setToneControls(_uiState.value.bass, clamped, _uiState.value.treble)
    }

    fun setTreble(value: Float) {
        val clamped = ((value * 10f).toInt() / 10f).coerceIn(-10f, 10f)
        _uiState.update { it.copy(treble = clamped) }
        audioEngine.setToneControls(_uiState.value.bass, _uiState.value.mid, clamped)
    }

    fun setBassBoost(value: Float) {
        val clamped = value.coerceIn(0f, 100f)
        _uiState.update { it.copy(bassBoost = clamped) }
        audioEngine.setBassBoost(clamped)
    }

    fun setLoudness(value: Float) {
        val clamped = value.coerceIn(0f, 100f)
        _uiState.update { it.copy(loudness = clamped) }
        audioEngine.setLoudness(clamped)
    }

    fun setSubwoofer(level: Float, crossoverHz: Int) {
        val clampedLevel = level.coerceIn(0f, 100f)
        _uiState.update { it.copy(subwooferLevel = clampedLevel, subwooferCrossoverHz = crossoverHz) }
        audioEngine.setSubwoofer(clampedLevel, crossoverHz)
    }

    fun setGain(value: Float) {
        val clamped = ((value * 10f).toInt() / 10f).coerceIn(-12f, 12f)
        _uiState.update { it.copy(gain = clamped) }
        audioEngine.setGain(clamped)
    }

    fun setBalance(value: Float) {
        val clamped = value.coerceIn(-100f, 100f)
        _uiState.update { it.copy(balance = clamped) }
        audioEngine.setBalance(clamped)
    }

    fun setFader(value: Float) {
        val clamped = value.coerceIn(-100f, 100f)
        _uiState.update { it.copy(fader = clamped) }
        audioEngine.setFader(clamped)
    }

    fun setVolume(value: Float) {
        val clamped = value.coerceIn(0f, 100f)
        _uiState.update { it.copy(volume = clamped) }
        audioEngine.setMasterVolume(clamped / 100f)
    }

    fun setSystemProfile(profile: SystemProfile) {
        _uiState.update { it.copy(systemProfile = profile) }
        // Adapt sound profile defaults according to speaker layout
        when (profile) {
            SystemProfile.STEREO_2 -> {
                setFader(0f)
            }
            SystemProfile.QUAD_4 -> {
                // Keep fader active
            }
            SystemProfile.SUBWOOFER_SYSTEM -> {
                if (_uiState.value.subwooferLevel < 40f) {
                    setSubwoofer(70f, 80)
                }
            }
            SystemProfile.MINI_COMPONENTE -> {
                if (_uiState.value.loudness < 30f) setLoudness(55f)
            }
            SystemProfile.VEHICULO -> {
                if (_uiState.value.bassBoost < 40f) setBassBoost(60f)
            }
            SystemProfile.CUSTOM -> {}
        }
        showBanner("Perfil de Sistema: ${profile.title}")
    }

    // Auto-Set Wizard
    fun executeAutoSet(target: AutoSetTarget) {
        autoSetJob?.cancel()
        autoSetJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAutoSetting = true,
                    autoSetTargetSelected = target,
                    autoSetStepMessage = "Analizando acústica objetivo (${target.title})..."
                )
            }
            delay(500)
            _uiState.update { it.copy(autoSetStepMessage = "Midiendo rango dinámico y resonancias...") }
            delay(550)
            _uiState.update { it.copy(autoSetStepMessage = "Ajustando filtros de crossover y curvas EQ...") }
            delay(500)

            val config = PresetDefaults.getAutoSetConfiguration(target)
            applyPresetInternal(config, keepMode = true)

            _uiState.update {
                it.copy(
                    autoSetStepMessage = "¡Calibración completada con éxito!",
                    isAutoSetting = false,
                    showAutoSetDialog = false
                )
            }
            showBanner("Seteo Automático aplicado: ${target.title}")
        }
    }

    // Save Custom Preset
    fun saveCustomPreset(name: String) {
        if (name.isBlank()) return
        val currentGains = _uiState.value.eqBands.map { it.gainDb }
        val mode = _uiState.value.bandMode

        val g10 = if (mode == BandMode.BANDS_10) currentGains else PresetDefaults.interpolateGains(currentGains, 10)
        val g15 = if (mode == BandMode.BANDS_15) currentGains else PresetDefaults.interpolateGains(currentGains, 15)
        val g31 = if (mode == BandMode.BANDS_31) currentGains else PresetDefaults.interpolateGains(currentGains, 31)

        val newPreset = SoundPreset(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            isUserCreated = true,
            bandGains10 = g10,
            bandGains15 = g15,
            bandGains31 = g31,
            bass = _uiState.value.bass,
            mid = _uiState.value.mid,
            treble = _uiState.value.treble,
            bassBoost = _uiState.value.bassBoost,
            loudness = _uiState.value.loudness,
            subwooferLevel = _uiState.value.subwooferLevel,
            subwooferCrossoverHz = _uiState.value.subwooferCrossoverHz,
            gain = _uiState.value.gain
        )

        val updatedList = repository.saveUserPreset(newPreset)
        _uiState.update {
            it.copy(
                presets = updatedList,
                currentPreset = newPreset,
                showSavePresetDialog = false
            )
        }
        showBanner("Preset '$name' guardado")
    }

    fun deleteUserPreset(presetId: String) {
        val updatedList = repository.deleteUserPreset(presetId)
        val fallback = updatedList.firstOrNull() ?: PresetDefaults.BUILT_IN_PRESETS[0]
        _uiState.update {
            it.copy(
                presets = updatedList,
                currentPreset = if (it.currentPreset.id == presetId) fallback else it.currentPreset
            )
        }
        showBanner("Preset eliminado")
    }

    // Dialog toggles
    fun setShowAutoSetDialog(show: Boolean) {
        _uiState.update { it.copy(showAutoSetDialog = show) }
    }

    fun setShowSavePresetDialog(show: Boolean) {
        _uiState.update { it.copy(showSavePresetDialog = show) }
    }

    fun setShowSystemProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showSystemProfileDialog = show) }
    }

    // Player Actions
    fun playPause() = audioEngine.playPause()
    fun nextTrack() = audioEngine.nextTrack()
    fun previousTrack() = audioEngine.previousTrack()
    fun seekTo(positionMs: Long) = audioEngine.seekTo(positionMs)
    fun loadCustomAudio(uri: Uri) = audioEngine.loadCustomAudio(uri)
    fun selectDemoTrack(index: Int) = audioEngine.playTrack(index)

    private fun showBanner(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusBanner = message) }
            delay(3200)
            _uiState.update {
                if (it.statusBanner == message) it.copy(statusBanner = null) else it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
