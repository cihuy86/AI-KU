# DroidAI - Native Android AI Assistant

Aplikasi Android Native otonom yang memadukan **MediaProjection API** (pembacaan layar), **Accessibility Service** (navigasi & sentuhan otonom), dan **Gemini Vision AI** untuk menjawab soal ujian / kuis (Google Form, Quizizz) dan otomatisasi layar ponsel.

## Fitur Utama
1. **Screen Sharing & Capture**: MediaProjection API membaca konten layar secara real-time.
2. **Autonomous Accessibility Service**: Mengetuk tombol pilihan jawaban, mengisi teks, dan scroll tanpa campur tangan tangan pengguna.
3. **Gemini Vision Integration**: Mengirim tangkapan layar ke Gemini 1.5/2.5 Flash untuk analisis soal dan penentuan jawaban akurat.
4. **Auto-Answer Mode**: Otomatis mendeteksi soal pada Google Form atau kuis, mencari jawaban di Gemini, dan memilih opsi yang benar.
5. **Camera Scan Mode**: Memindai lembar soal fisik kertas via CameraX dan OCR Gemini.
6. **Floating Robot Avatar**: Maskot kepala robot melayang (SYSTEM_ALERT_WINDOW) yang dapat digeser (draggable) dengan mata LED beranimasi.
7. **Encrypted Storage**: Penyimpanan Gemini API Key aman menggunakan `EncryptedSharedPreferences` (AES-256 GCM).

## Cara Menjalankan di Android Studio
1. Ekstrak file zip ini ke komputer Anda.
2. Buka **Android Studio** -> Pilih **File** > **Open** -> Arahkan ke folder project ini.
3. Tunggu hingga Gradle Sync selesai.
4. Hubungkan ponsel Android via USB (dengan USB Debugging aktif).
5. Klik tombol **Run 'app'** (Shift + F10).

## Aktivasi Izin melalui ADB (Opsional & Cepat):
```bash
# Berikan izin Display Over Other Apps (Overlay Melayang):
adb shell appops set com.droidai.assistant SYSTEM_ALERT_WINDOW allow

# Aktifkan Accessibility Service secara langsung:
adb shell settings put secure enabled_accessibility_services com.droidai.assistant/.service.AutonomousAccessibilityService
adb shell settings put secure accessibility_enabled 1
```
