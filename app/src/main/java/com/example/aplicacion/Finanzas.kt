package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Finanzas : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanzasScreen()
        }
    }
}

@Composable
fun FinanzasScreen() {
    val colorPrincipal = Color(0xFF3F51B5)
    val colorClaro = Color(0xFFD6D9FF)
    val context = LocalContext.current

    Scaffold(
        bottomBar = { BottomNavigationBar(colorPrincipal) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Flecha atrás
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                       val intent = Intent(context, Inicio::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.TopStart).offset(y = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = colorPrincipal,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Título
            Text(
                text = "Finanzas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrincipal,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            // Tarjeta de saldo
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorClaro),
                modifier = Modifier.fillMaxWidth(0.9f).height(90.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Saldo actual", color = colorPrincipal, fontSize = 16.sp)
                    Text("$1.780.000", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorPrincipal)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gráfico circular
            CircularChart(ingresos = 3200000f, gastos = 1420000f, colorPrincipal, colorClaro)

            Spacer(modifier = Modifier.height(32.dp))

            // Ingresos y gastos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoCard("Ingresos", "$3.200.000", colorClaro, colorPrincipal)
                InfoCard("Gastos", "$1.420.000", colorClaro, colorPrincipal)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Ver historial
            Button(
                onClick = { /* Navegar al historial */ },
                colors = ButtonDefaults.buttonColors(containerColor = colorPrincipal),
                shape = RoundedCornerShape(40),
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
            ) {
                Text("Ver historial", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CircularChart(ingresos: Float, gastos: Float, colorPrincipal: Color, colorClaro: Color) {
    val total = ingresos + gastos
    val porcentajeGastos = gastos / total

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) { // reducido de 180.dp a 140.dp
            drawArc(
                color = colorClaro,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            drawArc(
                color = colorPrincipal,
                startAngle = -90f,
                sweepAngle = 360f * porcentajeGastos,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, color: Color, colorText: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.width(140.dp).height(70.dp) // reducido de 150x80
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = colorText, fontSize = 14.sp)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorText)
        }
    }
}

@Composable
fun BottomNavigationBar(colorPrincipal: Color) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio", tint = colorPrincipal) },
            label = { Text("Inicio", color = colorPrincipal) },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas", tint = colorPrincipal) },
            label = { Text("Alertas", color = colorPrincipal) },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Flag, contentDescription = "Metas", tint = colorPrincipal) },
            label = { Text("Metas", color = colorPrincipal) },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "Asistente IA", tint = colorPrincipal) },
            label = { Text("Asistente IA", color = colorPrincipal) },
            selected = false,
            onClick = {}
        )
    }
}