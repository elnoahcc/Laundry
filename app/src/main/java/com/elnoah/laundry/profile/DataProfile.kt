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

    private lateinit var tvNamaLengkap: TextView
    private lateinit var tvNomorTelepon: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnSignOut: Button
    private lateinit var ivBack: ImageView

    private lateinit var database: DatabaseReference
    private lateinit var sessionManager: SessionManager
    private var currentUser: modeluser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_profile)

        // Hapus bagian ViewCompat yang bermasalah dulu
        // ViewCompat.setOnApplyWindowInsetsListener akan ditambah nanti jika diperlukan

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
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSignOut = findViewById(R.id.btnSignOut)
        ivBack = findViewById(R.id.ivBack)
    }

    private fun initComponents() {
        database = FirebaseDatabase.getInstance().reference
        sessionManager = SessionManager(this)
    }

    private fun checkLoginStatus() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun loadProfileData() {
        val phoneNumber = sessionManager.getUserPhone()

        phoneNumber?.let { phone ->
            val savedName = sessionManager.getUserName()
            val savedPassword = sessionManager.getUserPassword()

            if (savedName != null && savedPassword != null) {
                displayUserData(phone, savedName, savedPassword)
            } else {
                loadFromFirebase(phone)
            }
        } ?: run {
            Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
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

                        sessionManager.updateUserData(nama, password)
                        displayUserData(phoneNumber, nama, password)
                    }
                } else {
                    Toast.makeText(this@DataProfile, "Data user tidak ditemukan di server", Toast.LENGTH_SHORT).show()
                    tvNamaLengkap.text = "Data tidak tersedia"
                    tvPassword.text = "***"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataProfile, "Error: ${error.message}", Toast.LENGTH_SHORT).show()

                val savedName = sessionManager.getUserName() ?: "Error loading data"
                val savedPassword = sessionManager.getUserPassword() ?: ""
                displayUserData(phoneNumber, savedName, savedPassword)
            }
        })
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

        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigasi ke EditProfileActivity jika sudah tersedia
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
        sessionManager.logout()
        Toast.makeText(this, "Berhasil sign out", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // SessionManager sebagai inner class
    class SessionManager(context: Context) {

        private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        private val editor: SharedPreferences.Editor = sharedPref.edit()

        companion object {
            private const val PREF_NAME = "LaundryApp"
            private const val KEY_PHONE = "users_phone"
            private const val KEY_NAME = "users_name"
            private const val KEY_PASSWORD = "users_password"
            private const val KEY_IS_LOGGED_IN = "is_logged_in"
        }

        // Login user - simpan session
        fun createLoginSession(phoneNumber: String, nama: String, password: String) {
            editor.apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_PHONE, phoneNumber)
                putString(KEY_NAME, nama)
                putString(KEY_PASSWORD, password)
                apply()
            }
        }

        // Cek apakah user sudah login
        fun isLoggedIn(): Boolean {
            return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
        }

        // Get user data
        fun getUserPhone(): String? {
            return sharedPref.getString(KEY_PHONE, null)
        }

        fun getUserName(): String? {
            return sharedPref.getString(KEY_NAME, null)
        }

        fun getUserPassword(): String? {
            return sharedPref.getString(KEY_PASSWORD, null)
        }

        // Update user data
        fun updateUserData(nama: String, password: String) {
            editor.apply {
                putString(KEY_NAME, nama)
                putString(KEY_PASSWORD, password)
                apply()
            }
        }

        // Logout user - clear session
        fun logout() {
            editor.apply {
                clear()
                apply()
            }
        }

        // Get all user data as HashMap
        fun getUserDetails(): HashMap<String, String?> {
            return hashMapOf(
                KEY_PHONE to getUserPhone(),
                KEY_NAME to getUserName(),
                KEY_PASSWORD to getUserPassword()
            )
        }
    }

    // Function untuk dipanggil dari Login atau activity lain
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DataProfile::class.java)
            context.startActivity(intent)
        }
    }
}