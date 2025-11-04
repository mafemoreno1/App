package com.example.aplicacion

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

object Alertas {

    private var dbRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("alertas")

    fun mostrarAlerta(context: Context, titulo: String, mensaje: String, tipo: String = "info") {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val id = dbRef.push().key ?: return

        val alerta = mapOf(
            "id" to id,
            "titulo" to titulo,
            "mensaje" to mensaje,
            "tipo" to tipo,
            "fecha" to fecha
        )
        dbRef.child(id).setValue(alerta)
    }
}

