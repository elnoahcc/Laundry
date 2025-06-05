package com.elnoah.laundry.modeldata

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class modelinvoice(
    var idTransaksi: String? = null,
    var namaPelanggan: String? = null,
    var status: String? = null,
    var noHPPelanggan: String? = null,
    var tanggalTerdaftar: Long? = 0L,  // timestamp dalam milidetik
    var namaLayanan: String? = null,
    var jumlahTambahan: Int? = 0,
    var totalBayar: Int? = 0,
    var tanggalDiambil: Long? = 0L,
    var metodePembayaran: String? = null,
    var catatan: String? = null,
    var hargaLayananUtama: Int? = 0,
    var subtotalTambahan: Int? = 0
) : Serializable {

    // Helper methods for better data handling
    fun getFormattedTanggalTerdaftar(): String {
        return formatTimestamp(tanggalTerdaftar)
    }

    fun getFormattedTanggalDiambil(): String {
        return formatTimestamp(tanggalDiambil)
    }

    fun getFormattedTotalBayar(): String {
        return formatCurrency(totalBayar ?: 0)
    }

    fun getFormattedNoHP(): String {
        return when {
            noHPPelanggan.isNullOrEmpty() -> "Tidak tersedia"
            noHPPelanggan!!.isBlank() -> "Tidak tersedia"
            else -> noHPPelanggan!!
        }
    }

    fun getDisplayStatus(): String {
        return status?.takeIf { it.isNotEmpty() } ?: "Status tidak diketahui"
    }

    fun isValidForDisplay(): Boolean {
        return !idTransaksi.isNullOrEmpty() &&
                !namaPelanggan.isNullOrEmpty() &&
                !namaLayanan.isNullOrEmpty()
    }

    fun hasValidPhoneNumber(): Boolean {
        return !noHPPelanggan.isNullOrEmpty() &&
                noHPPelanggan!!.isNotBlank() &&
                noHPPelanggan != "Tidak tersedia"
    }

    // Static helper methods
    companion object {
        private fun formatTimestamp(timestamp: Long?): String {
            return try {
                if (timestamp == null || timestamp == 0L) return "-"
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            } catch (e: Exception) {
                "-"
            }
        }

        private fun formatCurrency(value: Int): String {
            return try {
                val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                formatter.format(value)
            } catch (e: Exception) {
                "Rp $value"
            }
        }

        // Factory method to create a new invoice
        fun createNew(
            namaPelanggan: String,
            noHP: String?,
            namaLayanan: String,
            totalBayar: Int,
            metodePembayaran: String? = null
        ): modelinvoice {
            val currentTime = System.currentTimeMillis()
            val idTransaksi = "TRX${SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date(currentTime))}"

            return modelinvoice(
                idTransaksi = idTransaksi,
                namaPelanggan = namaPelanggan,
                noHPPelanggan = noHP,
                namaLayanan = namaLayanan,
                totalBayar = totalBayar,
                tanggalTerdaftar = currentTime,
                status = "Menunggu Pembayaran",
                metodePembayaran = metodePembayaran
            )
        }
    }

    // Override toString for better debugging
    override fun toString(): String {
        return "Invoice(id=$idTransaksi, nama=$namaPelanggan, status=$status, total=$totalBayar)"
    }
}