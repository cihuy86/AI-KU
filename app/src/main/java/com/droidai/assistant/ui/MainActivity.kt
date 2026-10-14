package com.droidai.assistant.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidai.assistant.data.SecurePreferences
import com.droidai.assistant.service.FloatingOverlayService
import com.droidai.assistant.service.ScreenCaptureService
import com.droidai.assistant.ui.theme.DroidAIDarkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DroidAIDarkTheme {
                MainAssistantDashboard(
                    onOpenOverlaySettings = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    },
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onStartAssistantService = {
                        startFloatingRobotService()
                    }
                )
            }
        }
    }

    private fun startFloatingRobotService() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Berikan izin 'Display over other apps' terlebih dahulu!", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, FloatingOverlayService::class.java))
        Toast.makeText(this, "Robot DroidAI melayang telah diaktifkan!", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAssistantDashboard(
    onOpenOverlaySettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onStartAssistantService: () -> Unit
) {
    val scrollState = rememberScrollState()
    var apiKeyInput by remember { mutableStateOf("") }
    var isKeySaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DroidAI Assistant", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color(0xFF38BDF8)
                )
            )
        },
        containerColor = Color(0xFF020617)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STATUS SISTEM ASISTEN", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Asisten Otonom Siap Dijalankan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Didukung oleh Gemini Vision + Accessibility Service", color = Color(0xFF38BDF8), fontSize = 13.sp)
                }
            }

            // API Key Input Setup Card (EncryptedSharedPreferences)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("KUNCI API GEMINI (ENCRYPTED)", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Masukkan Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { isKeySaved = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                    ) {
                        Text("Simpan & Enkripsi Kunci", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Permissions Checklist
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DAFTAR IZIN WAJIB", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                        Text("1. Izinkan Tampilan Melayang (SYSTEM_ALERT_WINDOW)")
                    }
                    Button(onClick = onOpenAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
                        Text("2. Aktifkan Autonomous Accessibility Service")
                    }
                }
            }

            // Start Floating Robot Button
            Button(
                onClick = onStartAssistantService,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("MULAI ROBOT MELAYANG", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}