package com.example.vibraset.model

object PresetDefaults {

    // Standard 10 Bands (ISO 1-octave spacing)
    val FREQS_10 = listOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    val LABELS_10 = listOf("31Hz", "63Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")

    // Standard 15 Bands (2/3-octave spacing)
    val FREQS_15 = listOf(
        25, 40, 63, 100, 160, 250, 400, 630, 1000, 1600, 2500, 4000, 6300, 10000, 16000
    )
    val LABELS_15 = listOf(
        "25Hz", "40Hz", "63Hz", "100Hz", "160Hz", "250Hz", "400Hz", "630Hz",
        "1kHz", "1.6k", "2.5k", "4kHz", "6.3k", "10k", "16kHz"
    )

    // Standard 31 Bands (1/3-octave ISO standard)
    val FREQS_31 = listOf(
        20, 25, 31, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500,
        630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000,
        10000, 12500, 16000, 20000
    )
    val LABELS_31 = listOf(
        "20Hz", "25Hz", "31Hz", "40Hz", "50Hz", "63Hz", "80Hz", "100Hz", "125Hz",
        "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz", "630Hz", "800Hz",
        "1kHz", "1.2k", "1.6k", "2kHz", "2.5k", "3.1k", "4kHz", "5kHz", "6.3k",
        "8kHz", "10k", "12.5k", "16k", "20kHz"
    )

    fun categorizeFrequency(hz: Int): FrequencyRange {
        return when {
            hz <= 60 -> FrequencyRange.SUBGRAVES
            hz <= 250 -> FrequencyRange.GRAVES
            hz <= 4000 -> FrequencyRange.MEDIOS
            else -> FrequencyRange.AGUDOS
        }
    }

    // Helper to generate 15 and 31 bands from a 10-band profile using smooth spline/linear interpolation
    fun interpolateGains(sourceGains: List<Float>, targetCount: Int): List<Float> {
        if (sourceGains.size == targetCount) return sourceGains
        val result = mutableListOf<Float>()
        for (i in 0 until targetCount) {
            val progress = i.toFloat() / (targetCount - 1).coerceAtLeast(1)
            val sourceIndexFloat = progress * (sourceGains.size - 1)
            val lower = sourceIndexFloat.toInt()
            val upper = (lower + 1).coerceAtMost(sourceGains.size - 1)
            val fraction = sourceIndexFloat - lower
            val interpolated = sourceGains[lower] + fraction * (sourceGains[upper] - sourceGains[lower])
            result.add((interpolated * 10f).toInt() / 10f)
        }
        return result
    }

    private fun createPreset(
        id: String,
        name: String,
        gains10: List<Float>,
        bass: Float = 0f,
        mid: Float = 0f,
        treble: Float = 0f,
        bassBoost: Float = 0f,
        loudness: Float = 0f,
        subwooferLevel: Float = 0f,
        crossover: Int = 80,
        gain: Float = 0f
    ): SoundPreset {
        return SoundPreset(
            id = id,
            name = name,
            isUserCreated = false,
            bandGains10 = gains10,
            bandGains15 = interpolateGains(gains10, 15),
            bandGains31 = interpolateGains(gains10, 31),
            bass = bass,
            mid = mid,
            treble = treble,
            bassBoost = bassBoost,
            loudness = loudness,
            subwooferLevel = subwooferLevel,
            subwooferCrossoverHz = crossover,
            gain = gain
        )
    }

