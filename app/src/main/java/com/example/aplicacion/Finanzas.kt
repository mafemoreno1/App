package com.example.aplicacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Finanzas : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Finanzas")

        setContent {
            PantallaFinanzas(
                onVolver = { finish() },
                auth = auth,
                database = database
            )
        }
    }
}

@Composable
fun PantallaFinanzas(onVolver: () -> Unit, auth: FirebaseAuth, database: DatabaseReference) {
    val userId = auth.currentUser?.uid ?: "sin_usuario"

    var ingresos by remember { mutableStateOf(0.0) }
    var gastos by remember { mutableStateOf(0.0) }

    // 🔹 Cargar datos desde Firebase
    LaunchedEffect(userId) {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ingresos = snapshot.child("ingresos").getValue(Double::class.java) ?: 0.0
                gastos = snapshot.child("gastos").getValue(Double::class.java) ?: 0.0
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val saldo = ingresos - gastos
    val porcentajeGasto = if (ingresos > 0) (gastos / ingresos * 100).toInt() else 0

    Scaffold(
        bottomBar = { BarraInferior() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Flecha atrás
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    colorFilter = ColorFilter.tint(Color(0xFF3F4E9A)),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onVolver() }
                )
            }

            // 🔹 Título
            Text(
                text = "Finanzas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F4E9A),
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 Saldo actual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCDD4FF), RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Saldo actual", fontSize = 16.sp, color = Color(0xFF3F4E9A))
                    Text(
                        text = "$${"%,.0f".format(saldo)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F4E9A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 Gráfico circular
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    drawArc(
                        color = Color(0xFFDCE0FF),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF3F4E9A),
                        startAngle = -90f,
                        sweepAngle = (porcentajeGasto / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$porcentajeGasto%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F4E9A))
                    Text("Gastado", fontSize = 14.sp, color = Color(0xFF3F4E9A))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Ingresos y Gastos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CajaDatoFinanciero("Ingresos", ingresos, Color(0xFF3F4E9A), Modifier.weight(1f)) {
                    val nuevoIngreso = ingresos + 500000
                    database.child(userId).child("ingresos").setValue(nuevoIngreso)
                }
                Spacer(modifier = Modifier.width(12.dp))
                CajaDatoFinanciero("Gastos", gastos, Color(0xFF3F4E9A), Modifier.weight(1f)) {
                    val nuevoGasto = gastos + 250000
                    database.child(userId).child("gastos").setValue(nuevoGasto)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 Botón historial
            Button(
                onClick = { /* acción ver historial */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F4E9A)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Ver historial", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CajaDatoFinanciero(
    titulo: String,
    valor: Double,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    onActualizar: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFFCDD4FF), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
            .clickable { onActualizar() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = titulo, fontSize = 16.sp, color = colorTexto)
            Text(
                text = "$${"%,.0f".format(valor)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}

@Composable
fun BarraInferior() {
    val azul = Color(0xFF3F4E9A)
    NavigationBar(containerColor = Color.White, tonalElevation = 6.dp) {
        val items = listOf(
            R.drawable.outline_home_24 to "Inicio",
            R.drawable.outline_notifications_24 to "Alertas",
            R.drawable.outline_assignment_turned_in_24 to "Metas",
            R.drawable.outline_person_24 to "Asistente IA"
        )

        items.forEach { (icon, label) ->
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        colorFilter = ColorFilter.tint(azul)
                    )
                },
                label = { Text(label, fontSize = 11.sp, color = azul) },
                selected = false,
                onClick = {}
            )
        }
    }
}



