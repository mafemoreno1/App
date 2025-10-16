package com.example.appahorrosgrupo.com.example.appahorrosgrupo.com.example.appahorrosgrupo.com

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions

class AgregarAhorroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgregarAhorroScreen()
        }
    }
}

@Composable
fun AgregarAhorroScreen() {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var meta by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Agregar Ahorro",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F4E9A),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = { Text("Nombre del ahorro") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            placeholder = { Text("Monto ahorrado") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = meta,
            onValueChange = { meta = it },
            placeholder = { Text("Meta total") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                if (nombre.isEmpty() || monto.isEmpty() || meta.isEmpty()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ahorro agregado correctamente", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
        ) {
            Text("Guardar", color = Color.White, fontSize = 18.sp)
        }
    }
}
