package com.elnoah.laundry

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.Layanan.DataLayanan
import com.elnoah.laundry.Transaksi.DataTransaksi
import com.elnoah.laundry.laporan.DataLaporan
import com.elnoah.laundry.Tambahan.DataTambahan
import com.elnoah.laundry.cabang.DataCabang
import com.elnoah.laundry.pegawai.DataPegawai
import com.elnoah.laundry.pelanggan.DataPelanggan
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.elnoah.laundry.Authorize.Login
import com.elnoah.laundry.profile.DataProfile
import android.content.Context

class MainActivity : AppCompatActivity() {

    companion object {
        // Gunakan konstanta yang sama dengan Login.kt
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGIN_TIME = "loginTime"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_PHONE = "userPhone"
        private const val SESSION_DURATION_MS = 30 * 60 * 1000 // 30 menit dalam milliseconds
    }

    private lateinit var database: DatabaseReference
    private lateinit var autoLogoutHandler: Handler
    private lateinit var autoLogoutRunnable: Runnable
    private lateinit var dateTimeHandler: Handler
    private lateinit var dateTimeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize auto logout handler
        initializeAutoLogout()

        // Setup date and time with real-time update
        setupDateTimeDisplay()

        // Ambil data user dari Intent (yang dikirim dari Login/SplashActivity)
        val userName = intent.getStringExtra("nama") ?: ""
        val userPhone = intent.getStringExtra("phone") ?: ""

        android.util.Log.d("MainActivity", "Data dari Intent - Nama: '$userName', Phone: '$userPhone'")

        // Setup user greeting dengan data yang ada
        setupUserGreeting(userName)

        // Setup click listeners dengan transisi layout
        setupClickListeners()

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Start auto logout timer
        startAutoLogoutTimer()

