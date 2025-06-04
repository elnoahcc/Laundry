package com.elnoah.laundry.Authorize

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elnoah.laundry.R

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 3000L
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        checkRequirements()
    }

    private fun checkRequirements() {
        when {
            !isInternetAvailable() -> showInternetDialog()
            !isBluetoothEnabled() -> showBluetoothDialog()
            else -> continueToLogin()
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
            .setNegativeButton(getString(R.string.remindmelater)) { _, _ -> continueToLogin() }
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

    private fun continueToLogin() {
        handler.postDelayed({
            startActivity(Intent(this@SplashActivity, Login::class.java))
            finish()
        }, SPLASH_DURATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}