    val BUILT_IN_PRESETS: List<SoundPreset> = listOf(
        createPreset(
            id = "car_sound",
            name = "Sonido de Carro",
            gains10 = listOf(6.0f, 5.0f, 3.5f, 1.0f, -0.5f, 0.5f, 2.0f, 3.5f, 5.0f, 6.0f),
            bass = 5f,
            mid = 0.5f,
            treble = 4.5f,
            bassBoost = 60f,
            loudness = 50f,
            subwooferLevel = 65f,
            crossover = 80,
            gain = 1.5f
        ),
        createPreset(
            id = "mini_componente",
            name = "Mini Componente",
            gains10 = listOf(7.5f, 6.0f, 4.0f, 1.5f, -1.0f, 1.0f, 2.5f, 4.0f, 5.5f, 7.0f),
            bass = 6.5f,
            mid = 1.0f,
            treble = 5.0f,
            bassBoost = 75f,
            loudness = 65f,
            subwooferLevel = 70f,
            crossover = 100,
            gain = 2.0f
        ),
        createPreset(
            id = "bass_potente",
            name = "Bass Potente",
            gains10 = listOf(9.0f, 8.5f, 6.5f, 3.0f, 0.0f, -1.0f, 0.0f, 1.5f, 3.0f, 4.0f),
            bass = 8.5f,
            mid = -0.5f,
            treble = 2.0f,
            bassBoost = 90f,
            loudness = 60f,
            subwooferLevel = 90f,
            crossover = 80,
            gain = 2.0f
        ),
        createPreset(
            id = "bass_profundo",
            name = "Bass Profundo",
            gains10 = listOf(10.0f, 8.0f, 5.0f, 1.5f, -1.0f, -1.5f, -1.0f, 0.5f, 2.0f, 3.0f),
            bass = 9.0f,
            mid = -1.0f,
            treble = 1.0f,
            bassBoost = 95f,
            loudness = 45f,
            subwooferLevel = 85f,
            crossover = 50,
            gain = 1.0f
        ),
        createPreset(
            id = "bachata",
            name = "Bachata",
            gains10 = listOf(3.5f, 3.0f, 2.0f, 1.5f, 2.5f, 4.0f, 4.5f, 3.5f, 4.0f, 5.0f),
            bass = 3.0f,
            mid = 3.5f,
            treble = 4.0f,
            bassBoost = 35f,
            loudness = 40f,
            subwooferLevel = 45f,
            crossover = 80,
            gain = 1.0f
        ),
        createPreset(
            id = "reggaeton",
            name = "Reggaetón",
            gains10 = listOf(8.0f, 7.5f, 5.0f, 1.0f, -0.5f, 1.5f, 3.0f, 4.0f, 5.0f, 6.5f),
            bass = 7.5f,
            mid = 1.0f,
            treble = 4.5f,
            bassBoost = 80f,
            loudness = 60f,
            subwooferLevel = 80f,
            crossover = 80,
            gain = 2.0f
        ),
        createPreset(
            id = "rap",
            name = "Rap",
            gains10 = listOf(8.5f, 7.0f, 4.0f, 0.5f, -1.5f, 1.0f, 2.5f, 4.0f, 4.5f, 5.5f),
            bass = 8.0f,
            mid = 0.5f,
            treble = 4.0f,
            bassBoost = 85f,
            loudness = 55f,
            subwooferLevel = 75f,
            crossover = 60,
            gain = 1.5f
        ),
        createPreset(
            id = "electronica",
            name = "Electrónica",
            gains10 = listOf(8.5f, 7.5f, 4.5f, 1.0f, 0.0f, 1.0f, 3.0f, 5.0f, 7.0f, 8.0f),
            bass = 8.0f,
            mid = 1.5f,
            treble = 6.5f,
            bassBoost = 75f,
            loudness = 70f,
            subwooferLevel = 75f,
            crossover = 80,
            gain = 2.5f
        ),
        createPreset(
            id = "voces_claras",
            name = "Voces Claras",
            gains10 = listOf(-3.0f, -2.0f, 0.0f, 2.0f, 4.5f, 6.0f, 5.5f, 4.0f, 2.0f, 1.0f),
            bass = -2.0f,
            mid = 5.5f,
            treble = 3.0f,
            bassBoost = 10f,
            loudness = 30f,
            subwooferLevel = 20f,
            crossover = 120,
            gain = 0.5f
        ),
        createPreset(
            id = "musica_suave",
            name = "Música Suave",
            gains10 = listOf(2.5f, 2.0f, 1.5f, 1.0f, 0.5f, 0.5f, 1.0f, 1.5f, 2.0f, 1.5f),
            bass = 2.0f,
            mid = 0.5f,
            treble = 1.5f,
            bassBoost = 25f,
            loudness = 20f,
            subwooferLevel = 30f,
            crossover = 80,
            gain = 0.0f
        ),
        createPreset(
            id = "noche",
            name = "Noche",
            gains10 = listOf(-4.0f, -3.0f, -1.0f, 1.0f, 2.0f, 2.5f, 2.0f, 1.0f, -1.0f, -3.0f),
            bass = -3.5f,
            mid = 2.0f,
            treble = -1.0f,
            bassBoost = 0f,
            loudness = 15f,
            subwooferLevel = 0f,
            crossover = 120,
            gain = -1.0f
        ),
        createPreset(
            id = "personalizado",
            name = "Personalizado",
            gains10 = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            bass = 0f,
            mid = 0f,
            treble = 0f,
            bassBoost = 0f,
            loudness = 0f,
            subwooferLevel = 0f,
            crossover = 80,
            gain = 0f
        )
    )

