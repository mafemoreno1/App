package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class AlertasActivity : ComponentActivity() {

    private lateinit var dbRefAlertas: DatabaseReference
    private lateinit var currentUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener UID del usuario actual
        currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Referencia a las alertas del usuario en Firebase
        dbRefAlertas = FirebaseDatabase.getInstance()
            .getReference("alertas")
            .child(currentUid)

        setContent {
            var listaAlertas by remember { mutableStateOf(listOf<Alerta>()) }

            // Escuchar cambios en la base de datos
            LaunchedEffect(Unit) {
                dbRefAlertas.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tempList = mutableListOf<Alerta>()
                        for (alertaSnap in snapshot.children) {
                            try {
                                val alerta = alertaSnap.getValue(Alerta::class.java)
                                if (alerta != null && alerta.fecha != null) {
                                    tempList.add(alerta)
                                }
                            } catch (e: Exception) {
                                Log.e("AlertasActivity", "Error al leer alerta: ${e.message}")
                            }
                        }
                        listaAlertas = tempList.sortedByDescending { it.fecha!! }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AlertasActivity", "Error Firebase: ${error.message}")
                    }
                })
            }

            PantallaAlertas(listaAlertas, dbRefAlertas)
        }
    }
}

// Modelo de datos para una alerta
data class Alerta(
    val id: String = "",
    val uid: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "",
    val fecha: Long? = System.currentTimeMillis(),
    val leida: Boolean = false
)

@Composable
fun PantallaAlertas(listaAlertas: List<Alerta>, dbRefAlertas: DatabaseReference) {
    val context = LocalContext.current
    val azulPrincipal = Color(0xFF3F51B5)
    val azulClaro = Color(0xFFCCD4FF)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                IconButton(
                    onClick = { (context as? ComponentActivity)?.finish() },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Volver",
                        tint = azulPrincipal
                    )
                }

                Text(
                    text = "Alertas",
                    color = azulPrincipal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
            }
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = R.drawable.outline_home_24,
                    label = "Inicio",
                    onClick = { context.startActivity(Intent(context, Inicio::class.java)) }
                )
                BottomNavItem(
                    icon = R.drawable.outline_notifications_24,
                    label = "Alertas",
                    onClick = { /* Ya estás aquí */ }
                )
                BottomNavItem(
                    icon = R.drawable.outline_assignment_turned_in_24,
                    label = "Metas",
                    onClick = { /* Ir a metas */ }
                )
                BottomNavItem(
                    icon = R.drawable.outline_person_24,
                    label = "Asistente IA",
                    onClick = { /* Ir a Asistente IA */ }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (listaAlertas.isEmpty()) {
                Text(
                    text = "No hay alertas por ahora.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 100.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaAlertas) { alerta ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(azulClaro)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (!alerta.leida) {
                                                dbRefAlertas.child(alerta.id)
                                                    .child("leida")
                                                    .setValue(true)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = alerta.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = azulPrincipal
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = formatMensaje(alerta.mensaje),
                                        fontSize = 15.sp,
                                        color = azulPrincipal,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        dbRefAlertas.child(alerta.id).removeValue()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_x_circle_24), // usa un ícono “x” existente
                                        contentDescription = "Eliminar alerta",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: Int, label: String, onClick: () -> Unit) {
    val azulPrincipal = Color(0xFF3F51B5)
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = azulPrincipal
            )
            Text(
                text = label,
                color = azulPrincipal,
                fontSize = 12.sp
            )
        }
    }
}

fun formatMensaje(texto: String): String {
    val regex = Regex("""\$\d+(\.\d+)?""")
    val formatter = NumberFormat.getInstance(Locale("es", "CO"))
    return texto.replace(regex) {
        val numero = it.value.replace("$", "").toDoubleOrNull() ?: 0.0
        "$" + formatter.format(numero)
    }
}
