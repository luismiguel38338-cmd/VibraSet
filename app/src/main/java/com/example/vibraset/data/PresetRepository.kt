package com.example.vibraset.data

import android.content.Context
import android.content.SharedPreferences
import com.example.vibraset.model.PresetDefaults
import com.example.vibraset.model.SoundPreset
import org.json.JSONArray
import org.json.JSONObject

class PresetRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vibraset_presets", Context.MODE_PRIVATE)

    fun getUserPresets(): List<SoundPreset> {
        val raw = prefs.getString("custom_presets", null) ?: return emptyList()
        val list = mutableListOf<SoundPreset>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(jsonToPreset(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getAllPresets(): List<SoundPreset> {
        return PresetDefaults.BUILT_IN_PRESETS + getUserPresets()
    }

    fun saveUserPreset(preset: SoundPreset): List<SoundPreset> {
        val current = getUserPresets().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == preset.id || it.name.equals(preset.name, ignoreCase = true) }
        val toSave = preset.copy(isUserCreated = true)
        if (existingIndex >= 0) {
            current[existingIndex] = toSave
        } else {
            current.add(toSave)
        }
        saveList(current)
        return getAllPresets()
    }

    fun deleteUserPreset(presetId: String): List<SoundPreset> {
        val current = getUserPresets().toMutableList()
        current.removeAll { it.id == presetId }
        saveList(current)
        return getAllPresets()
    }

    private fun saveList(list: List<SoundPreset>) {
        val array = JSONArray()
        for (p in list) {
            array.put(presetToJson(p))
        }
        prefs.edit().putString("custom_presets", array.toString()).apply()
    }

    private fun presetToJson(p: SoundPreset): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("isUserCreated", true)
            put("gains10", JSONArray(p.bandGains10))
            put("gains15", JSONArray(p.bandGains15))
            put("gains31", JSONArray(p.bandGains31))
            put("bass", p.bass.toDouble())
            put("mid", p.mid.toDouble())
            put("treble", p.treble.toDouble())
            put("bassBoost", p.bassBoost.toDouble())
            put("loudness", p.loudness.toDouble())
            put("subwooferLevel", p.subwooferLevel.toDouble())
            put("subwooferCrossoverHz", p.subwooferCrossoverHz)
            put("gain", p.gain.toDouble())
        }
    }

    private fun jsonToPreset(obj: JSONObject): SoundPreset {
        val gains10 = jsonArrayToFloatList(obj.optJSONArray("gains10"))
        val gains15 = jsonArrayToFloatList(obj.optJSONArray("gains15"))
        val gains31 = jsonArrayToFloatList(obj.optJSONArray("gains31"))

        return SoundPreset(
            id = obj.getString("id"),
            name = obj.getString("name"),
            isUserCreated = true,
            bandGains10 = if (gains10.isNotEmpty()) gains10 else PresetDefaults.interpolateGains(listOf(0f), 10),
            bandGains15 = if (gains15.isNotEmpty()) gains15 else PresetDefaults.interpolateGains(gains10, 15),
            bandGains31 = if (gains31.isNotEmpty()) gains31 else PresetDefaults.interpolateGains(gains10, 31),
            bass = obj.optDouble("bass", 0.0).toFloat(),
            mid = obj.optDouble("mid", 0.0).toFloat(),
            treble = obj.optDouble("treble", 0.0).toFloat(),
            bassBoost = obj.optDouble("bassBoost", 0.0).toFloat(),
            loudness = obj.optDouble("loudness", 0.0).toFloat(),
            subwooferLevel = obj.optDouble("subwooferLevel", 0.0).toFloat(),
            subwooferCrossoverHz = obj.optInt("subwooferCrossoverHz", 80),
            gain = obj.optDouble("gain", 0.0).toFloat()
        )
    }

    private fun jsonArrayToFloatList(arr: JSONArray?): List<Float> {
        if (arr == null) return emptyList()
        val list = mutableListOf<Float>()
        for (i in 0 until arr.length()) {
            list.add(arr.getDouble(i).toFloat())
        }
        return list
    }
}
