package com.droidai.assistant.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.droidai.assistant.service.AutonomousAccessibilityService

object AutoAnswerEngine {

    private const val TAG = "DroidAI_AutoEngine"

    suspend fun analyzeAndExecuteAnswer(
        context: Context,
        screenBitmap: Bitmap,
        onResult: (Boolean, String) -> Unit
    ) {
        val client = GeminiVisionClient(context)
        val result = client.analyzeScreenForAutoAnswer(screenBitmap)

        if (!result.questionDetected || result.correctOptionText.isNullOrEmpty()) {
            onResult(false, result.errorMessage ?: "Tidak menemukan pertanyaan aktif di layar.")
            return
        }

        Log.i(TAG, "Pertanyaan terdeteksi: ${result.questionText}")
        Log.i(TAG, "Jawaban benar menurut Gemini: ${result.correctOptionText}")

        val a11yService = AutonomousAccessibilityService.instance
        if (a11yService == null) {
            onResult(false, "Layanan Aksesibilitas belum diaktifkan oleh pengguna!")
            return
        }

        // Jalankan aksi klik otonom pada layar ponsel
        val clicked = a11yService.clickNodeByText(result.correctOptionText)
        if (clicked) {
            onResult(true, "Opsi '${result.correctOptionText}' berhasil dipilih! (${result.explanation})")
        } else {
            // Coba pencarian teks parsial jika nama opsi panjang
            val firstWords = result.correctOptionText.split(" ").take(3).joinToString(" ")
            val retryClicked = a11yService.clickNodeByText(firstWords)
            if (retryClicked) {
                onResult(true, "Opsi '$firstWords' berhasil dipilih otomatis.")
            } else {
                onResult(false, "Gagal mengklik opsi di layar. Elemen tidak terjangkau.")
            }
        }
    }
}