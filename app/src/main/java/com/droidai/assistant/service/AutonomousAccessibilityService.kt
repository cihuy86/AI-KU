package com.droidai.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AutonomousAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "DroidAI_A11y"
        var instance: AutonomousAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        Log.d(TAG, "Autonomous Accessibility Service terhubung dan aktif!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Mendengarkan perubahan layar (misal: ganti soal, halaman formulir selesai dimuat)
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                // Notifikasi siap untuk analisis layar otonom
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service diinterupsi")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
    }

    /**
     * Mencari tombol atau opsi pilihan ganda berdasarkan teks dan melakukan klik otomatis.
     */
    fun clickNodeByText(text: String, exactMatch: Boolean = false): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        
        for (node in nodes) {
            val nodeText = node.text?.toString() ?: ""
            val matches = if (exactMatch) nodeText.equals(text, ignoreCase = true) else nodeText.contains(text, ignoreCase = true)
            if (matches) {
                if (performClickHierarchy(node)) {
                    Log.i(TAG, "Berhasil klik node dengan teks: $text")
                    return true
                }
            }
        }
        return false
    }

    /**
     * Klik elemen berdasarkan Resource ID (misal: 'submit_button', 'radio_option_b')
     */
    fun clickNodeById(viewId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        for (node in nodes) {
            if (performClickHierarchy(node)) {
                Log.i(TAG, "Berhasil klik node ID: $viewId")
                return true
            }
        }
        return false
    }

    /**
     * Mengetikkan teks secara otomatis pada EditText atau formulir input
     */
    fun setTextToInputNode(targetTextHint: String, valueToType: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetTextHint)
        for (node in nodes) {
            if (node.isEditable || node.className?.contains("EditText") == true) {
                val arguments = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, valueToType)
                }
                val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                if (success) {
                    Log.i(TAG, "Berhasil mengetik nilai pada input: $valueToType")
                    return true
                }
            }
        }
        return false
    }

    /**
     * Mengirim gestur Tap (Sentuhan jari buatan) pada koordinat piksel layar (X, Y)
     */
    fun dispatchTap(x: Float, y: Float, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Gestur tap pada ($x, $y) berhasil!")
                callback?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Gestur tap pada ($x, $y) dibatalkan!")
                callback?.invoke(false)
            }
        }, null)
    }

    /**
     * Melakukan scroll ke bawah jika soal berikutnya berada di luar layar
     */
    fun scrollForward(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    private fun performClickHierarchy(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        // Jika tidak clickable, gunakan koordinat tengah node untuk dispatch tap
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (!bounds.isEmpty) {
            dispatchTap(bounds.centerX().toFloat(), bounds.centerY().toFloat())
            return true
        }
        return false
    }
}