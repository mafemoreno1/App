package com.example.aplicacion

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import java.util.*

// Pantalla de ingresos (solo de ejemplo)
class IngresosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Text(
                    text = "Pantalla de Ingresos",
                    modifier = Modifier.wrapContentSize(Alignment.Center),
                    fontSize = 24.sp
                )
            }
        }
    }
}

// Actividad principal para registrar ingresos
class RegistroIngreso : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroIngresoScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroIngresoScreen() {
    var nombreIngreso by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    val azulPrincipal = Color(0xFF3F51B5)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            fecha = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        },
        year, month, day
    )

    Scaffold(
        bottomBar = { BottomNavigationBar(azulPrincipal) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Flecha atrás
            IconButton(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 32.dp, start = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    tint = azulPrincipal,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro Ingreso",
                    color = azulPrincipal,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Nombre Ingreso
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nombre Ingreso",
                        color = azulPrincipal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = nombreIngreso,
                        onValueChange = { nombreIngreso = it },
                        textStyle = TextStyle(color = azulPrincipal),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xDDCDD4FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = azulPrincipal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Monto
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Monto",
                        color = azulPrincipal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = monto,
                        onValueChange = { monto = it },
                        textStyle = TextStyle(color = azulPrincipal),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.clearFocus()
                                datePickerDialog.show()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xDDCDD4FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = azulPrincipal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fecha
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fecha",
                        color = azulPrincipal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = fecha,
                        onValueChange = {},
                        textStyle = TextStyle(color = azulPrincipal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        enabled = false,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xDDCDD4FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledTextColor = azulPrincipal,
                            disabledIndicatorColor = Color.Transparent,
                            disabledLabelColor = azulPrincipal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Botón Guardar Ingreso en Firebase
                Button(
                    onClick = {
                        if(nombreIngreso.isNotEmpty() && monto.isNotEmpty() && fecha.isNotEmpty()) {
                            val database = FirebaseDatabase.getInstance()
                            val ingresosRef = database.getReference("ingresos")
                            val idIngreso = ingresosRef.push().key

                            val ingreso = mapOf(
                                "nombre" to nombreIngreso,
                                "monto" to monto,
                                "fecha" to fecha
                            )

                            if(idIngreso != null) {
                                ingresosRef.child(idIngreso).setValue(ingreso)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Ingreso guardado", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, IngresosActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(40.dp)
                ) {
                    Text("Guardar Ingreso", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(colorPrincipal: Color) {
    NavigationBar(containerColor = Color.White) {
        val iconos = listOf(
            Pair(R.drawable.outline_home_24, "Inicio"),
            Pair(R.drawable.outline_notifications_24, "Alertas"),
            Pair(R.drawable.outline_assignment_turned_in_24, "Metas"),
            Pair(R.drawable.outline_person_24, "Asistente IA")
        )

        iconos.forEach { (icono, texto) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = icono),
                        contentDescription = texto,
                        tint = Color(0xFF3F4E9A)
                    )
                },
                label = {
                    Text(texto, fontSize = 12.sp, color = Color(0xFF3F4E9A))
                },
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3F4E9A),
                    selectedTextColor = Color(0xFF3F4E9A),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

