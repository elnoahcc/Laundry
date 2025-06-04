package com.elnoah.laundry.Authorize
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeluser
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var edtNama: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        edtNama = findViewById(R.id.editTextNama)
        edtPhone = findViewById(R.id.editTextPhone)
        edtPassword = findViewById(R.id.editTextPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val nama = edtNama.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (nama.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, (getString(R.string.isisemuafield)), Toast.LENGTH_SHORT).show()
            } else {
                val user = modeluser(password = password, nama = nama)

                val db = FirebaseDatabase.getInstance().getReference("users")
                db.child(phone).setValue(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, (getString(R.string.registerberhasil)), Toast.LENGTH_SHORT).show()
                        finish() // kembali ke login
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, (getString(R.string.Gagal)), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
