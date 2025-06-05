package com.elnoah.laundry.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelinvoice
import com.elnoah.laundry.modeldata.StatusLaporan
import com.elnoah.laundry.modeldata.modellaporan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanAdapter(
    private val laporanList: MutableList<modellaporan>,
    private val onStatusChangeListener: OnStatusChangeListener? = null,
    private val onDeleteListener: OnDeleteListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnStatusChangeListener {
        fun onStatusChanged(position: Int, newStatus: StatusLaporan)
    }

    interface OnDeleteListener {
        fun onDeleteItem(position: Int)
    }

    companion object {
        const val TYPE_SUDAH_DIBAYAR = 0
        const val TYPE_BELUM_DIBAYAR = 1
        const val TYPE_SELESAI = 2

        // Konstanta untuk validasi harga
        const val MAX_REASONABLE_PRICE = 1000000 // 1 juta rupiah
        const val MIN_REASONABLE_PRICE = 1000    // 1 ribu rupiah

        fun formatCurrency(amount: Int): String {
            // Validasi harga yang wajar
            val validatedAmount = when {
                amount > MAX_REASONABLE_PRICE -> {
                    Log.w("DataLaporanAdapter", "Harga terlalu besar: $amount, kemungkinan ada kesalahan input")
                    amount // Tetap tampilkan tapi beri warning
                }
                amount < 0 -> {
                    Log.w("DataLaporanAdapter", "Harga negatif: $amount, diubah menjadi 0")
                    0
                }
                else -> amount
            }

            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(validatedAmount).replace("IDR", "Rp")
        }

        // Helper function untuk cek harga wajar
        fun isReasonablePrice(amount: Int): Boolean {
            return amount in MIN_REASONABLE_PRICE..MAX_REASONABLE_PRICE
        }
    }

    override fun getItemViewType(position: Int): Int {
        val status = getStatusFromString(laporanList[position].status)
        return when (status) {
            StatusLaporan.SUDAH_DIBAYAR -> TYPE_SUDAH_DIBAYAR
            StatusLaporan.BELUM_DIBAYAR -> TYPE_BELUM_DIBAYAR
            StatusLaporan.SELESAI -> TYPE_SELESAI
        }
    }

    // Helper function to convert String status to StatusLaporan enum
    private fun getStatusFromString(statusString: String?): StatusLaporan {
        return when (statusString?.uppercase()) {
            "SUDAH_DIBAYAR", "SUDAH DIBAYAR", "PAID" -> StatusLaporan.SUDAH_DIBAYAR
            "SELESAI", "COMPLETED", "DONE" -> StatusLaporan.SELESAI
            else -> StatusLaporan.BELUM_DIBAYAR // Default to BELUM_DIBAYAR
        }
    }

    // Helper function to convert StatusLaporan enum to String
    private fun getStringFromStatus(status: StatusLaporan): String {
        return when (status) {
            StatusLaporan.SUDAH_DIBAYAR -> "SUDAH_DIBAYAR"
            StatusLaporan.BELUM_DIBAYAR -> "BELUM_DIBAYAR"
            StatusLaporan.SELESAI -> "SELESAI"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SUDAH_DIBAYAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_sudahdibayar, parent, false)
                SudahDibayarViewHolder(view)
            }
            TYPE_BELUM_DIBAYAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_belumdibayar, parent, false)
                BelumDibayarViewHolder(view)
            }
            TYPE_SELESAI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_selesai, parent, false)
                SelesaiViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val laporan = laporanList[position]
        when (holder) {
            is SudahDibayarViewHolder -> holder.bind(laporan, position)
            is BelumDibayarViewHolder -> holder.bind(laporan, position)
            is SelesaiViewHolder -> holder.bind(laporan, position)
        }
    }

    override fun getItemCount(): Int = laporanList.size

    private fun setupLongClickDelete(itemView: View, position: Int) {
        itemView.setOnLongClickListener {
            val context = itemView.context
            val laporan = laporanList[position]

            AlertDialog.Builder(context)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi:\n\n" +
                        "No. Transaksi: ${laporan.noTransaksi}\n" +
                        "Pelanggan: ${laporan.namaPelanggan}\n" +
                        "Tanggal: ${laporan.tanggal}")
                .setPositiveButton("Hapus") { _, _ ->
                    onDeleteListener?.onDeleteItem(position)
                }
                .setNegativeButton("Batal", null)
                .show()

            true
        }
    }

    // Helper function untuk menghitung total layanan tambahan
    private fun getTotalAdditionalServices(laporan: modellaporan): Int {
        var total = 0

        // Hitung berdasarkan field yang ada di model_laporan
        laporan.parfum?.let {
            if (it.isNotEmpty() && it.equals("Tidak", ignoreCase = true).not()) {
                total++
            }
        }

        laporan.setrika?.let {
            if (it.equals("Ya", ignoreCase = true) || it.equals("true", ignoreCase = true)) {
                total++
            }
        }

        laporan.antar?.let {
            if (it.equals("Ya", ignoreCase = true) || it.equals("true", ignoreCase = true)) {
                total++
            }
        }

        return total
    }

    // Helper function untuk menampilkan text total layanan tambahan
    private fun getAdditionalServicesText(laporan: modellaporan): String {
        val total = getTotalAdditionalServices(laporan)

        return when (total) {
            0 -> "Tidak ada layanan tambahan"
            1 -> "1 Layanan Tambahan"
            else -> "$total Layanan Tambahan"
        }
    }

    // ViewHolder untuk Sudah Dibayar
    inner class SudahDibayarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView? = try {
            itemView.findViewById(R.id.tvNamaSudahDibayar)
        } catch (e: Exception) {
            // Try alternative name IDs
            try {
                itemView.findViewById(R.id.tvNamaSudahDibayar)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvDateTime: TextView? = try {
            itemView.findViewById(R.id.tvTanggal)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvTanggalSudahDibayar)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvLayanan: TextView? = try {
            itemView.findViewById(R.id.tvLayanan)
        } catch (e: Exception) {
            null
        }
        private val tvTotalAmount: TextView? = try {
            itemView.findViewById(R.id.tvTotalBayarsudahdibayar)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvTotalBayarsudahdibayar)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvAdditionalServices: TextView? = try {
            itemView.findViewById(R.id.tvTambahan)
        } catch (e: Exception) {
            null
        }
        private val btnSelesai: Button? = try {
            itemView.findViewById(R.id.btnPickup)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.btnPickup)
            } catch (e2: Exception) {
                null
            }
        }

        fun bind(laporan: modellaporan, position: Int) {
            tvNamaPelanggan?.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime?.text = laporan.tanggal ?: ""
            tvLayanan?.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount?.text = formatCurrency(laporan.totalHarga ?: 0)

            // Set additional services text - tampilkan total saja
            tvAdditionalServices?.text = getAdditionalServicesText(laporan)

            // Setup long click untuk delete
            setupLongClickDelete(itemView, position)

            // Set listener untuk tombol selesai
            btnSelesai?.setOnClickListener {
                onStatusChangeListener?.onStatusChanged(position, StatusLaporan.SELESAI)
            }
        }
    }

    // ViewHolder untuk Belum Dibayar - FIXED
    inner class BelumDibayarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Fixed: Use the correct ID from layout
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvNamaBelumDibayar)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvTanggalBelumDibayar)
        private val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalBayarbelumdibayar)
        private val tvAdditionalServices: TextView? = try {
            itemView.findViewById(R.id.tvTambahan)
        } catch (e: Exception) {
            null
        }
        // Fixed: Use the correct button ID from layout
        private val btnBayar: Button? = try {
            itemView.findViewById(R.id.btnBayar)
        } catch (e: Exception) {
            null
        }

        fun bind(laporan: modellaporan, position: Int) {
            tvNamaPelanggan.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime.text = laporan.tanggal ?: ""
            tvLayanan.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount.text = formatCurrency(laporan.totalHarga ?: 0)

            // Set additional services text - tampilkan total saja
            tvAdditionalServices?.text = getAdditionalServicesText(laporan)

            // Setup long click untuk delete
            setupLongClickDelete(itemView, position)

            // Set listener untuk tombol bayar
            btnBayar?.setOnClickListener {
                onStatusChangeListener?.onStatusChanged(position, StatusLaporan.SUDAH_DIBAYAR)
            }
        }
    }

    // Helper function untuk format tanggal pengambilan - DIMODIFIKASI
    private fun formatTanggalPengambilan(tanggalPengambilan: String?): String {
        // Jika tanggal pengambilan kosong atau null, return string kosong (bukan "Belum diambil")
        if (tanggalPengambilan.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy 'pukul' HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(tanggalPengambilan)

            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            calendar.time = date ?: return ""

            when {
                isSameDay(calendar, today) -> {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "Hari ini pukul ${timeFormat.format(date)}"
                }
                isSameDay(calendar, yesterday) -> {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "Kemarin pukul ${timeFormat.format(date)}"
                }
                isWithinWeek(calendar, today) -> {
                    val dayFormat = SimpleDateFormat("EEEE 'pukul' HH:mm", Locale("id", "ID"))
                    dayFormat.format(date)
                }
                else -> outputFormat.format(date)
            }
        } catch (e: Exception) {
            // Jika terjadi error parsing, return string kosong
            ""
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinWeek(pickupDate: Calendar, today: Calendar): Boolean {
        val daysDiff = ((today.timeInMillis - pickupDate.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        return daysDiff in 2..6
    }

    // ViewHolder untuk Selesai
    inner class SelesaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView? = try {
            itemView.findViewById(R.id.tvNamaSelesai)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvNamaSelesai)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvDateTime: TextView? = try {
            itemView.findViewById(R.id.tvTanggal)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvTanggalSelesai)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvLayanan: TextView? = try {
            itemView.findViewById(R.id.tvLayanan)
        } catch (e: Exception) {
            null
        }
        private val tvTotalAmount: TextView? = try {
            itemView.findViewById(R.id.tvTotalBayarselesai)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvTotalBayarselesai)
            } catch (e2: Exception) {
                null
            }
        }
        private val tvAdditionalServices: TextView? = try {
            itemView.findViewById(R.id.tvTambahan)
        } catch (e: Exception) {
            null
        }
        private val tvTanggalPengambilan: TextView? = try {
            itemView.findViewById(R.id.tvTanggalPengambilan)
        } catch (e: Exception) {
            try {
                itemView.findViewById(R.id.tvDiambil)
            } catch (e2: Exception) {
                null
            }
        }

        fun bind(laporan: modellaporan, position: Int) {
            tvNamaPelanggan?.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime?.text = laporan.tanggal ?: ""
            tvLayanan?.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount?.text = formatCurrency(laporan.totalHarga ?: 0)

            // Set additional services text - tampilkan total saja
            tvAdditionalServices?.text = getAdditionalServicesText(laporan)

            // Set tanggal pengambilan - tidak akan menampilkan "Belum diambil"
            val tanggalPengambilanText = formatTanggalPengambilan(laporan.tanggalPengambilan)
            tvTanggalPengambilan?.text = tanggalPengambilanText

            // Jika tidak ada tanggal pengambilan, sembunyikan TextView
            if (tanggalPengambilanText.isEmpty()) {
                tvTanggalPengambilan?.visibility = View.GONE
            } else {
                tvTanggalPengambilan?.visibility = View.VISIBLE
            }

            // Setup long click untuk delete
            setupLongClickDelete(itemView, position)
        }
    }
}