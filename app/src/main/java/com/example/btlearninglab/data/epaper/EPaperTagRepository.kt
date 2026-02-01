package com.example.btlearninglab.data.epaper

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for managing E-Paper tag configurations.
 * Stores tags in SharedPreferences as JSON.
 */
class EPaperTagRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("epaper_tags", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TAGS = "tags"
        private const val KEY_LAST_SELECTED = "last_selected_index"
    }

    /**
     * Get all saved tags.
     */
    suspend fun getTags(): List<EPaperTag> = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_TAGS, null) ?: return@withContext emptyList()
        try {
            val jsonArray = JSONArray(json)
            val tags = mutableListOf<EPaperTag>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                tags.add(
                    EPaperTag(
                        name = obj.getString("name"),
                        ipAddress = obj.getString("ipAddress"),
                        macAddress = obj.getString("macAddress")
                    )
                )
            }
            tags
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save tags.
     */
    suspend fun saveTags(tags: List<EPaperTag>) = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray()
        tags.forEach { tag ->
            val obj = JSONObject()
            obj.put("name", tag.name)
            obj.put("ipAddress", tag.ipAddress)
            obj.put("macAddress", tag.macAddress)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TAGS, jsonArray.toString()).apply()
    }

    /**
     * Add a new tag or update existing one.
     */
    suspend fun addOrUpdateTag(tag: EPaperTag) {
        val tags = getTags().toMutableList()
        val existingIndex = tags.indexOfFirst { it.macAddress == tag.macAddress }
        if (existingIndex >= 0) {
            tags[existingIndex] = tag
        } else {
            tags.add(tag)
        }
        saveTags(tags)
    }

    /**
     * Get last selected tag index.
     */
    fun getLastSelectedIndex(): Int {
        return prefs.getInt(KEY_LAST_SELECTED, -1)
    }

    /**
     * Save last selected tag index.
     */
    fun saveLastSelectedIndex(index: Int) {
        prefs.edit().putInt(KEY_LAST_SELECTED, index).apply()
    }
}