    fun getAutoSetConfiguration(target: AutoSetTarget): SoundPreset {
        return when (target) {
            AutoSetTarget.GRAVES_PROFUNDOS -> createPreset(
                id = "autoset_deep_bass",
                name = "Auto: Graves Profundos",
                gains10 = listOf(11.0f, 9.5f, 6.0f, 2.0f, -0.5f, -1.5f, -0.5f, 1.0f, 2.0f, 3.0f),
                bass = 9.5f,
                mid = -0.5f,
                treble = 1.5f,
                bassBoost = 95f,
                loudness = 50f,
                subwooferLevel = 90f,
                crossover = 50,
                gain = 1.5f
            )
            AutoSetTarget.GRAVES_FUERTES -> createPreset(
                id = "autoset_punchy_bass",
                name = "Auto: Graves Fuertes",
                gains10 = listOf(8.0f, 9.5f, 7.5f, 3.5f, 0.5f, -0.5f, 0.5f, 2.0f, 3.5f, 4.5f),
                bass = 8.5f,
                mid = 0.0f,
                treble = 2.5f,
                bassBoost = 85f,
                loudness = 65f,
                subwooferLevel = 85f,
                crossover = 80,
                gain = 2.0f
            )
            AutoSetTarget.VOCES_CLARAS -> createPreset(
                id = "autoset_vocals",
                name = "Auto: Voces Claras",
                gains10 = listOf(-4.0f, -2.5f, -0.5f, 2.5f, 5.0f, 6.5f, 6.0f, 4.5f, 2.5f, 1.5f),
                bass = -2.5f,
                mid = 6.0f,
                treble = 3.5f,
                bassBoost = 5f,
                loudness = 30f,
                subwooferLevel = 15f,
                crossover = 120,
                gain = 0.5f
            )
            AutoSetTarget.SONIDO_EQUILIBRADO -> createPreset(
                id = "autoset_balanced",
                name = "Auto: Sonido Equilibrado",
                gains10 = listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.5f, 1.5f),
                bass = 1.0f,
                mid = 0.0f,
                treble = 1.0f,
                bassBoost = 20f,
                loudness = 25f,
                subwooferLevel = 35f,
                crossover = 80,
                gain = 0.0f
            )
            AutoSetTarget.MAS_VOLUMEN -> createPreset(
                id = "autoset_loudness",
                name = "Auto: Más Volumen",
                gains10 = listOf(6.0f, 5.5f, 4.0f, 2.5f, 2.0f, 3.0f, 4.5f, 5.5f, 6.5f, 7.5f),
                bass = 5.5f,
                mid = 3.0f,
                treble = 5.5f,
                bassBoost = 65f,
                loudness = 85f,
                subwooferLevel = 60f,
                crossover = 80,
                gain = 4.0f
            )
            AutoSetTarget.SONIDO_VEHICULO -> createPreset(
                id = "autoset_car",
                name = "Auto: Sonido de Vehículo",
                gains10 = listOf(7.5f, 6.5f, 4.0f, 1.0f, -0.5f, 1.0f, 2.5f, 4.0f, 6.0f, 7.0f),
                bass = 6.5f,
                mid = 1.0f,
                treble = 5.5f,
                bassBoost = 70f,
                loudness = 60f,
                subwooferLevel = 75f,
                crossover = 80,
                gain = 2.0f
            )
            AutoSetTarget.SONIDO_MINI_COMPONENTE -> createPreset(
                id = "autoset_mini",
                name = "Auto: Mini Componente",
                gains10 = listOf(8.5f, 7.0f, 4.5f, 1.5f, -1.0f, 1.5f, 3.0f, 5.0f, 6.5f, 7.5f),
                bass = 7.5f,
                mid = 1.5f,
                treble = 6.0f,
                bassBoost = 80f,
                loudness = 75f,
                subwooferLevel = 80f,
                crossover = 100,
                gain = 2.5f
            )
        }
    }
}
