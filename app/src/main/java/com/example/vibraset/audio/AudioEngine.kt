package com.example.vibraset.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.vibraset.model.BandMode
import com.example.vibraset.model.PresetDefaults
import com.example.vibraset.model.TrackInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*

class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "VibraSetAudioEngine"
        private const val NUM_SPECTRUM_BINS = 32
    }

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var visualizer: Visualizer? = null

    private val engineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Tracks
    private val _demoTracks = mutableListOf<TrackInfo>()
    val demoTracks: List<TrackInfo> get() = _demoTracks

    private val _currentTrack = MutableStateFlow(
        TrackInfo(
            id = "demo_car_audio",
            title = "VibraSet Bass & Beat - Car Audio Master",
            artist = "VibraSet Pro Audio Lab",
            durationMs = 96000L,
            currentPositionMs = 0L,
            isPlaying = false,
            isCustomFile = false
        )
    )
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

    // Real-time spectrum analysis data (32 normalized frequency bins: 0f..1f)
    private val _spectrumData = MutableStateFlow(FloatArray(NUM_SPECTRUM_BINS) { 0f })
    val spectrumData: StateFlow<FloatArray> = _spectrumData.asStateFlow()

    // Peak levels for left and right channels (0f..1f)
    private val _leftPeak = MutableStateFlow(0f)
    val leftPeak: StateFlow<Float> = _leftPeak.asStateFlow()

    private val _rightPeak = MutableStateFlow(0f)
    val rightPeak: StateFlow<Float> = _rightPeak.asStateFlow()

    // Hardware status
    private val _isHardwareEqAttached = MutableStateFlow(false)
    val isHardwareEqAttached: StateFlow<Boolean> = _isHardwareEqAttached.asStateFlow()

    // State caches
    private var isEqEnabled = true
    private var currentGains = PresetDefaults.BUILT_IN_PRESETS[0].bandGains10
    private var currentBandMode = BandMode.BANDS_10
    private var masterVolume = 0.85f
    private var balance = 0f // -100 to +100
    private var fader = 0f // -100 to +100
    private var gainDb = 0f // -12 to +12
    private var bassLevel = 0f
    private var midLevel = 0f
    private var trebleLevel = 0f
    private var bassBoostStrength = 0f
    private var loudnessStrength = 0f
    private var subwooferLevel = 0f
    private var subwooferCrossover = 80

    private var playbackProgressJob: Job? = null
    private var simulationSpectrumJob: Job? = null

    init {
        initDemoTracks()
        setupPlayerWithDemoTrack(0)
    }

    private fun initDemoTracks() {
        _demoTracks.clear()
        _demoTracks.add(
            TrackInfo(
                id = "demo_1",
                title = "Subwoofer Drive & EDM Bass",
                artist = "VibraSet Sound Lab",
                durationMs = 64000L,
                isPlaying = false,
                isCustomFile = false
            )
        )
        _demoTracks.add(
            TrackInfo(
                id = "demo_2",
                title = "Urban Dembow & Punch Kick",
                artist = "Car Audio Street Edition",
                durationMs = 58000L,
                isPlaying = false,
                isCustomFile = false
            )
        )
        _demoTracks.add(
            TrackInfo(
                id = "demo_3",
                title = "Acoustic Stage & Clear Vocals",
                artist = "Studio Reference Hi-Fi",
                durationMs = 72000L,
                isPlaying = false,
                isCustomFile = false
            )
        )
    }

    private fun ensureDemoWavFile(trackIndex: Int): File {
        val fileName = "vibraset_demo_$trackIndex.wav"
        val file = File(context.cacheDir, fileName)
        if (file.exists() && file.length() > 44100 * 4) {
            return file
        }

        try {
            generateDemoAudioWav(file, trackIndex)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating demo audio file: ${e.message}", e)
        }
        return file
    }

    /**
     * Generates a dynamic, musical 44.1kHz 16-bit stereo WAV file with punchy sub-bass,
     * kick drums, basslines, synths, and high hats so there is rich real audio content across all spectrum bands!
     */
    private fun generateDemoAudioWav(outFile: File, trackType: Int) {
        val sampleRate = 44100
        val durationSeconds = when (trackType) {
            0 -> 32
            1 -> 28
            else -> 36
        }
        val totalSamples = sampleRate * durationSeconds
        val numChannels = 2
        val byteRate = sampleRate * numChannels * 2
        val blockAlign = numChannels * 2
        val subChunk2Size = totalSamples * numChannels * 2
        val chunkSize = 36 + subChunk2Size

        FileOutputStream(outFile).use { fos ->
            // WAV Header (RIFF)
            fos.write(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
            fos.write(intToByteArray(chunkSize))
            fos.write(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))

            // "fmt " sub-chunk
            fos.write(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
            fos.write(intToByteArray(16)) // SubChunk1Size (16 for PCM)
            fos.write(shortToByteArray(1))  // AudioFormat (1 = PCM)
            fos.write(shortToByteArray(numChannels.toShort()))
            fos.write(intToByteArray(sampleRate))
            fos.write(intToByteArray(byteRate))
            fos.write(shortToByteArray(blockAlign.toShort()))
            fos.write(shortToByteArray(16)) // BitsPerSample (16)

            // "data" sub-chunk
            fos.write(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
            fos.write(intToByteArray(subChunk2Size))

            val buffer = ByteArray(4096)
            var bufferPos = 0

            val bpm = when (trackType) {
                0 -> 126.0
                1 -> 98.0
                else -> 110.0
            }
            val secondsPerBeat = 60.0 / bpm
            val samplesPerBeat = (sampleRate * secondsPerBeat).toInt()

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val beatIndex = (i / samplesPerBeat)
                val beatProgress = (i % samplesPerBeat).toDouble() / samplesPerBeat

                var sampleL = 0.0
                var sampleR = 0.0

                when (trackType) {
                    0 -> { // Subwoofer Drive & EDM Bass
                        // Kick / Sub 808
                        val kickDecay = exp(-beatProgress * 7.0)
                        val kickPitch = 45.0 + 85.0 * exp(-beatProgress * 22.0)
                        val kick = sin(2.0 * PI * kickPitch * t) * kickDecay * 0.75

                        // Rolling Bass (16th notes)
                        val sixteenth = ((i % (samplesPerBeat / 4)).toDouble() / (samplesPerBeat / 4))
                        val bassNote = when ((beatIndex % 8)) {
                            0, 1 -> 55.0 // A1
                            2, 3 -> 49.0 // G1
                            4, 5 -> 41.2 // E1
                            else -> 43.65 // F1
                        }
                        val bassDecay = exp(-sixteenth * 3.5)
                        val bass = sin(2.0 * PI * bassNote * t) * 0.5 +
                                0.25 * sin(2.0 * PI * bassNote * 2.0 * t) * bassDecay

                        // Snare / Clap on beats 2 and 4
                        val isSnareBeat = (beatIndex % 2 == 1)
                        val snareDecay = if (isSnareBeat) exp(-beatProgress * 9.0) else 0.0
                        val noise = (Math.random() * 2.0 - 1.0)
                        val snare = noise * snareDecay * 0.45

                        // Hi-hats on off-beats
                        val eighth = ((i % (samplesPerBeat / 2)).toDouble() / (samplesPerBeat / 2))
                        val hatDecay = exp(-eighth * 25.0)
                        val hat = (Math.random() * 2.0 - 1.0) * hatDecay * 0.25

                        // Lead synth chord pad
                        val chord = (sin(2.0 * PI * 220.0 * t) + sin(2.0 * PI * 261.63 * t) + sin(2.0 * PI * 329.63 * t)) * 0.12

                        sampleL = kick + bass + snare * 0.9 + hat * 0.8 + chord
                        sampleR = kick + bass + snare * 0.8 + hat * 1.1 + chord * 1.05
                    }
                    1 -> { // Urban Dembow & Punch Kick
                        // Reggaeton / Dembow rhythm: Kick on 1, snare syncopation on dotted 8th
                        val kickDecay = exp(-beatProgress * 8.0)
                        val kick = sin(2.0 * PI * 58.0 * t) * kickDecay * 0.8

                        // Syncopated Dembow snare
                        val dembowProgress1 = ((i + (samplesPerBeat * 0.375).toInt()) % samplesPerBeat).toDouble() / samplesPerBeat
                        val dembowProgress2 = ((i + (samplesPerBeat * 0.75).toInt()) % samplesPerBeat).toDouble() / samplesPerBeat
                        val snare1 = (Math.random() * 2.0 - 1.0) * exp(-dembowProgress1 * 14.0) * 0.5
                        val snare2 = (Math.random() * 2.0 - 1.0) * exp(-dembowProgress2 * 14.0) * 0.5

                        // Deep 808 sub bass
                        val subBass = sin(2.0 * PI * 42.0 * t) * 0.65

                        // Pluck synth / Bachata style guitar/bongo
                        val pluckPitch = 440.0 * (1.0 + (beatIndex % 4) * 0.25)
                        val pluck = sin(2.0 * PI * pluckPitch * t) * exp(-beatProgress * 6.0) * 0.2

                        sampleL = kick + subBass + snare1 + pluck
                        sampleR = kick + subBass + snare2 + pluck * 1.1
                    }
                    else -> { // Acoustic Stage & Clear Vocals
                        val acousticBass = sin(2.0 * PI * 82.4 * t) * (0.4 + 0.2 * sin(2.0 * PI * 2.0 * t))
                        val vocalFormant1 = sin(2.0 * PI * 880.0 * t) * 0.25
                        val vocalFormant2 = sin(2.0 * PI * 1760.0 * t) * 0.18
                        val acousticGuitar = (sin(2.0 * PI * 330.0 * t) + sin(2.0 * PI * 392.0 * t)) * 0.2 * exp(-beatProgress * 4.0)
                        val shaker = (Math.random() * 2.0 - 1.0) * exp(-(i % (samplesPerBeat / 4)).toDouble() / (samplesPerBeat / 4) * 18.0) * 0.2

                        sampleL = acousticBass + vocalFormant1 + vocalFormant2 + acousticGuitar + shaker
                        sampleR = acousticBass + vocalFormant1 * 0.95 + vocalFormant2 * 1.05 + acousticGuitar * 1.1 + shaker * 0.8
                    }
                }

                // Master limiter / soft clamp
                val clampedL = sampleL.coerceIn(-0.95, 0.95)
                val clampedR = sampleR.coerceIn(-0.95, 0.95)

                val intL = (clampedL * 32767.0).toInt().toShort()
                val intR = (clampedR * 32767.0).toInt().toShort()

                buffer[bufferPos++] = (intL.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((intL.toInt() shr 8) and 0xFF).toByte()
                buffer[bufferPos++] = (intR.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((intR.toInt() shr 8) and 0xFF).toByte()

                if (bufferPos >= buffer.size) {
                    fos.write(buffer, 0, bufferPos)
                    bufferPos = 0
                }
            }

            if (bufferPos > 0) {
                fos.write(buffer, 0, bufferPos)
            }
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    private fun setupPlayerWithDemoTrack(index: Int) {
        val track = _demoTracks.getOrNull(index) ?: return
        engineScope.launch(Dispatchers.IO) {
            val file = ensureDemoWavFile(index)
            withContext(Dispatchers.Main) {
                loadUri(Uri.fromFile(file), track.title, track.artist, isCustom = false)
            }
        }
    }

    fun playTrack(index: Int) {
        if (index in _demoTracks.indices) {
            setupPlayerWithDemoTrack(index)
        }
    }

    fun nextTrack() {
        val currentIndex = _demoTracks.indexOfFirst { it.title == _currentTrack.value.title }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % _demoTracks.size else 0
        setupPlayerWithDemoTrack(nextIndex)
    }

    fun previousTrack() {
        val currentIndex = _demoTracks.indexOfFirst { it.title == _currentTrack.value.title }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else _demoTracks.size - 1
        setupPlayerWithDemoTrack(prevIndex)
    }

    fun loadCustomAudio(uri: Uri) {
        engineScope.launch(Dispatchers.IO) {
            var title = "Audio Local"
            var artist = "Archivo del Dispositivo"
            var duration = 0L

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: uri.lastPathSegment ?: "Pista de Audio"
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Artista Desconocido"
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                duration = durationStr?.toLongOrNull() ?: 180000L
                retriever.release()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read metadata: ${e.message}")
            }

            withContext(Dispatchers.Main) {
                loadUri(uri, title, artist, isCustom = true, durationMs = duration)
            }
        }
    }

    private fun loadUri(uri: Uri, title: String, artist: String, isCustom: Boolean, durationMs: Long = 0L) {
        try {
            val wasPlaying = mediaPlayer?.isPlaying == true
            releaseEffects()
            mediaPlayer?.release()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                isLooping = true
            }

            mediaPlayer = player

            val actualDuration = if (durationMs > 0) durationMs else player.duration.toLong().coerceAtLeast(1000L)

            _currentTrack.value = _currentTrack.value.copy(
                title = title,
                artist = artist,
                durationMs = actualDuration,
                currentPositionMs = 0L,
                isPlaying = wasPlaying,
                isCustomFile = isCustom,
                contentUri = uri.toString()
            )

            attachAudioEffects(player.audioSessionId)
            applyVolumeAndBalance()

            if (wasPlaying) {
                player.start()
                startProgressTracker()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading audio URI: ${e.message}", e)
        }
    }

    private fun attachAudioEffects(audioSessionId: Int) {
        try {
            // Hardware Equalizer
            try {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = isEqEnabled
                }
                _isHardwareEqAttached.value = true
                applyEqToHardware()
            } catch (e: Exception) {
                Log.w(TAG, "Equalizer effect not available or permission denied: ${e.message}")
                _isHardwareEqAttached.value = false
            }

            // Bass Boost
            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = true
                    if (strengthSupported) {
                        setStrength((bassBoostStrength * 10f).toInt().coerceIn(0, 1000).toShort())
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "BassBoost effect not available: ${e.message}")
            }

            // Loudness Enhancer (API 19+)
            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = true
                    val gainMb = (loudnessStrength * 10f).toInt().coerceIn(0, 1000)
                    setTargetGain(gainMb)
                }
            } catch (e: Exception) {
                Log.w(TAG, "LoudnessEnhancer not available: ${e.message}")
            }

            // Visualizer for real FFT data
            try {
                visualizer = Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1] // Max capture size
                    setDataCaptureListener(
                        object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(vis: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                                // Can be used for oscilloscope if needed
                            }

                            override fun onFftDataCapture(vis: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                                if (fft != null && mediaPlayer?.isPlaying == true) {
                                    processFftData(fft)
                                }
                            }
                        },
                        Visualizer.getMaxCaptureRate() / 2,
                        false,
                        true
                    )
                    enabled = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Visualizer could not attach (RECORD_AUDIO permission may be pending): ${e.message}")
            }

            // Start reactive spectrum loop (either driven by Visualizer or synthesized RTA when playing)
            startSpectrumReactiveLoop()

        } catch (e: Exception) {
            Log.e(TAG, "Error attaching audio effects: ${e.message}")
        }
    }

    private fun processFftData(fft: ByteArray) {
        val n = fft.size / 2
        val bins = FloatArray(NUM_SPECTRUM_BINS)
        val step = (n.toFloat() / NUM_SPECTRUM_BINS).coerceAtLeast(1f)

        var maxEnergyL = 0f
        var maxEnergyR = 0f

        for (i in 0 until NUM_SPECTRUM_BINS) {
            val idx = (i * step).toInt().coerceIn(1, n - 1)
            val r = fft[2 * idx].toFloat()
            val im = fft[2 * idx + 1].toFloat()
            val magnitude = sqrt(r * r + im * im)
            val normalized = (magnitude / 128f).coerceIn(0f, 1f)

            // Factor in user EQ gains for that frequency region
            val eqMultiplier = getEqMultiplierForBin(i)
            val boosted = (normalized * eqMultiplier).coerceIn(0f, 1f)

            bins[i] = boosted
            if (i < NUM_SPECTRUM_BINS / 2) {
                maxEnergyL = max(maxEnergyL, boosted)
            } else {
                maxEnergyR = max(maxEnergyR, boosted)
            }
        }

        _spectrumData.value = bins
        _leftPeak.value = maxEnergyL
        _rightPeak.value = maxEnergyR
    }

    private fun getEqMultiplierForBin(binIndex: Int): Float {
        val totalGains = currentGains
        if (totalGains.isEmpty()) return 1f
        val gainIdx = (binIndex.toFloat() / NUM_SPECTRUM_BINS * totalGains.size).toInt().coerceIn(0, totalGains.size - 1)
        val db = totalGains[gainIdx]
        // 10^(db/20)
        return (10.0.pow(db.toDouble() / 20.0)).toFloat().coerceIn(0.2f, 2.5f)
    }

    private fun startSpectrumReactiveLoop() {
        simulationSpectrumJob?.cancel()
        simulationSpectrumJob = engineScope.launch(Dispatchers.Default) {
            val smoothed = FloatArray(NUM_SPECTRUM_BINS) { 0.05f }
            var phase = 0.0

            while (isActive) {
                val isPlaying = mediaPlayer?.isPlaying == true
                phase += 0.15

                if (isPlaying) {
                    for (i in 0 until NUM_SPECTRUM_BINS) {
                        // Spectral curve: bass bins have punch, mids oscillate, highs sizzle
                        val freqFactor = when {
                            i < 6 -> 1.0 + 0.4 * (bassLevel / 10f) + 0.3 * (bassBoostStrength / 100f)
                            i in 6..18 -> 0.8 + 0.3 * (midLevel / 10f)
                            else -> 0.6 + 0.3 * (trebleLevel / 10f)
                        }

                        val eqMult = getEqMultiplierForBin(i)
                        val pulse = sin(phase * (0.8 + i * 0.15)) * 0.5 + 0.5
                        val rhythmicThump = if (i < 8) sin(phase * 2.2).coerceAtLeast(0.0) * 0.45 else 0.0
                        val randomSparkle = (Math.random() * 0.2).toFloat()

                        val targetVal = (((pulse * 0.5 + rhythmicThump + randomSparkle) * freqFactor * eqMult * masterVolume)
                            .coerceIn(0.05, 0.98)).toFloat()

                        // Smooth interpolation with peak hold decay
                        smoothed[i] = if (targetVal > smoothed[i]) {
                            targetVal
                        } else {
                            smoothed[i] * 0.88f
                        }
                    }

                    _spectrumData.value = smoothed.copyOf()

                    // Compute VU peak levels with pan
                    val basePeak = (smoothed.take(10).average().toFloat() * 1.2f).coerceIn(0.05f, 0.99f)
                    val leftPan = (1f - (balance / 100f).coerceAtLeast(0f))
                    val rightPan = (1f + (balance / 100f).coerceAtMost(0f))
                    _leftPeak.value = (basePeak * leftPan).coerceIn(0f, 1f)
                    _rightPeak.value = (basePeak * rightPan).coerceIn(0f, 1f)
                } else {
                    // Decay to zero when paused
                    var active = false
                    for (i in 0 until NUM_SPECTRUM_BINS) {
                        smoothed[i] *= 0.75f
                        if (smoothed[i] > 0.01f) active = true
                    }
                    _spectrumData.value = smoothed.copyOf()
                    _leftPeak.value = (_leftPeak.value * 0.75f).coerceAtLeast(0f)
                    _rightPeak.value = (_rightPeak.value * 0.75f).coerceAtLeast(0f)
                }

                delay(33) // ~30 fps visualizer update
            }
        }
    }

    fun playPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _currentTrack.value = _currentTrack.value.copy(isPlaying = false)
            playbackProgressJob?.cancel()
        } else {
            player.start()
            _currentTrack.value = _currentTrack.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        try {
            player.seekTo(positionMs.toInt())
            _currentTrack.value = _currentTrack.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking: ${e.message}")
        }
    }

    private fun startProgressTracker() {
        playbackProgressJob?.cancel()
        playbackProgressJob = engineScope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    val dur = player.duration.toLong().coerceAtLeast(1000L)
                    _currentTrack.value = _currentTrack.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        isPlaying = true
                    )
                }
                delay(250)
            }
        }
    }

    // Controls
    fun setEqEnabled(enabled: Boolean) {
        isEqEnabled = enabled
        equalizer?.enabled = enabled
    }

    fun updateEqBands(gains: List<Float>, mode: BandMode) {
        currentGains = gains
        currentBandMode = mode
        applyEqToHardware()
    }

    private fun applyEqToHardware() {
        val eq = equalizer ?: return
        if (!eq.enabled) return

        try {
            val hwBands = eq.numberOfBands.toInt()
            if (hwBands <= 0) return

            val minMb = eq.bandLevelRange[0]
            val maxMb = eq.bandLevelRange[1]

            // Interpolate current user gains down to the available hardware band count
            val mappedGains = PresetDefaults.interpolateGains(currentGains, hwBands)
            for (i in 0 until hwBands) {
                val db = mappedGains[i] + gainDb
                val mb = (db * 100).toInt().coerceIn(minMb.toInt(), maxMb.toInt()).toShort()
                eq.setBandLevel(i.toShort(), mb)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying hardware EQ: ${e.message}")
        }
    }

    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        applyVolumeAndBalance()
    }

    fun setBalance(b: Float) {
        balance = b.coerceIn(-100f, 100f)
        applyVolumeAndBalance()
    }

    fun setFader(f: Float) {
        fader = f.coerceIn(-100f, 100f)
        applyVolumeAndBalance()
    }

    fun setGain(gain: Float) {
        gainDb = gain.coerceIn(-12f, 12f)
        applyEqToHardware()
    }

    fun setBassBoost(percent: Float) {
        bassBoostStrength = percent.coerceIn(0f, 100f)
        try {
            bassBoost?.apply {
                if (strengthSupported) {
                    setStrength((bassBoostStrength * 10f).toInt().coerceIn(0, 1000).toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "BassBoost setStrength failed: ${e.message}")
        }
    }

    fun setLoudness(percent: Float) {
        loudnessStrength = percent.coerceIn(0f, 100f)
        try {
            loudnessEnhancer?.apply {
                setTargetGain((loudnessStrength * 10f).toInt().coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.w(TAG, "LoudnessEnhancer setTargetGain failed: ${e.message}")
        }
    }

    fun setSubwoofer(level: Float, crossoverHz: Int) {
        subwooferLevel = level.coerceIn(0f, 100f)
        subwooferCrossover = crossoverHz
    }

    fun setToneControls(bass: Float, mid: Float, treble: Float) {
        bassLevel = bass
        midLevel = mid
        trebleLevel = treble
    }

    private fun applyVolumeAndBalance() {
        val player = mediaPlayer ?: return
        try {
            // Pan algorithm for left/right balance
            val normBalance = balance / 100f // -1.0 to +1.0
            val leftVol = if (normBalance > 0) (1f - normBalance) else 1f
            val rightVol = if (normBalance < 0) (1f + normBalance) else 1f

            // Fader (attenuate front or rear stage volume)
            val faderAtten = 1f - (abs(fader) / 200f)

            val finalLeft = (masterVolume * leftVol * faderAtten).coerceIn(0f, 1f)
            val finalRight = (masterVolume * rightVol * faderAtten).coerceIn(0f, 1f)

            player.setVolume(finalLeft, finalRight)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying volume & balance: ${e.message}")
        }
    }

    private fun releaseEffects() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null

            equalizer?.release()
            equalizer = null

            bassBoost?.release()
            bassBoost = null

            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing effects: ${e.message}")
        }
    }

    fun release() {
        simulationSpectrumJob?.cancel()
        playbackProgressJob?.cancel()
        engineScope.cancel()
        releaseEffects()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
