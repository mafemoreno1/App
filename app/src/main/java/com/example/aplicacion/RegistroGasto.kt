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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegistroGasto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroGastoScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGastoScreen() {
    var nombreGasto by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    val categoriasGasto = listOf("Comida", "Transporte", "Servicios", "Alquiler", "Entretenimiento", "Otro")
    var categoriaSeleccionada by remember { mutableStateOf(categoriasGasto.first()) }
    var categoriaExpandida by remember { mutableStateOf(false) }

    val azulPrincipal = Color(0xFF3F51B5)
    val fondoCampo = Color(0xFFE8EAF6)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fecha = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        bottomBar = { GastosBottomNavigationBar(azulPrincipal) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

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
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro Gasto",
                    color = azulPrincipal,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Nombre del gasto
                CampoTextoPersonalizado(
                    label = "Nombre del Gasto",
                    valor = nombreGasto,
                    onChange = { nombreGasto = it },
                    azul = azulPrincipal,
                    fondo = fondoCampo,
                    imeAction = ImeAction.Next,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Monto
                CampoTextoPersonalizado(
                    label = "Monto",
                    valor = monto,
                    onChange = { monto = it.replace(',', '.') },
                    azul = azulPrincipal,
                    fondo = fondoCampo,
                    tipo = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Categoría (ExposedDropdown)
                ExposedDropdownMenuBox(
                    expanded = categoriaExpandida,
                    onExpandedChange = { categoriaExpandida = !categoriaExpandida },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Categoría",
                            color = azulPrincipal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpandida) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = fondoCampo,
                                unfocusedContainerColor = fondoCampo,
                                disabledContainerColor = fondoCampo,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(color = azulPrincipal)
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = categoriaExpandida,
                        onDismissRequest = { categoriaExpandida = false }
                    ) {
                        categoriasGasto.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria, color = azulPrincipal) },
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    categoriaExpandida = false
                                }
                            )
                        }
                    }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        enabled = false,
                        textStyle = TextStyle(color = azulPrincipal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = fondoCampo,
                            unfocusedContainerColor = fondoCampo,
                            disabledContainerColor = fondoCampo,
                            disabledTextColor = azulPrincipal,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (nombreGasto.isNotEmpty() && monto.isNotEmpty() && fecha.isNotEmpty()) {
                            guardarGasto(
                                context,
                                nombreGasto,
                                monto,
                                fecha,
                                categoriaSeleccionada
                            )
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
                    Text("Guardar Gasto", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

fun guardarGasto(
    context: android.content.Context,
    nombre: String,
    monto: String,
    fecha: String,
    categoria: String
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val uid = user.uid
        val database = FirebaseDatabase.getInstance()
        val gastosRef = database.getReference("gastos").child(uid)
        val alertasRef = database.getReference("alertas").child(uid)

        val montoDouble = monto.toDoubleOrNull()
        if (montoDouble == null) {
            Toast.makeText(context, "Monto no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val idGasto = gastosRef.push().key
        if (idGasto != null) {
            val gasto = mapOf(
                "id" to idGasto,
                "nombre" to nombre,
                "monto" to montoDouble,
                "fecha" to fecha,
                "categoria" to categoria,
                "tipo" to "Gasto",
                "uidUsuario" to uid
            )

            gastosRef.child(idGasto).setValue(gasto)
                .addOnSuccessListener {
                    // Guardar alerta personalizada dentro del nodo del usuario
                    val idAlerta = alertasRef.push().key ?: return@addOnSuccessListener
                    val alerta = mapOf(
                        "id" to idAlerta,
                        "titulo" to "Nuevo Gasto",
                        "mensaje" to "Has registrado un gasto de $$montoDouble en $categoria.",
                        "tipo" to "gasto",
                        "fecha" to System.currentTimeMillis(),
                        "leida" to false
                    )
                    alertasRef.child(idAlerta).setValue(alerta)

                    Toast.makeText(context, "Gasto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, Historial::class.java))
                    (context as? ComponentActivity)?.finish()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    } else {
        Toast.makeText(context, "Inicia sesión para guardar gastos", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTextoPersonalizado(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    azul: Color,
    fondo: Color,
    tipo: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = azul, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = valor,
            onValueChange = onChange,
            textStyle = TextStyle(color = azul),
            keyboardOptions = KeyboardOptions(keyboardType = tipo, imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fondo,
                unfocusedContainerColor = fondo,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun GastosBottomNavigationBar(colorPrincipal: Color) {
    val context = LocalContext.current
    val navegarA: (Class<*>) -> Unit = { cls ->
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        (context as? ComponentActivity)?.finish()
    }

    val iconosYAcciones = listOf(
        Pair(R.drawable.outline_home_24, Pair("Inicio", { navegarA(Inicio::class.java) })),
        Pair(R.drawable.outline_notifications_24, Pair("Alertas", { navegarA(AlertasActivity::class.java) })),
        Pair(R.drawable.outline_assignment_turned_in_24, Pair("Metas", { })),
        Pair(R.drawable.outline_person_24, Pair("Asistente IA", { }))
    )

    NavigationBar(containerColor = Color.White) {
        iconosYAcciones.forEach { (icono, textoYAccion) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = icono),
                        contentDescription = textoYAccion.first,
                        tint = colorPrincipal
                    )
                },
                label = { Text(textoYAccion.first, fontSize = 12.sp, color = colorPrincipal) },
                selected = false,
                onClick = textoYAccion.second,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorPrincipal,
                    selectedTextColor = colorPrincipal,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
