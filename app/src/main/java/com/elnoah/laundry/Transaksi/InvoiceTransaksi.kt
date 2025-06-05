package com.elnoah.laundry.Transaksi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import com.google.firebase.database.FirebaseDatabase
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.DataTambahanAdapter
import com.elnoah.laundry.modeldata.modelinvoice
import com.elnoah.laundry.modeldata.modellaporan
import com.elnoah.laundry.modeldata.modeltransaksitambahan
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class InvoiceTransaksi : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHP: TextView
    private var tvStatus: TextView? = null
    private lateinit var tvLayananUtama: TextView
    private lateinit var tvHargaLayanan: TextView
    private var rvTambahan: RecyclerView? = null
    private lateinit var tvSubtotalTambahan: TextView
    private lateinit var tvTotalBayar: TextView
    private lateinit var btnCetak: Button
    private lateinit var btnKirimWhatsapp: Button

    private val listTambahan = ArrayList<modeltransaksitambahan>()
    private var adapter: DataTambahanAdapter? = null

    // Property untuk menyimpan nomor HP pelanggan
    private var noHPPelanggan: String = ""

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val printerMAC = "DC:0D:51:A7:FF:7A"
    private val printerUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 100
        private const val TAG = "InvoiceTransaksi"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_transaksi)

        // Debug semua extra yang diterima
        debugIntentExtras()

        initializeViews()
        initializeBluetooth()
        requestBluetoothPermissions()
        setupRecyclerView()
        loadDataFromIntent()
        setupButtons()

        // Setup action bar untuk kembali ke DataTransaksi
        setupActionBar()

        // Setup modern back press handling
        setupBackPressHandler()

        // ===== TAMBAHAN BARU: Auto-save ke laporan saat invoice dibuat =====
        Handler(Looper.getMainLooper()).postDelayed({
            autoSaveLaporan()
        }, 1000) // Delay 1 detik untuk memastikan semua data sudah ter-load
    }

    private fun setupBackPressHandler() {
        // Modern way untuk handle back press (Available since AndroidX Activity 1.0.0)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToDataTransaksi()
            }
        })
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Invoice Transaksi"
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateBackToDataTransaksi()
        return true
    }

    private fun navigateBackToDataTransaksi() {
        try {
            // Cari activity DataTransaksi di stack dan kembali ke sana
            val intent = Intent()
            intent.setClassName(this, "com.elnoah.laundry.MainActivity")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to Home", e)
            // Fallback: gunakan finish() biasa
            finish()
        }
    }

    private fun debugIntentExtras() {
        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "=== DEBUG INTENT EXTRAS ===")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(TAG, "Key: $key = Value: $value")
            }
            Log.d(TAG, "=== END DEBUG EXTRAS ===")
        } else {
            Log.d(TAG, "No extras found in intent")
        }
    }

    private fun initializeViews() {
        try {
            tvTanggal = findViewById(R.id.tv_tanggal_invoice)
            tvIdTransaksi = findViewById(R.id.tv_id_transaksi)
            tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan_invoice)
            tvNoHP = findViewById(R.id.tv_no_hp_invoice)
            tvLayananUtama = findViewById(R.id.tv_nama_layanan_invoice)
            tvHargaLayanan = findViewById(R.id.tv_harga_layanan_invoice)
            tvSubtotalTambahan = findViewById(R.id.tv_tambahan_invoice)
            btnCetak = findViewById(R.id.btn_cetak_invoice)
            btnKirimWhatsapp = findViewById(R.id.btn_kirim_wa)

            // Coba cari tvStatus, jika tidak ada biarkan null
            tvStatus = findViewById(R.id.tvStatus)

            // Coba cari RecyclerView, jika tidak ada biarkan null
            rvTambahan = findViewById(R.id.rv_tambahan_konfirmasi)

            // Cari tvTotalBayar dengan ID yang ada di layout saat ini
            tvTotalBayar = findViewById(R.id.tv_total_bayar)
                ?: findViewById(R.id.tv_metode_pembayaran_invoice)
                        ?: throw IllegalStateException("TextView untuk total bayar tidak ditemukan")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            showToast("Error initializing views: ${e.message}")
            finish()
        }
    }

    private fun initializeBluetooth() {
        try {
            bluetoothAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager.adapter
            } else {
                @Suppress("DEPRECATION")
                BluetoothAdapter.getDefaultAdapter()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException initializing Bluetooth", e)
            showToast("Tidak dapat mengakses Bluetooth: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Bluetooth", e)
            showToast("Error initializing Bluetooth: ${e.message}")
        }
    }

    // PERBAIKAN: Fungsi yang lebih komprehensif untuk cek permission
    private fun hasAllBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ memerlukan BLUETOOTH_CONNECT dan BLUETOOTH_SCAN
            val connectPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            val scanPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            connectPermission && scanPermission
        } else {
            // Android 11 dan dibawah
            val bluetoothPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED

            val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED

            bluetoothPermission && bluetoothAdminPermission
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Untuk Android dibawah 12, cek permission BLUETOOTH
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    private fun showPermissionSettingsPrompt() {
        showToast("Harap aktifkan izin Bluetooth di pengaturan aplikasi")
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        rvTambahan?.let { recyclerView ->
            adapter = DataTambahanAdapter(listTambahan)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }
    }

    private fun loadDataFromIntent() {
        try {
            // Coba ambil data sebagai object invoice dulu (untuk backward compatibility)
            val invoice = intent.getSerializableExtra("invoice") as? modelinvoice

            if (invoice != null) {
                Log.d(TAG, "Loading data from invoice object")
                loadFromInvoiceObject(invoice)
            } else {
                Log.d(TAG, "Loading data from individual extras")
                loadFromIndividualData()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading data from intent", e)
            showToast("Error loading data: ${e.message}")
            finish()
        }
    }

    private fun loadFromInvoiceObject(invoice: modelinvoice) {
        val tanggalTerdaftarStr = formatTimestamp(invoice.tanggalTerdaftar)

        tvTanggal.text = tanggalTerdaftarStr
        tvIdTransaksi.text = invoice.idTransaksi ?: "-"
        tvNamaPelanggan.text = invoice.namaPelanggan ?: "-"
        tvStatus?.text = invoice.status ?: "-"
        tvLayananUtama.text = invoice.namaLayanan ?: "-"

        // Ambil nomor HP dengan berbagai kemungkinan key
        noHPPelanggan = invoice.noHPPelanggan
            ?: intent.getStringExtra("noHP")
                    ?: intent.getStringExtra("noHPPelanggan")
                    ?: intent.getStringExtra("phoneNumber")
                    ?: intent.getStringExtra("phone")
                    ?: ""

        Log.d(TAG, "NoHP from invoice object: '$noHPPelanggan'")
        tvNoHP.text = "No. HP: ${if (noHPPelanggan.isNotEmpty()) noHPPelanggan else "Tidak tersedia"}"

        @Suppress("UNCHECKED_CAST")
        val tambahan = intent.getSerializableExtra("layanan") as? ArrayList<modeltransaksitambahan> ?: arrayListOf()

        listTambahan.clear()
        listTambahan.addAll(tambahan)
        adapter?.notifyDataSetChanged()

        val subtotalTambahan = tambahan.sumOf { layanan ->
            layanan.hargaLayanan?.toIntOrNull() ?: 0
        }

        val totalBayar = invoice.totalBayar ?: 0
        val hargaLayananUtama = totalBayar - subtotalTambahan

        tvHargaLayanan.text = formatCurrency(hargaLayananUtama)
        tvSubtotalTambahan.text = formatCurrency(subtotalTambahan)
        tvTotalBayar.text = formatCurrency(totalBayar)
    }

    private fun loadFromIndividualData() {
        // Ambil data individual dari KonfirmasiTransaksi dengan berbagai kemungkinan key
        val nama = intent.getStringExtra("namaPelanggan")
            ?: intent.getStringExtra("nama")
            ?: intent.getStringExtra("customerName")
            ?: "-"

        // Coba berbagai kemungkinan key untuk nomor HP
        noHPPelanggan = intent.getStringExtra("noHP")
            ?: intent.getStringExtra("noHPPelanggan")
                    ?: intent.getStringExtra("phoneNumber")
                    ?: intent.getStringExtra("phone")
                    ?: intent.getStringExtra("nomorHP")
                    ?: intent.getStringExtra("hp")
                    ?: ""

        Log.d(TAG, "NoHP from individual data: '$noHPPelanggan'")

        val layanan = intent.getStringExtra("namaLayanan")
            ?: intent.getStringExtra("layanan")
            ?: "-"

        val harga = intent.getStringExtra("hargaLayanan")
            ?: intent.getStringExtra("harga")
            ?: "0"

        val totalHarga = intent.getIntExtra("totalHarga", 0)
        val metodePembayaran = intent.getStringExtra("metodePembayaran")
            ?: intent.getStringExtra("metode")
            ?: "-"

        val tambahan = intent.getSerializableExtra("listTambahan") as? ArrayList<modeltransaksitambahan>
            ?: intent.getSerializableExtra("tambahan") as? ArrayList<modeltransaksitambahan>
            ?: arrayListOf()

        // Generate ID transaksi dan tanggal saat ini
        val currentTime = System.currentTimeMillis()
        val idTransaksi = "TRX${SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date(currentTime))}"
        val tanggalStr = formatTimestamp(currentTime)

        // Set data ke view
        tvTanggal.text = tanggalStr
        tvIdTransaksi.text = idTransaksi
        tvNamaPelanggan.text = nama

        // Tampilkan nomor HP dengan status yang jelas
        val displayHP = when {
            noHPPelanggan.isNotEmpty() && noHPPelanggan.isNotBlank() -> noHPPelanggan
            else -> "Tidak tersedia"
        }
        tvNoHP.text = "No. HP: $displayHP"

        tvStatus?.text = "Menunggu Pembayaran ($metodePembayaran)"
        tvLayananUtama.text = layanan

        // Update RecyclerView untuk tambahan
        listTambahan.clear()
        listTambahan.addAll(tambahan)
        adapter?.notifyDataSetChanged()

        // Hitung subtotal tambahan
        val subtotalTambahan = tambahan.sumOf { item ->
            val cleanHarga = item.hargaLayanan?.replace("[^\\d]".toRegex(), "") ?: "0"
            cleanHarga.toIntOrNull() ?: 0
        }

        // Hitung harga layanan utama
        val cleanHargaUtama = harga.replace("[^\\d]".toRegex(), "")
        val hargaLayananUtama = cleanHargaUtama.toIntOrNull() ?: 0

        // Set ke TextView
        tvHargaLayanan.text = formatCurrency(hargaLayananUtama)
        tvSubtotalTambahan.text = formatCurrency(subtotalTambahan)
        tvTotalBayar.text = formatCurrency(totalHarga)
    }

    private fun formatTimestamp(timestamp: Long?): String {
        return try {
            if (timestamp == null || timestamp == 0L) return "-"
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting timestamp", e)
            "-"
        }
    }

    private fun formatCurrency(value: Int): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.format(value)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting currency", e)
            "Rp $value"
        }
    }

    private fun setupButtons() {
        btnCetak.setOnClickListener {
            // PERBAIKAN: Cek permission sebelum mencoba print
            if (!hasAllBluetoothPermissions()) {
                showToast("Izin Bluetooth diperlukan untuk mencetak")
                requestBluetoothPermissions()
                return@setOnClickListener
            }

            val message = buildPrintMessage()
            printToBluetooth(message)

            // Add laporan after successful print attempt
            addLaporanToDataLaporan()
        }

        btnKirimWhatsapp.setOnClickListener {
            Log.d(TAG, "WhatsApp button clicked. noHPPelanggan = '$noHPPelanggan'")

            // Validate phone number before sending
            if (noHPPelanggan.isEmpty() || noHPPelanggan.isBlank() || noHPPelanggan == "Tidak tersedia") {
                showToast("Nomor HP pelanggan tidak tersedia atau tidak valid")
                Log.w(TAG, "WhatsApp failed: No valid phone number. Value: '$noHPPelanggan'")
                return@setOnClickListener
            }

            val message = buildWhatsappMessage()
            sendWhatsappMessage(message)

            // Add laporan after WhatsApp message is sent
            addLaporanToDataLaporan()
        }
    }

    // ===== FUNGSI-FUNGSI BARU UNTUK AUTO-SAVE LAPORAN =====

    private fun autoSaveLaporan() {
        // Panggil ini di onCreate() setelah loadDataFromIntent()
        if (shouldAutoSaveLaporan()) {
            debugLaporanData()
            addLaporanToDataLaporan()
        }
    }

    private fun shouldAutoSaveLaporan(): Boolean {
        // Cek apakah data sudah pernah disimpan sebelumnya
        val transactionId = tvIdTransaksi.text.toString()
        return transactionId.isNotEmpty() && transactionId != "-"
    }

    private fun debugLaporanData() {
        Log.d(TAG, "=== DEBUG LAPORAN DATA ===")
        Log.d(TAG, "Transaction ID: ${tvIdTransaksi.text}")
        Log.d(TAG, "Customer Name: ${tvNamaPelanggan.text}")
        Log.d(TAG, "Service Name: ${tvLayananUtama.text}")
        Log.d(TAG, "Total Amount: ${tvTotalBayar.text}")
        Log.d(TAG, "Status: ${tvStatus?.text}")
        Log.d(TAG, "=== END DEBUG LAPORAN ===")
    }

    private fun createNewLaporan(transactionId: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = sdf.format(Date())

        val customerName = tvNamaPelanggan.text.toString()
        val serviceName = tvLayananUtama.text.toString()
        val totalAmountText = tvTotalBayar.text.toString()
        val totalAmount = extractNumericValue(totalAmountText)
        val status = determinePaymentStatus()

        val newLaporan = modellaporan(
            noTransaksi = transactionId,
            tanggal = formattedDate,
            namaPelanggan = customerName,
            namaLayanan = serviceName,
            totalHarga = totalAmount,
            status = status
        )

        val database = FirebaseDatabase.getInstance().getReference("Laporan")
        database.child(transactionId).setValue(newLaporan)
            .addOnSuccessListener {
                Log.d(TAG, "Data laporan berhasil disimpan untuk ID: $transactionId")
                showToast("Data transaksi berhasil disimpan!")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save laporan for ID: $transactionId", exception)
                showToast("Gagal menyimpan data transaksi: ${exception.message}")
            }
    }

    private fun updateExistingLaporan(transactionId: String) {
        // Update status atau data lain jika diperlukan
        val updates = mapOf<String, Any>(
            "status" to determinePaymentStatus(),
            "lastUpdated" to System.currentTimeMillis()
        )

        val database = FirebaseDatabase.getInstance().getReference("Laporan")
        database.child(transactionId).updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Laporan has uploaded / Data Laporan terdaftar: $transactionId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update laporan for ID: $transactionId", exception)
            }
    }

    // ===== FUNGSI addLaporanToDataLaporan() YANG SUDAH DIPERBAIKI =====

    private fun addLaporanToDataLaporan() {
        try {
            val transactionId = tvIdTransaksi.text.toString()

            // Validasi data wajib
            if (transactionId.isEmpty() || transactionId == "-") {
                Log.w(TAG, "Transaction ID tidak valid / doesn't valid : $transactionId")
                return
            }

            val customerName = tvNamaPelanggan.text.toString()
            if (customerName.isEmpty() || customerName == "-") {
                Log.w(TAG, "Nama pelanggan tidak valid / Customer name is not valid : $customerName")
                return
            }

            // Cek dulu apakah data sudah ada di database
            val database = FirebaseDatabase.getInstance().getReference("Laporan")
            database.child(transactionId).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "Data laporan sudah ada untuk ID: $transactionId")
                        // Optional: Update data yang sudah ada
                        updateExistingLaporan(transactionId)
                    } else {
                        // Data belum ada, buat baru
                        createNewLaporan(transactionId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error checking existing laporan", exception)
                    // Fallback: tetap coba buat data baru
                    createNewLaporan(transactionId)
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error in addLaporanToDataLaporan", e)
            showToast("Error menyimpan laporan: ${e.message}")
        }
    }

    private fun extractNumericValue(currencyText: String): Int {
        return try {
            Log.d(TAG, "Extracting numeric value from: '$currencyText'")

            // Hapus semua karakter kecuali digit dan titik/koma
            val cleanText = currencyText.replace("[^\\d.,]".toRegex(), "")

            // Jika ada titik DAN koma, anggap format Indonesia (12.000,50)
            if (cleanText.contains(".") && cleanText.contains(",")) {
                val parts = cleanText.split(",")
                val integerPart = parts[0].replace(".", "")
                return integerPart.toIntOrNull() ?: 0
            }

            // Jika hanya ada titik, cek posisinya
            if (cleanText.contains(".")) {
                val dotIndex = cleanText.lastIndexOf(".")
                val afterDot = cleanText.substring(dotIndex + 1)

                // Jika setelah titik ada 3 digit atau lebih, anggap pemisah ribuan
                if (afterDot.length >= 3) {
                    return cleanText.replace(".", "").toIntOrNull() ?: 0
                } else {
                    // Jika setelah titik kurang dari 3 digit, anggap desimal
                    return cleanText.split(".")[0].toIntOrNull() ?: 0
                }
            }

            // Jika hanya ada koma, anggap pemisah ribuan (format US) atau desimal
            if (cleanText.contains(",")) {
                val commaIndex = cleanText.lastIndexOf(",")
                val afterComma = cleanText.substring(commaIndex + 1)

                // Jika setelah koma ada 3 digit atau lebih, anggap pemisah ribuan
                if (afterComma.length >= 3) {
                    return cleanText.replace(",", "").toIntOrNull() ?: 0
                } else {
                    // Jika setelah koma kurang dari 3 digit, anggap desimal
                    return cleanText.split(",")[0].toIntOrNull() ?: 0
                }
            }

            // Hanya digit
            cleanText.toIntOrNull() ?: 0

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting numeric value from: '$currencyText'", e)
            0
        }
    }

    // DEBUGGING: Fungsi untuk test berbagai format
    private fun testExtractNumericValue() {
        val testCases = listOf(
            "Rp 12.000",
            "Rp12.000",
            "12.000",
            "12,000",
            "Rp 12.000,50",
            "Rp 1.200.000",
            "1200000",
            "12000"
        )

        testCases.forEach { testCase ->
            val result = extractNumericValue(testCase)
            Log.d(TAG, "Test: '$testCase' -> $result")
        }
    }

    // Helper function to determine payment status - DIPERBAIKI
    private fun determinePaymentStatus(): String {
        return try {
            val statusText = tvStatus?.text?.toString()
            Log.d(TAG, "Status text from tvStatus: '$statusText'")

            when {
                statusText?.contains("Selesai", ignoreCase = true) == true -> "Lunas"
                statusText?.contains("Lunas", ignoreCase = true) == true -> "Lunas"
                statusText?.contains("Paid", ignoreCase = true) == true -> "Lunas"
                statusText?.contains("Proses", ignoreCase = true) == true -> "Dalam Proses"
                statusText?.contains("Menunggu", ignoreCase = true) == true -> "Belum Lunas"
                statusText?.contains("Pending", ignoreCase = true) == true -> "Belum Lunas"
                else -> {
                    // Jika tvStatus null atau tidak ada, coba dari intent
                    val intentStatus = intent.getStringExtra("status")
                        ?: intent.getStringExtra("metodePembayaran")
                        ?: "Belum Lunas"
                    Log.d(TAG, "Using status from intent: '$intentStatus'")
                    intentStatus
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error determining payment status", e)
            "Belum Lunas"
        }
    }

    // ===== SISA FUNGSI YANG SAMA SEPERTI SEBELUMNYA =====

    private fun buildPrintMessage(): String {
        return buildString {
            append("\n")
            append("Laundry Elnoah\n")
            append("Alamat Laundry\n")
            append("==============================\n")
            append("ID Transaksi: ${tvIdTransaksi.text}\n")
            append("Tanggal: ${tvTanggal.text}\n")
            append("Pelanggan: ${tvNamaPelanggan.text}\n")

            // Tampilkan nomor HP di struk jika tersedia
            if (noHPPelanggan.isNotEmpty() && noHPPelanggan != "Tidak tersedia") {
                append("No. HP: $noHPPelanggan\n")
            }

            tvStatus?.let { append("Status: ${it.text}\n") }
            append("------------------------------\n")

            val namaUtama = tvLayananUtama.text.toString().take(20).padEnd(20)
            val hargaUtama = tvHargaLayanan.text.toString().padStart(12)
            append("$namaUtama$hargaUtama\n")

            if (listTambahan.isNotEmpty()) {
                append("\nLayanan Tambahan:\n")
                listTambahan.forEachIndexed { index, item ->
                    val nama = "${index + 1}. ${item.namaLayanan ?: ""}".take(20).padEnd(20)
                    val harga = (item.hargaLayanan ?: "0").padStart(12)
                    append("$nama Rp $harga\n")
                }
                append("------------------------------\n")
                append("Subtotal Tambahan: ${tvSubtotalTambahan.text}\n")
            }

            append("TOTAL BAYAR: ${tvTotalBayar.text}\n")
            append("==============================\n")
            append("Terima kasih telah memilih\n")
            append("Laundry Elnoah\n")
            append("\n\n\n")
        }
    }

    private fun buildWhatsappMessage(): String {
        return buildString {
            append("*Hai ${tvNamaPelanggan.text}* 👋\n\n")
            append("*Berikut rincian laundry Anda:*\n")
            append("• ID Transaksi: ${tvIdTransaksi.text}\n")
            append("• Tanggal: ${tvTanggal.text}\n")
            tvStatus?.let { append("• Status: ${it.text}\n") }
            append("\n")
            append("*Layanan Utama:*\n")
            append("• ${tvLayananUtama.text} - ${tvHargaLayanan.text}\n\n")

            if (listTambahan.isNotEmpty()) {
                append("*Layanan Tambahan:*\n")
                listTambahan.forEachIndexed { index, item ->
                    append("${index + 1}. ${item.namaLayanan ?: ""} - Rp ${item.hargaLayanan ?: "0"}\n")
                }
                append("\n")
            }

            append("*Total Bayar:* ${tvTotalBayar.text}\n\n")
            append("Terima kasih telah menggunakan Laundry Elnoah 💙")
        }
    }

    private fun sendWhatsappMessage(message: String) {
        try {
            // Format nomor HP
            val formattedNumber = formatPhoneNumber(noHPPelanggan)

            Log.d(TAG, "Original number: '$noHPPelanggan', Formatted: '$formattedNumber'")

            if (formattedNumber.isEmpty()) {
                showToast("Nomor HP tidak valid atau tidak dapat diformat")
                return
            }

            // PERBAIKAN: Gunakan wa.me tanpa + di depan
            val whatsappUrl = "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"
            Log.d(TAG, "WhatsApp URL: $whatsappUrl")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))

            // Langsung start tanpa cek resolveActivity dulu
            startActivity(intent)
            showToast("Membuka WhatsApp...")

        } catch (e: Exception) {
            Log.e(TAG, "Error sending WhatsApp message", e)
            // Jika gagal, coba dengan cara lain
            try {
                val formattedNumber = formatPhoneNumber(noHPPelanggan)
                val fallbackUrl = "https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}"
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                startActivity(fallbackIntent)
                showToast("Membuka WhatsApp...")
            } catch (e2: Exception) {
                showToast("Tidak dapat membuka WhatsApp")
            }
        }
    }

    // Fungsi untuk memformat nomor HP dengan logging
    private fun formatPhoneNumber(phoneNumber: String): String {
        Log.d(TAG, "formatPhoneNumber called with: '$phoneNumber'")

        if (phoneNumber.isEmpty() || phoneNumber.isBlank() || phoneNumber == "Tidak tersedia") {
            Log.w(TAG, "Phone number is empty, blank, or 'Tidak tersedia'")
            return ""
        }

        // Hapus semua karakter non-digit
        val cleanNumber = phoneNumber.replace("[^\\d]".toRegex(), "")
        Log.d(TAG, "Clean number: '$cleanNumber'")

        if (cleanNumber.length < 8) {
            Log.w(TAG, "Phone number too short: $cleanNumber")
            return ""
        }

        val result = when {
            // Jika sudah dimulai dengan 62, gunakan langsung
            cleanNumber.startsWith("62") -> cleanNumber
            // Jika dimulai dengan 08, ganti dengan 628
            cleanNumber.startsWith("08") -> "62${cleanNumber.substring(1)}"
            // Jika dimulai dengan 8, tambahkan 62
            cleanNumber.startsWith("8") -> "62$cleanNumber"
            // Jika dimulai dengan 0, ganti dengan 62
            cleanNumber.startsWith("0") -> "62${cleanNumber.substring(1)}"
            // Lainnya, tambahkan 62
            else -> "62$cleanNumber"
        }

        Log.d(TAG, "Final formatted number: '$result'")
        return result
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun printToBluetooth(message: String) {
        try {
            if (bluetoothAdapter == null) {
                showToast("Bluetooth tidak tersedia")
                return
            }

            if (!bluetoothAdapter!!.isEnabled) {
                showToast("Bluetooth tidak aktif")
                return
            }

            if (!hasBluetoothConnectPermission()) {
                showToast("Izin Bluetooth diperlukan untuk mencetak")
                requestBluetoothPermissions()
                showPermissionSettingsPrompt() // Direct user to settings
                return
            }

            val pairedDevices = try {
                bluetoothAdapter!!.bondedDevices
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException accessing bonded devices", e)
                showToast("Izin Bluetooth tidak cukup untuk mengakses perangkat")
                requestBluetoothPermissions()
                showPermissionSettingsPrompt() // Direct user to settings
                return
            }

            var printerDevice: BluetoothDevice? = null
            for (device in pairedDevices) {
                if (device.address == printerMAC) {
                    printerDevice = device
                    break
                }
            }

            if (printerDevice == null) {
                showToast("Printer tidak ditemukan. Printer not found.")
                return
            }

            try {
                bluetoothSocket = printerDevice.createRfcommSocketToServiceRecord(printerUUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
                showToast(getString(R.string.Berhasildicetak))
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException during Bluetooth connection", e)
                showToast("Bluetooth permission: ${e.message}")
                requestBluetoothPermissions()
                showPermissionSettingsPrompt() // Direct user to settings
            } catch (e: Exception) {
                Log.e(TAG, "Error printing to Bluetooth", e)
                showToast("Failed print: ${e.message}")
            } finally {
                try {
                    outputStream?.close()
                    bluetoothSocket?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing Bluetooth connection", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in printToBluetooth", e)
            showToast("Error saat mencetak: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "Bluetooth permissions granted")
                } else {
                    showToast("Izin Bluetooth ditolak. Fungsi cetak tidak dapat digunakan.")
                    Log.w(TAG, "Bluetooth permissions denied")
                    showPermissionSettingsPrompt() // Guide user to settings
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Bluetooth connection in onDestroy", e)
        }
    }
}