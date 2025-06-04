package com.elnoah.laundry.modeldata

import java.io.Serializable

data class modelinvoice(
    var idTransaksi: String? = null,
    var namaPelanggan: String? = null,
    var status: String? = null,
    var noHPPelanggan: String? = null,
    var tanggalTerdaftar: Long? = 0L,  // timestamp dalam milidetik
    var namaLayanan: String? = null,
    var jumlahTambahan: Int? = 0,
    var totalBayar: Int? = 0,
    var tanggalDiambil: Long? = 0L
) : Serializable