package com.example.aplicacion

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AgregarAhorroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            AgregarAhorroScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarAhorroScreen() {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var meta by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Color(0xFF3F4E9A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))  //8

                Text(
                    text = "Agregar Ahorro",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F4E9A),
                )
            }
        },

        bottomBar = {
            BottomBarAgregarAhorro(
                onInicioClick = { context.startActivity(Intent(context, Inicio::class.java)) },
                onAlertasClick = {context.startActivity(Intent(context, AlertasActivity::class.java))},
                onMetasClick = { },
                onAsistenteClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)
        ) {
            CampoConTitulo("Nombre", nombre, KeyboardType.Text, imeAction = ImeAction.Next) { nombre = it }
            CampoConTitulo("Monto ahorrado", monto, KeyboardType.Number, imeAction = ImeAction.Next) { monto = it }
            CampoConTitulo("Meta", meta, KeyboardType.Number, imeAction = ImeAction.Done) { meta = it }
            FechaPickerField(fecha = fecha) { fecha = it }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        Toast.makeText(context, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (nombre.isNotEmpty() && monto.isNotEmpty() && meta.isNotEmpty() && fecha.isNotEmpty()) {
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("ahorros")
                            .child(user.uid)

                        val ahorroId = ref.push().key ?: ""
                        val ahorro = mapOf(
                            "id" to ahorroId,
                            "nombre" to nombre,
                            "monto" to monto,
                            "meta" to meta,
                            "fecha" to fecha
                        )

                        ref.child(ahorroId).setValue(ahorro)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Ahorro guardado correctamente", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, AhorrosActivity::class.java))
                                (context as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F4E9A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(40.dp)
            ) {
                Text("Guardar", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun FechaPickerField(fecha: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "${selectedDay.toString().padStart(2, '0')}/" +
                    "${(selectedMonth + 1).toString().padStart(2, '0')}/$selectedYear"
            onDateSelected(formattedDate)
        },
        year,
        month,
        day
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fecha",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF3F4E9A),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9EBFF))
        ) {
            Text(
                text = if (fecha.isEmpty()) "Seleccionar Fecha" else fecha,
                color = Color(0xFF3F4E9A)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoConTitulo(
    titulo: String,
    valor: String,
    tipo: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF3F4E9A),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = valor,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = tipo, imeAction = imeAction),
            keyboardActions = KeyboardActions(),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE9EBFF),
                unfocusedContainerColor = Color(0xFFE9EBFF),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF3F4E9A),
                focusedTextColor = Color(0xFF3F4E9A),
                unfocusedTextColor = Color(0xFF3F4E9A)
            )
        )
    }
}

@Composable
fun BottomBarAgregarAhorro(
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    val colorIcono = Color(0xFF3F4E9A)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.height(65.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.outline_home_24), "Inicio", tint = colorIcono) },
                label = { Text("Inicio", fontSize = 12.sp, color = colorIcono) },
                selected = false,
                onClick = onInicioClick
            )
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.outline_notifications_24), "Alertas", tint = colorIcono) },
                label = { Text("Alertas", fontSize = 12.sp, color = colorIcono) },
                selected = false,
                onClick = onAlertasClick
            )
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.outline_assignment_turned_in_24), "Metas", tint = colorIcono) },
                label = { Text("Metas", fontSize = 12.sp, color = colorIcono) },
                selected = false,
                onClick = onMetasClick
            )
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.outline_person_24), "Asistente IA", tint = colorIcono) },
                label = { Text("Asistente IA", fontSize = 12.sp, color = colorIcono) },
                selected = false,
                onClick = onAsistenteClick
            )
        }
    }
}














