package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat

class Finanzas : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    private fun navegarA(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        if (auth.currentUser == null) {
            Toast.makeText(this, "Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            PantallaFinanzas(
                onVolver = { finish() },
                onHistorialClick = { navegarA(Historial::class.java) },
                onInicioClick = { navegarA(Inicio::class.java) },
                onAlertasClick = {  },
                onMetasClick = { },
                onAsistenteClick = { },
                auth = auth,
                database = database
            )
        }
    }
}

fun formatMontoFinanzas(monto: Double): String {
    val formatter = DecimalFormat("#,###")
    val symbols = formatter.decimalFormatSymbols
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    formatter.decimalFormatSymbols = symbols
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(monto)}"
}


@Composable
fun PantallaFinanzas(
    onVolver: () -> Unit,
    onHistorialClick: () -> Unit,
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit,
    auth: FirebaseAuth,
    database: DatabaseReference
) {
    val colorAzul = Color(0xFF3F4E9A)
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    if (userId == null) {
        Toast.makeText(context, "Error: Usuario no válido.", Toast.LENGTH_LONG).show()
        return
    }

    var ingresos by remember { mutableStateOf(0.0) }
    var gastos by remember { mutableStateOf(0.0) }
    var estaCargando by remember { mutableStateOf(true) }


    LaunchedEffect(userId) {

        val ingresosRef = database.child("ingresos")
        val gastosRef = database.child("gastos")

        val totalizar = { snapshot: DataSnapshot, tipo: String ->
            var total = 0.0
            snapshot.children.forEach {

                val monto = it.child("monto").getValue(Double::class.java) ?: 0.0
                total += monto
            }
            if (tipo == "ingresos") ingresos = total else gastos = total
        }

        val listener = object : ValueEventListener {
            private var consultasPendientes = 2

            override fun onDataChange(snapshot: DataSnapshot) {

                val refPath = snapshot.ref.key

                totalizar(snapshot, refPath ?: "")

                consultasPendientes--
                if (consultasPendientes == 0) {
                    estaCargando = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                consultasPendientes--
                if (consultasPendientes == 0) {
                    estaCargando = false
                }
                Toast.makeText(context, "Error al cargar datos: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }


        ingresosRef.orderByChild("uidUsuario").equalTo(userId).addValueEventListener(listener)
        gastosRef.orderByChild("uidUsuario").equalTo(userId).addValueEventListener(listener)
    }

    val saldo = ingresos - gastos

    val porcentajeGasto = if (ingresos > 0) (gastos / ingresos * 100).toInt().coerceIn(0, 100) else 0

    Scaffold(
        bottomBar = {
            BarraInferiorFinanzas(
                onInicioClick = onInicioClick,
                onAlertasClick = onAlertasClick,
                onMetasClick = onMetasClick,
                onAsistenteClick = onAsistenteClick
            )
        }
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    colorFilter = ColorFilter.tint(colorAzul),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onVolver() }
                )
            }

            Text(
                text = "Finanzas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorAzul,
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (estaCargando) {
                CircularProgressIndicator(modifier = Modifier.padding(20.dp), color = colorAzul)
            } else {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFCDD4FF), RoundedCornerShape(16.dp))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Saldo actual", fontSize = 16.sp, color = colorAzul)
                        Text(
                            text = formatMontoFinanzas(saldo),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorAzul
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

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
                            color = colorAzul,
                            startAngle = -90f,
                            sweepAngle = (porcentajeGasto / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 24f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$porcentajeGasto%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorAzul)
                        Text("Gastado", fontSize = 14.sp, color = colorAzul)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CajaDatoFinanciero("Ingresos", ingresos, colorAzul)
                    CajaDatoFinanciero("Gastos", gastos, colorAzul)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onHistorialClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorAzul),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text("Ver historial", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CajaDatoFinanciero(titulo: String, valor: Double, colorTexto: Color) {
    Box(
        modifier = Modifier
            .background(Color(0xFFCDD4FF), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
            .width(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = titulo, fontSize = 16.sp, color = colorTexto)
            Text(
                text = formatMontoFinanzas(valor),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}

@Composable
fun BarraInferiorFinanzas(
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    val colorAzul = Color(0xFF3F51B5)
    BottomAppBar(containerColor = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconoBarraInferior(R.drawable.outline_home_24, "Inicio", colorAzul, onInicioClick)
            IconoBarraInferior(R.drawable.outline_notifications_24, "Alertas", colorAzul, onAlertasClick)
            IconoBarraInferior(R.drawable.outline_assignment_turned_in_24, "Metas", colorAzul, onMetasClick)
            IconoBarraInferior(R.drawable.outline_person_24, "Asistente IA", colorAzul, onAsistenteClick)
        }
    }
}

@Composable
fun IconoBarraInferior(iconId: Int, texto: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = texto,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(text = texto, color = color, fontSize = 12.sp)
    }
}









