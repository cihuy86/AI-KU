package com.droidai.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.droidai.assistant.R
import com.droidai.assistant.ai.AutoAnswerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var controlPanelView: View
    private lateinit var robotAvatar: ImageView

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isPanelOpen = false

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupFloatingRobotView()
        setupControlPanelView()
    }

    private fun setupFloatingRobotView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_robot, null)
        robotAvatar = floatingView.findViewById(R.id.imgRobotHead)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 400
        }

        floatingView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    // Jika pergerakan kurang dari ambang batas, anggap sebagai Klik
                    if (diffX < 15 && diffY < 15) {
                        toggleControlPanel()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingView, params)
    }

    private fun setupControlPanelView() {
        controlPanelView = LayoutInflater.from(this).inflate(R.layout.layout_floating_control_panel, null)
        controlPanelView.visibility = View.GONE

        // Pasang event listener untuk tombol panel: Auto-Answer, Scan Kamera, dsb.
        val btnAutoAnswer = controlPanelView.findViewById<View>(R.id.btnAutoAnswer)
        btnAutoAnswer?.setOnClickListener {
            serviceScope.launch {
                triggerAutoAnswer()
            }
        }
    }

    private fun toggleControlPanel() {
        isPanelOpen = !isPanelOpen
        if (isPanelOpen) {
            controlPanelView.visibility = View.VISIBLE
            setRobotEyeState(RobotEyeState.ACTIVE_THINKING)
        } else {
            controlPanelView.visibility = View.GONE
            setRobotEyeState(RobotEyeState.IDLE)
        }
    }

    private suspend fun triggerAutoAnswer() {
        setRobotEyeState(RobotEyeState.SCANNING)
        Toast.makeText(this, "DroidAI: Menganalisis pertanyaan di layar...", Toast.LENGTH_SHORT).show()
        
        val bitmap = ScreenCaptureService.instance?.captureCurrentScreenBitmap()
        if (bitmap == null) {
            Toast.makeText(this, "Aktifkan Screen Capture Service terlebih dahulu!", Toast.LENGTH_SHORT).show()
            setRobotEyeState(RobotEyeState.IDLE)
            return
        }

        AutoAnswerEngine.analyzeAndExecuteAnswer(this, bitmap) { success, message ->
            if (success) {
                setRobotEyeState(RobotEyeState.SUCCESS)
                Toast.makeText(this, "Berhasil: $message", Toast.LENGTH_SHORT).show()
            } else {
                setRobotEyeState(RobotEyeState.IDLE)
                Toast.makeText(this, "Gagal: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setRobotEyeState(state: RobotEyeState) {
        when (state) {
            RobotEyeState.IDLE -> robotAvatar.setImageResource(R.drawable.robot_eyes_cyan_idle)
            RobotEyeState.SCANNING -> robotAvatar.setImageResource(R.drawable.robot_eyes_scanning_glow)
            RobotEyeState.ACTIVE_THINKING -> robotAvatar.setImageResource(R.drawable.robot_eyes_thinking_pulse)
            RobotEyeState.SUCCESS -> robotAvatar.setImageResource(R.drawable.robot_eyes_success_green)
        }
    }

    enum class RobotEyeState {
        IDLE, SCANNING, ACTIVE_THINKING, SUCCESS
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
        if (::controlPanelView.isInitialized) windowManager.removeView(controlPanelView)
    }
}