package com.elnoah.laundry.modeldata

data class modelcabang(
    var idCabang: String? = "",
    var namaLokasiCabang: String? = "",
    var alamatCabang: String? = "",
    var teleponCabang: String? = "",
    var tanggalTerdaftar: String? = "" // Added registration date field
)