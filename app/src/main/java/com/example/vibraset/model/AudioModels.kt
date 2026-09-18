package com.example.vibraset.model

import androidx.compose.ui.graphics.Color

enum class BandMode(val bandCount: Int, val title: String) {
    BANDS_10(10, "10 Bandas"),
    BANDS_15(15, "15 Bandas"),
    BANDS_31(31, "31 Bandas")
}

data class EqBand(
    val index: Int,
    val frequencyHz: Int,
    val label: String,
    val gainDb: Float, // -12.0f to +12.0f
    val rangeCategory: FrequencyRange
)

enum class FrequencyRange(val displayName: String, val colorHex: Long) {
    SUBGRAVES("Subgraves", 0xFFFF2A55), // Red / Deep Neon Red
    GRAVES("Graves", 0xFFFF8800),       // Amber / Orange
    MEDIOS("Medios", 0xFF10B981),       // Green / Lime
    AGUDOS("Agudos", 0xFF00E5FF)        // Cyan / Electric Blue
}

enum class SystemProfile(val id: String, val title: String, val description: String, val iconName: String) {
    STEREO_2("2_speakers", "2 Bocinas", "Estéreo estándar frontal (2.0)", "SpeakerGroup"),
    QUAD_4("4_speakers", "4 Bocinas", "Cuatro canales (Frontal + Trasero)", "VolumeUp"),
    SUBWOOFER_SYSTEM("subwoofer", "Sistema con Subwoofer", "2.1 / 4.1 con refuerzo LFE activo", "Speaker"),
    MINI_COMPONENTE("mini_componente", "Mini Componente", "Equipo de sonido doméstico Hi-Fi con Mega Bass", "Radio"),
    VEHICULO("car_audio", "Sistema de Vehículo", "Audio automotriz con fader y crossover para cabina", "DirectionsCar"),
    CUSTOM("custom", "Sistema Personalizado", "Alineación y canales a medida", "Tune")
}

enum class AutoSetTarget(val id: String, val title: String, val subtitle: String, val description: String) {
    GRAVES_PROFUNDOS(
        "deep_bass",
        "Graves Profundos",
        "Sub-bass 20Hz - 60Hz",
        "Enfatiza las frecuencias ultrabajas para vibración profunda y sensación sísmica."
    ),
    GRAVES_FUERTES(
        "punchy_bass",
        "Graves Fuertes",
        "Punch 60Hz - 120Hz",
        "Golpe de bajo seco, contundente y potente sin distorsión."
    ),
    VOCES_CLARAS(
        "clear_vocals",
        "Voces Claras",
        "Medios 1kHz - 4kHz",
        "Realce en presencia vocal y diálogos, atenuando el exceso de graves que opacan la voz."
    ),
    SONIDO_EQUILIBRADO(
        "balanced",
        "Sonido Equilibrado",
        "Respuesta plana Hi-Fi",
        "Afinación de referencia con curva acústica uniforme para todo género."
    ),
    MAS_VOLUMEN(
        "loudness_boost",
        "Más Volumen",
        "Ganancia y dinámica",
        "Aumento en compresión de sonoridad y presencia para espacios abiertos."
    ),
    SONIDO_VEHICULO(
        "car_sound",
        "Sonido para Vehículo",
        "Compensación de cabina",
        "Curva en V con compensación de ruido de rodadura, subgraves elevados y fader centrado."
    ),
    SONIDO_MINI_COMPONENTE(
        "mini_componente_sound",
        "Sonido para Mini Componente",
        "Mega Bass & Live Mids",
        "Afinación inspirada en torres de sonido con graves retumbantes y brillo en agudos."
    )
}

data class SoundPreset(
    val id: String,
    val name: String,
    val isUserCreated: Boolean = false,
    val bandGains10: List<Float>,
    val bandGains15: List<Float>,
    val bandGains31: List<Float>,
    val bass: Float = 0f,
    val mid: Float = 0f,
    val treble: Float = 0f,
    val bassBoost: Float = 0f,
    val loudness: Float = 0f,
    val subwooferLevel: Float = 0f,
    val subwooferCrossoverHz: Int = 80,
    val gain: Float = 0f
)

data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isCustomFile: Boolean = false,
    val contentUri: String? = null
)
