package com.elnoah.laundry.Authorize

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elnoah.laundry.MainActivity
import com.elnoah.laundry.R

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 3000L
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001
    }

    private val handler = Handler(Looper.getMainLooper())

    // Login session constants (sama seperti di Login.kt)
    private val PREF_NAME = "LoginPrefs"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_LOGIN_TIME = "loginTime"
    private val KEY_USER_NAME = "userName"
    private val KEY_USER_PHONE = "userPhone"
    private val SESSION_DURATION_MS = 30 * 60 * 1000 // 30 menit dalam milliseconds

    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        checkRequirements()
    }

    private fun checkRequirements() {
        when {
            !isInternetAvailable() -> showInternetDialog()
            !isBluetoothEnabled() -> showBluetoothDialog()
            else -> continueAfterCheck()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    private fun showInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.koneksiinternet))
            .setMessage(getString(R.string.turnoninternet))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.cobalagi)){ _, _ -> checkRequirements() }
            .show()
    }

    private fun showBluetoothDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.bluetoothoff))
            .setMessage(getString(R.string.reasonbluethoot))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.nyalakan)) { _, _ ->
                if (checkBluetoothPermission()) {
                    enableBluetooth()
                } else {
                    requestBluetoothPermission()
                }
            }
            .setNegativeButton(getString(R.string.remindmelater)) { _, _ -> continueAfterCheck() }
            .show()
    }

    private fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }

    private fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        try {
            bluetoothAdapter?.enable()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        handler.postDelayed({ checkRequirements() }, 2000)
    }

    // Method baru untuk mengecek login session
    private fun continueAfterCheck() {
        handler.postDelayed({
            // Cek apakah user sudah login dan session masih berlaku
            if (isLoggedIn() && !isSessionExpired()) {
                Log.d(TAG, "User masih login, redirect ke MainActivity")
                redirectToMainActivity()
            } else {
                Log.d(TAG, "User belum login atau session expired, redirect ke Login")
                if (isLoggedIn()) {
                    clearLoginSession() // hapus session jika kadaluarsa
                }
                redirectToLogin()
            }
        }, SPLASH_DURATION)
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun isSessionExpired(): Boolean {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionExpired = (currentTime - loginTime) > SESSION_DURATION_MS

        if (sessionExpired) {
            Log.d(TAG, "Session expired. Login time: $loginTime, Current time: $currentTime, Duration: ${(currentTime - loginTime) / 1000 / 60} minutes")
        }

        return sessionExpired
    }

    private fun clearLoginSession() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Login session cleared due to expiration")
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

    private fun redirectToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}