package com.droidai.assistant.ai

import android.content.Context
import android.graphics.Bitmap
import com.droidai.assistant.data.SecurePreferences
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiVisionClient(private val context: Context) {

    private fun getModel(): GenerativeModel {
        val apiKey = SecurePreferences.getApiKey(context)
            ?: throw IllegalStateException("Gemini API Key belum dikonfigurasi! Buka aplikasi DroidAI.")
        
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    /**
     * Mengirim gambar layar ke Gemini Vision dan meminta jawaban soal serta aksi sentuh UI
     */
    suspend fun analyzeScreenForAutoAnswer(bitmap: Bitmap): AutoAnswerResult = withContext(Dispatchers.IO) {
        val prompt = """
            Kamu adalah asisten otomatis pintar yang sedang mengamati layar ponsel Android pengguna.
            Periksa gambar layar berikut:
            1. Identifikasi apakah ada pertanyaan formulir ujian, kuis (misal Google Form, Quizizz, dsb.), atau dialog pilihan ganda.
            2. Ekstrak teks pertanyaan dan semua opsi jawaban (A, B, C, D, dsb.).
            3. Tentukan pilihan mana yang BENAR secara ilmiah dan faktual.
            4. Tentukan teks target persis dari opsi tersebut agar Accessibility Service dapat mengkliknya.
            
            Keluarkan jawaban HANYA dalam format JSON valid tanpa markdown tambahan:
            {
              "questionDetected": true,
              "questionText": "...",
              "correctOptionText": "...",
              "actionType": "CLICK_TEXT",
              "explanation": "...",
              "confidence": 0.99
            }
        """.trimIndent()

        try {
            val model = getModel()
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            val responseText = response.text ?: "{}"
            
            // Ekstrak blok JSON
            val cleanJson = responseText.substringAfter("{").substringBeforeLast("}")
            val jsonObject = JSONObject("{$cleanJson}")

            AutoAnswerResult(
                questionDetected = jsonObject.optBoolean("questionDetected", false),
                questionText = jsonObject.optString("questionText"),
                correctOptionText = jsonObject.optString("correctOptionText"),
                actionType = jsonObject.optString("actionType", "CLICK_TEXT"),
                explanation = jsonObject.optString("explanation"),
                confidence = jsonObject.optDouble("confidence", 1.0)
            )
        } catch (e: Exception) {
            AutoAnswerResult(
                questionDetected = false,
                errorMessage = e.localizedMessage ?: "Gagal menghubungi Gemini API"
            )
        }
    }
}

data class AutoAnswerResult(
    val questionDetected: Boolean,
    val questionText: String? = null,
    val correctOptionText: String? = null,
    val actionType: String = "CLICK_TEXT",
    val explanation: String? = null,
    val confidence: Double = 0.0,
    val errorMessage: String? = null
)