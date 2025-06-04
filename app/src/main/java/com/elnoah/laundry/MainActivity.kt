package com.elnoah.laundry

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.ImageView
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

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREF_NAME = "users_nama"
        private const val KEY_USER_NAME = "users_nama"
        private const val KEY_USER_ID = "users_phone"
        private const val KEY_LAST_ACTIVITY = "last_activity"
        private const val AUTO_LOGOUT_TIME = 30 * 60 * 1000L // 30 menit dalam milliseconds
    }

    private lateinit var database: DatabaseReference
    private lateinit var autoLogoutHandler: Handler
    private lateinit var autoLogoutRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize auto logout handler
        initializeAutoLogout()

        // Setup date
        val dateTextView: TextView = findViewById(R.id.date)
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate

        // TEST: Set user ID untuk testing (dalam kondisi nyata, ini dari login)
        saveCurrentUserId("621245788") // Ganti dengan ID user yang login

        // Setup default greeting dulu
        setupUserGreeting()

        // Sync nama dari Firebase Database
        syncUserNameFromFirebase()

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
        // Reset timer ketika activity di-resume
        resetAutoLogoutTimer()
    }

    override fun onPause() {
        super.onPause()
        // Update last activity time ketika activity di-pause
        updateLastActivityTime()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop auto logout timer
        stopAutoLogoutTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset timer setiap kali user berinteraksi dengan aplikasi
        resetAutoLogoutTimer()
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
        autoLogoutHandler.postDelayed(autoLogoutRunnable, AUTO_LOGOUT_TIME)
        android.util.Log.d("MainActivity", "Auto logout timer started (30 minutes)")
    }

    private fun resetAutoLogoutTimer() {
        // Update last activity time
        updateLastActivityTime()

        // Restart timer
        startAutoLogoutTimer()
        android.util.Log.d("MainActivity", "Auto logout timer reset")
    }

    private fun stopAutoLogoutTimer() {
        autoLogoutHandler.removeCallbacks(autoLogoutRunnable)
        android.util.Log.d("MainActivity", "Auto logout timer stopped")
    }

    private fun updateLastActivityTime() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
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
        // Clear user data
        logoutUser()

        // Redirect to login activity
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Function untuk cek apakah user perlu auto logout saat app dibuka kembali
    private fun checkAutoLogoutOnResume() {
        if (!isUserLoggedIn()) return

        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val lastActivityTime = sharedPref.getLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastActivityTime

        if (timeDifference >= AUTO_LOGOUT_TIME) {
            android.util.Log.d("MainActivity", "Auto logout triggered on resume")
            performAutoLogout()
        }
    }

    private fun setupUserGreeting() {
        val tvHello: TextView? = findViewById(R.id.tvhello)

        if (tvHello == null) {
            android.util.Log.e("MainActivity", "TextView tvhello tidak ditemukan!")
            return
        }

        // Ambil nama user dari SharedPreferences
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userName = sharedPref.getString(KEY_USER_NAME, "users")

        // Ambil nama depan saja (split berdasarkan spasi dan ambil yang pertama)
        val firstName = userName?.split(" ")?.firstOrNull() ?: "users"

        // Debug log
        android.util.Log.d("MainActivity", "SharedPref userName: '$userName'")
        android.util.Log.d("MainActivity", "First name: '$firstName'")

        // Set greeting text dengan nama depan saja + sapaan sesuai waktu
        val greetingText = getGreetingByTime() + ", $firstName!"
        tvHello.text = greetingText

        android.util.Log.d("MainActivity", "Greeting text set: '$greetingText'")
    }

    private fun getGreetingByTime(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..14 -> "Good Afternoon"
            in 15..17 -> "Good Evening"
            else -> "Good Night"
        }
    }

    private fun setupClickListeners() {
        // Main menu clicks dengan transisi layout
        findViewById<ConstraintLayout>(R.id.clPelanggan)?.setOnClickListener {
            showTransitionAndNavigate(DataPelanggan::class.java)
        }

        findViewById<ConstraintLayout>(R.id.clTransaksi)?.setOnClickListener {
            showTransitionAndNavigate(DataTransaksi::class.java)
        }

        findViewById<ConstraintLayout>(R.id.clLaporan)?.setOnClickListener {
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

    // Function untuk menyimpan ID user yang sedang login
    private fun saveCurrentUserId(userId: String) {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_USER_ID, userId)
            apply()
        }
        updateLastActivityTime()
    }

    // Function untuk menyimpan data login user (dipanggil dari LoginActivity)
    fun saveUserLogin(userId: String, userName: String) {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            apply()
        }
        updateLastActivityTime()
        android.util.Log.d("MainActivity", "User login saved: ID=$userId, Name=$userName")

        // Setup greeting setelah save user login
        setupUserGreeting()
    }

    // Function untuk mengambil nama user dari Firebase
    private fun syncUserNameFromFirebase() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userId = sharedPref.getString(KEY_USER_ID, null)

        android.util.Log.d("MainActivity", "Mencoba sync dengan User ID: $userId")

        if (userId.isNullOrEmpty()) {
            android.util.Log.d("MainActivity", "User ID tidak ditemukan")
            return
        }

        // Ambil data dari Firebase Realtime Database
        val userRef = database.child("users").child(userId)
        android.util.Log.d("MainActivity", "Firebase path: users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("MainActivity", "Firebase snapshot exists: ${snapshot.exists()}")
                android.util.Log.d("MainActivity", "Firebase snapshot value: ${snapshot.value}")

                if (snapshot.exists()) {
                    val userName = snapshot.child("nama").getValue(String::class.java)
                    android.util.Log.d("MainActivity", "Nama dari Firebase: '$userName'")

                    if (!userName.isNullOrEmpty()) {
                        // Simpan nama ke SharedPreferences
                        updateUserName(userName)
                        android.util.Log.d("MainActivity", "Nama berhasil disimpan: $userName")
                    } else {
                        android.util.Log.d("MainActivity", "Field 'nama' tidak ditemukan atau kosong")
                        // Set default greeting jika nama tidak ditemukan
                        setupUserGreeting()
                    }
                } else {
                    android.util.Log.d("MainActivity", "User dengan ID $userId tidak ditemukan di Firebase")
                    // Set default greeting jika user tidak ditemukan
                    setupUserGreeting()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainActivity", "Error mengambil data dari Firebase: ${error.message}")
                // Set default greeting jika error
                setupUserGreeting()
            }
        })
    }

    // Function untuk mengupdate nama user
    fun updateUserName(newName: String) {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_USER_NAME, newName)
            apply()
        }

        // Update greeting langsung setelah nama diupdate
        runOnUiThread {
            setupUserGreeting()
        }
    }

    // Function untuk cek apakah user sudah login
    fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userId = sharedPref.getString(KEY_USER_ID, null)
        return !userId.isNullOrEmpty()
    }

    // Function untuk logout user
    fun logoutUser() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_LAST_ACTIVITY)
            apply()
        }
        stopAutoLogoutTimer()
        android.util.Log.d("MainActivity", "User logged out")
    }
}