package com.elnoah.laundry.modeldata

import java.io.Serializable

data class modelinvoice(
    val idTransaksi: String? = "",
    val tanggalTerdaftar: Long = 0L
) : Serializable