        // Debug: Cek apakah TextView ada
        val tvHello: TextView? = findViewById(R.id.tvhello)
        if (tvHello == null) {
            android.util.Log.e("MainActivity", "TextView tvhello tidak ditemukan di layout!")
        } else {
            android.util.Log.d("MainActivity", "TextView tvhello ditemukan")
        }
    }

    override fun onResume() {
        super.onResume()
        // Check session validity
        checkSessionValidity()
        // Reset timer ketika activity di-resume
        resetAutoLogoutTimer()
        // Start date time update
        startDateTimeUpdate()
    }

    override fun onPause() {
        super.onPause()
        // Stop date time update
        stopDateTimeUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop auto logout timer
        stopAutoLogoutTimer()
        // Stop date time update
        stopDateTimeUpdate()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset timer setiap kali user berinteraksi dengan aplikasi
        resetAutoLogoutTimer()
    }

    private fun setupDateTimeDisplay() {
        // Initialize date time handler
        dateTimeHandler = Handler(Looper.getMainLooper())
        dateTimeRunnable = Runnable {
            updateDateTime()
            // Update setiap 1 detik
            dateTimeHandler.postDelayed(dateTimeRunnable, 1000)
        }

        // Initial update
        updateDateTime()

        // Start continuous update
        startDateTimeUpdate()
    }

    private fun updateDateTime() {
        val dateTextView: TextView = findViewById(R.id.date)

        // Format: Hari, dd MMMM yyyy - HH:mm
        // Contoh: Rabu, 04 Juni 2025 - 14:30
        val dateTimeFormat = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale.getDefault())
        val currentDateTime = dateTimeFormat.format(Date())

        dateTextView.text = currentDateTime
    }

    private fun startDateTimeUpdate() {
        stopDateTimeUpdate() // Stop any existing update
        dateTimeHandler.postDelayed(dateTimeRunnable, 1000)
    }

    private fun stopDateTimeUpdate() {
        if (::dateTimeHandler.isInitialized) {
            dateTimeHandler.removeCallbacks(dateTimeRunnable)
        }
    }

    private fun initializeAutoLogout() {
        autoLogoutHandler = Handler(Looper.getMainLooper())
        autoLogoutRunnable = Runnable {
            performAutoLogout()
        }
    }

    private fun startAutoLogoutTimer() {
        // Stop timer sebelumnya jika ada
        stopAutoLogoutTimer()

        // Start timer baru
        autoLogoutHandler.postDelayed(autoLogoutRunnable, SESSION_DURATION_MS.toLong())
        android.util.Log.d("MainActivity", "Auto logout timer started (30 minutes)")
    }

    private fun resetAutoLogoutTimer() {
        // Update login time in SharedPreferences (reset session)
        updateLoginTime()

        // Restart timer
        startAutoLogoutTimer()
        android.util.Log.d("MainActivity", "Auto logout timer reset")
    }

    private fun stopAutoLogoutTimer() {
        if (::autoLogoutHandler.isInitialized) {
            autoLogoutHandler.removeCallbacks(autoLogoutRunnable)
        }
        android.util.Log.d("MainActivity", "Auto logout timer stopped")
    }

    private fun updateLoginTime() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
    }

    private fun performAutoLogout() {
        android.util.Log.d("MainActivity", "Performing auto logout due to inactivity")

        // Show logout dialog
        showAutoLogoutDialog()
    }

    private fun showAutoLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Sesi Berakhir")
            .setMessage("Anda telah tidak aktif selama 30 menit. Untuk keamanan, Anda akan logout otomatis.")
            .setPositiveButton("OK") { _, _ ->
                executeLogout()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun executeLogout() {
        // Clear login session
        clearLoginSession()

        // Redirect to login activity
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkSessionValidity() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionExpired = (currentTime - loginTime) > SESSION_DURATION_MS

        if (!isLoggedIn || sessionExpired) {
            android.util.Log.d("MainActivity", "Session invalid, redirecting to login")
            executeLogout()
        }
    }

    private fun clearLoginSession() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        android.util.Log.d("MainActivity", "Login session cleared")
    }

    private fun setupUserGreeting(userName: String = "") {
        val tvHello: TextView? = findViewById(R.id.tvhello)

        if (tvHello == null) {
            android.util.Log.e("MainActivity", "TextView tvhello tidak ditemukan!")
            return
        }

        var displayName = userName

        // Jika nama kosong, coba ambil dari SharedPreferences
        if (displayName.isEmpty()) {
            val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            displayName = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        }

        // Ambil nama depan saja (split berdasarkan spasi dan ambil yang pertama)
        val firstName = if (displayName.isNotEmpty()) {
            displayName.split(" ").firstOrNull() ?: "User"
        } else {
            "User"
        }

        // Debug log
        android.util.Log.d("MainActivity", "Display name: '$displayName'")
        android.util.Log.d("MainActivity", "First name: '$firstName'")

        // Set greeting text dengan nama depan saja + sapaan sesuai waktu
        val greetingText = getGreetingByTime() + ", $firstName!"
        tvHello.text = greetingText

        android.util.Log.d("MainActivity", "Greeting text set: '$greetingText'")
    }

    private fun getGreetingByTime(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> getString(R.string.selamatpagi)
            in 12..14 -> getString(R.string.selamatsiang)
            in 15..17 -> getString(R.string.selamatsore)
            else -> getString(R.string.selamatmalam)
        }
    }

    private fun setupClickListeners() {
        // Main menu clicks dengan transisi layout - FIXED: Gunakan LinearLayout bukan ConstraintLayout
        findViewById<LinearLayout>(R.id.clPelanggan)?.setOnClickListener {
            showTransitionAndNavigate(DataPelanggan::class.java)
        }

        findViewById<LinearLayout>(R.id.clTransaksi)?.setOnClickListener {
            showTransitionAndNavigate(DataTransaksi::class.java)
        }

        findViewById<LinearLayout>(R.id.clLaporan)?.setOnClickListener {
            showTransitionAndNavigate(DataLaporan::class.java)
        }

        // Secondary menu clicks dengan transisi layout
        findViewById<CardView>(R.id.cardPegawai)?.setOnClickListener {
            showTransitionAndNavigate(DataPegawai::class.java)
        }

        findViewById<CardView>(R.id.cardLayanan)?.setOnClickListener {
            showTransitionAndNavigate(DataLayanan::class.java)
        }

        findViewById<CardView>(R.id.cardCabang)?.setOnClickListener {
            showTransitionAndNavigate(DataCabang::class.java)
        }

        // Untuk CLtambahan, gunakan ConstraintLayout karena di XML memang ConstraintLayout
        findViewById<ConstraintLayout>(R.id.CLtambahan)?.setOnClickListener {
            showTransitionAndNavigate(DataTambahan::class.java)
        }

        findViewById<CardView>(R.id.cardAkun)?.setOnClickListener {
            showTransitionAndNavigate(DataProfile::class.java)
        }

        findViewById<CardView>(R.id.cardPrinter)?.setOnClickListener {
            // TODO: Implementasi DataPrinter - untuk sementara hanya transisi
            showTransitionOnly()
        }
    }

    // Method untuk menampilkan transisi layout dan navigasi ke activity
    private fun showTransitionAndNavigate(targetActivity: Class<*>) {
        // Inflate transisi layout
        val inflater = LayoutInflater.from(this)
        val transitionOverlay = inflater.inflate(R.layout.transisi_layout, null)

        // Set layout params untuk fullscreen
        transitionOverlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Tambahkan overlay ke root layout
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        rootLayout.addView(transitionOverlay)

        // Get screen height untuk animasi
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Set initial state - posisi di atas layar (seperti korden tertutup)
        transitionOverlay.translationY = -screenHeight
        transitionOverlay.alpha = 1f

        // Animasi korden turun dari atas (slide down)
        transitionOverlay.animate()
            .translationY(0f) // Turun ke posisi normal
            .setDuration(300)
            .withEndAction {
                // Delay singkat untuk efek transisi (korden tertutup)
                Handler(Looper.getMainLooper()).postDelayed({
                    // Navigate to target activity DULU
                    val intent = Intent(this@MainActivity, targetActivity)
                    startActivity(intent)

                    // Kemudian animasi korden naik kembali
                    transitionOverlay.animate()
                        .translationY(-screenHeight)
                        .setDuration(300)
                        .withEndAction {
                            // Remove overlay
                            rootLayout.removeView(transitionOverlay)
                        }

                    // Override transition setelah navigate
                    overridePendingTransition(0, 0) // No default transition
                }, 200) // Delay lebih singkat
            }
    }

    // Method untuk transisi saja (untuk menu yang belum ada activity-nya)
    private fun showTransitionOnly() {
        // Inflate transisi layout
        val inflater = LayoutInflater.from(this)
        val transitionOverlay = inflater.inflate(R.layout.transisi_layout, null)

        // Set layout params untuk fullscreen
        transitionOverlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Tambahkan overlay ke root layout
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        rootLayout.addView(transitionOverlay)

        // Get screen height untuk animasi
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Set initial state - posisi di atas layar (seperti korden tertutup)
        transitionOverlay.translationY = -screenHeight
        transitionOverlay.alpha = 1f

        // Animasi korden turun dari atas (slide down)
        transitionOverlay.animate()
            .translationY(0f) // Turun ke posisi normal
            .setDuration(300)
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    // Animasi korden naik kembali (slide up)
                    transitionOverlay.animate()
                        .translationY(-screenHeight)
                        .setDuration(300)
                        .withEndAction {
                            rootLayout.removeView(transitionOverlay)
                        }
                }, 300) // Delay lebih singkat
            }
    }
}