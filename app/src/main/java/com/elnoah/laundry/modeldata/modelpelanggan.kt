package com.elnoah.laundry.modeldata

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class modelpelanggan (
        val idPelanggan: String? = "",
        val namaPelanggan: String? = "",
        val alamatPelanggan: String? = "",
        val noHPPelanggan: String? = "",
        val cabangPelanggan: String? = "",
        val tanggalTerdaftar: String? = ""
)