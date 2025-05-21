package com.elnoah.laundry.Transaksi

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihTambahanAdapter
import com.elnoah.laundry.modeldata.modeltransaksitambahan

class KonfirmasiTransaksi : AppCompatActivity() {
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHP: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvTambahan: RecyclerView
    private lateinit var btnBayar: Button
    private lateinit var btnBatal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_transaksi)

        // Inisialisasi view
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan_konfirmasi)
        tvNoHP = findViewById(R.id.tv_no_hp_konfirmasi)
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan_konfirmasi)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan_konfirmasi)
        tvTotal = findViewById(R.id.tv_total_bayar)
        rvTambahan = findViewById(R.id.rv_tambahan_konfirmasi)
        btnBayar = findViewById(R.id.btn_pembayaran)
        btnBatal = findViewById(R.id.btn_batal)

        // Ambil data dari Intent
        val nama = intent.getStringExtra("namaPelanggan") ?: "-"
        val noHP = intent.getStringExtra("noHP") ?: "-"
        val layanan = intent.getStringExtra("namaLayanan") ?: "-"
        val harga = intent.getStringExtra("hargaLayanan") ?: "0"
        val listTambahan = intent.getSerializableExtra("listTambahan") as? ArrayList<modeltransaksitambahan> ?: arrayListOf()

        // Hitung total
        val hargaClean = harga.replace("[^\\d]".toRegex(), "")
        var totalHarga = hargaClean.toIntOrNull() ?: 0

        listTambahan.forEach { tambahan ->
            val tambahanHarga = tambahan.hargaLayanan?.replace("[^\\d]".toRegex(), "") ?: "0"
            totalHarga += tambahanHarga.toIntOrNull() ?: 0
        }

        // Tampilkan data
        tvNamaPelanggan.text = nama
        tvNoHP.text = noHP
        tvNamaLayanan.text = layanan
        tvHargaLayanan.text = "Rp$harga"
        tvTotal.text = "Rp$totalHarga"

        rvTambahan.layoutManager = LinearLayoutManager(this)
        rvTambahan.adapter = PilihTambahanAdapter(listTambahan) { /* kosong */ }

        // Tombol bayar -> tampilkan dialog pembayaran
        btnBayar.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mod_pembayaran, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Tangkap tombol dari layout dialog
            val btnOvo = dialogView.findViewById<Button>(R.id.btnOvo)
            val btnDana = dialogView.findViewById<Button>(R.id.btnDana)
            val btnGopay = dialogView.findViewById<Button>(R.id.btnGopay)
            val btnQris = dialogView.findViewById<Button>(R.id.btnQris)
            val btnTunai = dialogView.findViewById<Button>(R.id.btnTunai)
            val btnBayarNanti = dialogView.findViewById<Button>(R.id.btnBayarNanti)

            val bayarDanKeInvoice = { metode: String ->
                alertDialog.dismiss()
                val intent = Intent(this, InvoiceTransaksi::class.java).apply {
                    putExtra("namaPelanggan", nama)
                    putExtra("noHP", noHP)
                    putExtra("namaLayanan", layanan)
                    putExtra("hargaLayanan", harga)
                    putExtra("totalHarga", totalHarga)
                    putExtra("metodePembayaran", metode)
                    putExtra("listTambahan", listTambahan)
                }
                startActivity(intent)
            }

            // Set listener semua tombol metode
            btnOvo.setOnClickListener { bayarDanKeInvoice("OVO") }
            btnDana.setOnClickListener { bayarDanKeInvoice("DANA") }
            btnGopay.setOnClickListener { bayarDanKeInvoice("GoPay") }
            btnQris.setOnClickListener { bayarDanKeInvoice("QRIS") }
            btnTunai.setOnClickListener { bayarDanKeInvoice("Tunai") }
            btnBayarNanti.setOnClickListener { bayarDanKeInvoice("Bayar Nanti") }


        }


        // Tombol batal di halaman utama -> kembali ke DataTransaksi
        btnBatal.setOnClickListener {
            val intent = Intent(this, DataTransaksi::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}
