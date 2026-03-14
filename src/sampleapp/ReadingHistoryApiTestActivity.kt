package com.example.mushafimad.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mushafimad.core.api.ReadingHistoryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadingHistoryApiTestActivity : AppCompatActivity() {

    private val readingHistoryApi = ReadingHistoryApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_history_api_test)

        lifecycleScope.launch { fetchReadingHistory() }
    }

    private suspend fun fetchReadingHistory() {
        try {
            val history = withContext(Dispatchers.IO) { readingHistoryApi.getReadingHistory() }
            displayReadingHistory(history)
        } catch (e: Exception) {
            showToast("Error fetching history: ${e.message}")
        }
    }

    private fun displayReadingHistory(history: List<String>) {
        // Logic to display reading history
        showToast("Fetched ${history.size} items from Reading History API.")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
