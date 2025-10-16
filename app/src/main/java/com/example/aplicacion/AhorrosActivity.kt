package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AhorrosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AhorrosScreen(
                onAgregarClick = {
                    startActivity(Intent(this, AgregarAhorroActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun AhorrosScreen(onAgregarClick: () -> Unit) {
    var ahorros = remember {
        mutableStateListOf(
            Ahorro("Viaje a la playa", 900000, 1200000),
            Ahorro("Fondo de emergencia", 500000, 1000000)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "Ahorros",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F4E9A),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ahorros.forEach { ahorro ->
            AhorroItem(ahorro)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAgregarClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
        ) {
            Text("Agregar ahorro", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun AhorroItem(ahorro: Ahorro) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8EAF6), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(ahorro.nombre, fontWeight = FontWeight.Bold, color = Color(0xFF3F4E9A))
        LinearProgressIndicator(
            progress = ahorro.montoActual.toFloat() / ahorro.meta.toFloat(),
            color = Color(0xFF3F51B5),
            trackColor = Color(0xFFCFD8DC),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            text = "$${ahorro.montoActual} / $${ahorro.meta}",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

data class Ahorro(val nombre: String, val montoActual: Int, val meta: Int)
