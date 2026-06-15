package com.yourcompany.standby.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _customQuotes = MutableStateFlow<List<String>>(emptyList())
    val customQuotes: StateFlow<List<String>> = _customQuotes.asStateFlow()

    private val allQuotes = mutableListOf<String>()
    private val maxLimit = 500

    init {
        loadQuotes()
    }

    private fun loadQuotes() {
        // 1. Load raw resources quotes
        try {
            val resourceId = context.resources.getIdentifier("quotes", "raw", context.packageName)
            if (resourceId != 0) {
                context.resources.openRawResource(resourceId).use { inputStream ->
                    val jsonText = inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonText)
                    val jsonArray = jsonObject.getJSONArray("quotes")
                    for (i in 0 until jsonArray.length()) {
                        val quote = jsonArray.getString(i).trim()
                        if (quote.isNotEmpty() && !allQuotes.contains(quote)) {
                            allQuotes.add(quote)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Load custom quotes from internal storage
        try {
            val customFile = File(context.filesDir, "quotes/custom_quotes.json")
            if (customFile.exists()) {
                val jsonText = customFile.readText()
                val jsonObject = JSONObject(jsonText)
                val jsonArray = jsonObject.getJSONArray("quotes")
                val customList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val quote = jsonArray.getString(i).trim()
                    if (quote.isNotEmpty()) {
                        customList.add(quote)
                        if (!allQuotes.contains(quote)) {
                            allQuotes.add(quote)
                        }
                    }
                }
                _customQuotes.value = customList.take(maxLimit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback default
        if (allQuotes.isEmpty()) {
            allQuotes.add("Peace begins with a smile.")
        }
    }

    suspend fun addCustomQuote(quote: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = quote.trim()
        if (trimmed.isEmpty()) return@withContext false

        val currentList = _customQuotes.value.toMutableList()
        if (currentList.size >= maxLimit) {
            return@withContext false // Exceeds limit
        }

        if (!currentList.contains(trimmed)) {
            currentList.add(trimmed)
            val success = saveCustomQuotesToFile(currentList)
            if (success) {
                _customQuotes.value = currentList
                if (!allQuotes.contains(trimmed)) {
                    allQuotes.add(trimmed)
                }
                return@withContext true
            }
        }
        return@withContext false
    }

    suspend fun deleteCustomQuote(quote: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = quote.trim()
        val currentList = _customQuotes.value.toMutableList()
        if (currentList.contains(trimmed)) {
            currentList.remove(trimmed)
            val success = saveCustomQuotesToFile(currentList)
            if (success) {
                _customQuotes.value = currentList
                allQuotes.remove(trimmed)
                return@withContext true
            }
        }
        return@withContext false
    }

    suspend fun importCustomQuotes(quotes: List<String>): Pair<Int, Boolean> = withContext(Dispatchers.IO) {
        val currentList = _customQuotes.value.toMutableList()
        var importedCount = 0
        var limitReached = false

        for (q in quotes) {
            val trimmed = q.trim()
            if (trimmed.isEmpty()) continue
            if (currentList.size >= maxLimit) {
                limitReached = true
                break
            }
            if (!currentList.contains(trimmed)) {
                currentList.add(trimmed)
                importedCount++
                if (!allQuotes.contains(trimmed)) {
                    allQuotes.add(trimmed)
                }
            }
        }

        if (importedCount > 0) {
            val success = saveCustomQuotesToFile(currentList)
            if (success) {
                _customQuotes.value = currentList
            }
        }
        return@withContext Pair(importedCount, limitReached)
    }

    private fun saveCustomQuotesToFile(list: List<String>): Boolean {
        return try {
            val customFile = File(context.filesDir, "quotes/custom_quotes.json")
            customFile.parentFile?.mkdirs()
            val jsonObject = JSONObject()
            val jsonArray = org.json.JSONArray()
            list.forEach { jsonArray.put(it) }
            jsonObject.put("quotes", jsonArray)
            customFile.writeText(jsonObject.toString())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getRandomQuote(): String {
        return if (allQuotes.isNotEmpty()) {
            allQuotes.random()
        } else {
            "Peace begins with a smile."
        }
    }
}
