package com.elnoah.laundry.Authorize

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elnoah.laundry.MainActivity
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeluser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var edtPhone: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    private val TAG = "LoginActivity"

    // Constants
    private val PREF_NAME = "LoginPrefs"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_LOGIN_TIME = "loginTime"
    private val KEY_USER_NAME = "userName"
    private val KEY_USER_PHONE = "userPhone"
    private val SESSION_DURATION_MS = 24 * 60 * 60 * 1000 // 1 hari dalam milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Cek apakah user sudah login dan session masih berlaku
        if (isLoggedIn() && !isSessionExpired()) {
            redirectToMainActivity()
            return
        } else {
            clearLoginSession() // hapus session jika kadaluarsa
        }

        setContentView(R.layout.activity_login)

        edtPhone = findViewById(R.id.edtPhone)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance().getReference("users")

        btnLogin.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, (getString(R.string.isinotelppw)), Toast.LENGTH_SHORT).show()
            } else {
                loginUser(phone, password)
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }

    private fun loginUser(phone: String, password: String) {
        database.child(phone).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(modeluser::class.java)
                if (user != null) {
                    if (user.password == password) {
                        Toast.makeText(this, (getString(R.string.loginberhasil)), Toast.LENGTH_SHORT).show()
                        saveLoginSession(user.nama ?: "", phone)
                        redirectToMainActivity()
                    } else {
                        Toast.makeText(this, (getString(R.string.passwordsalah)), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, (getString(R.string.datapenggunatidakvalid)), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, (getString(R.string.passwordsalah)), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Any failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLoginSession(userName: String, userPhone: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_PHONE, userPhone)
            apply()
        }
        Log.d(TAG, "Login session saved")
    }

    private fun clearLoginSession() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Login session cleared")
    }

    private fun redirectToMainActivity() {
        val userName = sharedPreferences.getString(KEY_USER_NAME, "")
        val userPhone = sharedPreferences.getString(KEY_USER_PHONE, "")

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("nama", userName)
        intent.putExtra("phone", userPhone)
        startActivity(intent)
        finish()
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun isSessionExpired(): Boolean {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        return (currentTime - loginTime) > SESSION_DURATION_MS
    }

    // Method logout (panggil dari MainActivity jika perlu)
    fun logout() {
        clearLoginSession()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
