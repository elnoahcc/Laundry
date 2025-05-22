package com.elnoah.laundry.Transaksi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelinvoice
import com.elnoah.laundry.modeldata.modeltransaksitambahan
import java.text.SimpleDateFormat
import java.util.*

class InvoiceTransaksi : AppCompatActivity() {
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvWaktuTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHP: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var tvTotalBayar: TextView
    private lateinit var tvMetodePembayaran: TextView
    private lateinit var tvTambahan: TextView
    private lateinit var layoutListTambahan: LinearLayout
    private lateinit var btnKirimWA: Button

    private var invoiceData: modelinvoice? = null
    private var listTambahanData = arrayListOf<modeltransaksitambahan>()
    private var namaPelanggan = "-"
    private var noHP = "-"
    private var namaLayanan = "-"
    private var hargaLayanan = "0"
    private var totalHarga = 0
    private var metodePembayaran = "-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_transaksi)

        // Bind view
        tvIdTransaksi = findViewById(R.id.tv_id_transaksi)
        tvWaktuTransaksi = findViewById(R.id.tv_tanggal_invoice)
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan_invoice)
        tvNoHP = findViewById(R.id.tv_no_hp_invoice)
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan_invoice)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan_invoice)
        tvTotalBayar = findViewById(R.id.tv_total_bayar_invoice)
        tvMetodePembayaran = findViewById(R.id.tv_metode_pembayaran_invoice)
        tvTambahan = findViewById(R.id.tv_tambahan_invoice)
        layoutListTambahan = findViewById(R.id.layout_list_tambahan)
        btnKirimWA = findViewById(R.id.btn_kirim_wa)

        // Ambil data dari intent
        invoiceData = intent.getSerializableExtra("invoice") as? modelinvoice
        namaPelanggan = intent.getStringExtra("namaPelanggan") ?: "-"
        noHP = intent.getStringExtra("noHP") ?: "-"
        namaLayanan = intent.getStringExtra("namaLayanan") ?: "-"
        hargaLayanan = intent.getStringExtra("hargaLayanan") ?: "0"
        totalHarga = intent.getIntExtra("totalHarga", 0)
        metodePembayaran = intent.getStringExtra("metodePembayaran") ?: "-"
        listTambahanData = intent.getSerializableExtra("listTambahan") as? ArrayList<modeltransaksitambahan> ?: arrayListOf()

        // Tampilkan data ke tampilan
        tvIdTransaksi.text = "ID Transaksi: ${invoiceData?.idTransaksi ?: "-"}"
        tvNamaPelanggan.text = namaPelanggan
        tvNoHP.text = noHP
        tvNamaLayanan.text = namaLayanan
        tvHargaLayanan.text = "Rp. $hargaLayanan"
        tvTotalBayar.text = "Total Rp. $totalHarga"
        tvMetodePembayaran.text = "Metode Pembayaran: $metodePembayaran"

        // Format dan tampilkan waktu
        val tanggalMillis = invoiceData?.tanggalTerdaftar
        if (tanggalMillis != null && tanggalMillis > 0) {
            val date = Date(tanggalMillis)
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val waktuFormat = sdf.format(date)
            tvWaktuTransaksi.text = "Waktu: $waktuFormat"
        } else {
            tvWaktuTransaksi.text = "Waktu: -"
        }


        // Tampilkan tambahan layanan
        var subtotalTambahan = 0
        layoutListTambahan.removeAllViews()

        if (listTambahanData.isNotEmpty()) {
            for (tambahan in listTambahanData) {
                val textView = TextView(this).apply {
                    text = "${tambahan.namaLayanan ?: "-"} : Rp ${tambahan.hargaLayanan ?: "0"}"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@InvoiceTransaksi, android.R.color.black))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 4, 0, 4)
                    }
                }
                layoutListTambahan.addView(textView)
                subtotalTambahan += tambahan.hargaLayanan?.toIntOrNull() ?: 0
            }

            tvTambahan.text = "Subtotal Tambahan: Rp $subtotalTambahan"
        } else {
            tvTambahan.text = "Tidak ada layanan tambahan"
        }

        // Tombol kirim ke WhatsApp
        btnKirimWA.setOnClickListener {
            kirimInvoiceKeWA()
        }
    }

    private fun kirimInvoiceKeWA() {
        val builder = StringBuilder()
        builder.append("Invoice Transaksi\n")
        builder.append("ID Transaksi: ${invoiceData?.idTransaksi ?: "-"}\n")
        builder.append("${tvWaktuTransaksi.text}\n")
        builder.append("Nama Pelanggan: $namaPelanggan\n")
        builder.append("No HP: $noHP\n")
        builder.append("Layanan: $namaLayanan\n")
        builder.append("Harga Layanan: Rp. $hargaLayanan\n")
        builder.append("Metode Pembayaran: $metodePembayaran\n")

        if (listTambahanData.isNotEmpty()) {
            builder.append("Layanan Tambahan:\n")
            var subtotal = 0
            for (t in listTambahanData) {
                builder.append("- ${t.namaLayanan ?: "-"} : Rp ${t.hargaLayanan ?: "0"}\n")
                subtotal += t.hargaLayanan?.toIntOrNull() ?: 0
            }
            builder.append("Subtotal Tambahan: Rp $subtotal\n")
        } else {
            builder.append("Tidak ada layanan tambahan\n")
        }

        builder.append("Total Bayar: Rp $totalHarga")

        val pesan = builder.toString()
        val cleanedNumber = noHP
            .replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        val waUrl = "https://wa.me/$cleanedNumber?text=${Uri.encode(pesan)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Bisa tambahkan toast kalau WA gak ada
        }
    }
}
