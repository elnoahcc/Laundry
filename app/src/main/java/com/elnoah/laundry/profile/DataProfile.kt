package com.elnoah.laundry.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.Authorize.Login
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeluser
import com.google.firebase.database.*

class DataProfile : AppCompatActivity() {

    companion object {
        // GUNAKAN KONSTANTA YANG SAMA DENGAN MainActivity
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGIN_TIME = "loginTime"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_PHONE = "userPhone"
        private const val SESSION_DURATION_MS = 30 * 60 * 1000 // 30 menit

        // Function untuk dipanggil dari Login atau activity lain
        fun start(context: Context) {
            val intent = Intent(context, DataProfile::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var tvNamaLengkap: TextView
    private lateinit var tvNomorTelepon: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnSignOut: Button
    private lateinit var ivBack: ImageView

    private lateinit var database: DatabaseReference
    private var currentUser: modeluser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_profile)

        initViews()
        initComponents()
        checkLoginStatus()
        loadProfileData()
        setupClickListeners()
    }

    private fun initViews() {
        tvNamaLengkap = findViewById(R.id.tvNamaLengkap)
        tvNomorTelepon = findViewById(R.id.tvNomorTelepon)
        tvPassword = findViewById(R.id.tvPassword)
        btnSignOut = findViewById(R.id.btnSignOut)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun initComponents() {
        database = FirebaseDatabase.getInstance().reference
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionExpired = (currentTime - loginTime) > SESSION_DURATION_MS

        if (!isLoggedIn || sessionExpired) {
            Toast.makeText(this, "Sesi telah berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        // Clear login session
        clearLoginSession()

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun clearLoginSession() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val phoneNumber = sharedPreferences.getString(KEY_USER_PHONE, null)
        val userName = sharedPreferences.getString(KEY_USER_NAME, null)

        android.util.Log.d("DataProfile", "Phone: $phoneNumber, Name: $userName")

        phoneNumber?.let { phone ->
            if (userName != null && userName.isNotEmpty()) {
                // Tampilkan data dari SharedPreferences dulu
                displayUserData(phone, userName, "***")

                // Kemudian load dari Firebase untuk update data terbaru
                loadFromFirebase(phone)
            } else {
                // Jika tidak ada nama di SharedPreferences, load dari Firebase
                loadFromFirebase(phone)
            }
        } ?: run {
            Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun loadFromFirebase(phoneNumber: String) {
        val phoneKey = getPhoneKey(phoneNumber)

        tvNamaLengkap.text = "Loading..."
        tvPassword.text = "Loading..."
        tvNomorTelepon.text = phoneNumber

        database.child("users").child(phoneKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(modeluser::class.java)

                    currentUser?.let { user ->
                        val nama = user.nama ?: "Nama tidak tersedia"
                        val password = user.password ?: ""

                        // Update SharedPreferences dengan data terbaru
                        updateUserDataInPrefs(nama)
                        displayUserData(phoneNumber, nama, password)
                    }
                } else {
                    Toast.makeText(this@DataProfile, "Data user tidak ditemukan di server", Toast.LENGTH_SHORT).show()

                    // Gunakan data dari SharedPreferences jika Firebase gagal
                    val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    val savedName = sharedPreferences.getString(KEY_USER_NAME, "Data tidak tersedia")
                    displayUserData(phoneNumber, savedName ?: "Data tidak tersedia", "***")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataProfile, "Error: ${error.message}", Toast.LENGTH_SHORT).show()

                // Gunakan data dari SharedPreferences jika Firebase error
                val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val savedName = sharedPreferences.getString(KEY_USER_NAME, "Error loading data")
                displayUserData(phoneNumber, savedName ?: "Error loading data", "***")
            }
        })
    }

    private fun updateUserDataInPrefs(nama: String) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, nama)
            apply()
        }
    }

    private fun displayUserData(phoneNumber: String, nama: String, password: String) {
        tvNamaLengkap.text = nama
        tvNomorTelepon.text = phoneNumber
        tvPassword.text = censorPassword(password)
    }

    private fun censorPassword(password: String): String {
        return if (password.length > 3) {
            "***" + password.substring(3)
        } else {
            "***"
        }
    }

    private fun getPhoneKey(phoneNumber: String): String {
        return phoneNumber.replace("+", "").replace("-", "").replace(" ", "")
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener {
            finish()
        }


        btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { _, _ ->
                signOut()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun signOut() {
        // Clear login session menggunakan method yang sama dengan MainActivity
        clearLoginSession()

        Toast.makeText(this, "Berhasil sign out", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}