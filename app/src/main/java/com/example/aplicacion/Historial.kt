package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMonto(monto: Double): String {
    val formatter = DecimalFormat("#,###")
    val symbols = formatter.decimalFormatSymbols
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    formatter.decimalFormatSymbols = symbols
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(monto)}"
}

fun formatFecha(fechaString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val date = inputFormat.parse(fechaString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        fechaString
    }
}


@Parcelize
data class Registro(
    var id: String = "",
    var nombre: String = "",
    var fecha: String = "",
    var monto: Double = 0.0,
    var categoria: String = "Sin Categoría",
    var tipo: String = ""
) : Parcelable


class Historial : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navegarA: (cls: Class<*>) -> Unit = { cls ->
            val intent = Intent(this, cls)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        setContent {
            HistorialScreen(
                onInicioClick = { navegarA(Inicio::class.java) },
                onAlertasClick = { },
                onMetasClick = {  },
                onAsistenteClick = {  }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    val azulPrincipal = Color(0xFF3F51B5)

    val colorFondoIngreso = Color(0x283F51B5)
    val colorFondoGasto = Color(0x283F51B5)
    val context = LocalContext.current


    var registros by remember { mutableStateOf<List<Registro>>(emptyList()) }
    var estaCargando by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("Ingreso") }

    var showDeleteDialog by remember { mutableStateOf<Registro?>(null) }
    var showUpdateMontoDialog by remember { mutableStateOf<Registro?>(null) }


    val reloadRegistros: () -> Unit = {
        estaCargando = true
        cargarRegistros(
            onSuccess = { loadedRegistros ->
                registros = loadedRegistros.sortedByDescending { it.fecha }
                estaCargando = false
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                registros = emptyList()
                estaCargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        reloadRegistros()
    }

    val filteredRegistros = remember(registros, selectedTab) {
        registros.filter { it.tipo == selectedTab }
    }


    Scaffold(
        bottomBar = { HistorialBottomNavigationBar(
            colorPrincipal = azulPrincipal,
            onInicioClick = onInicioClick,
            onAlertasClick = onAlertasClick,
            onMetasClick = onMetasClick,
            onAsistenteClick = onAsistenteClick
        ) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            IconButton(
                onClick = {
                    val intent = Intent(context, Finanzas::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 8.dp)
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
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Historial",
                    color = azulPrincipal,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Pestaña(
                        title = "Ingresos",
                        isSelected = selectedTab == "Ingreso",
                        onSelect = { selectedTab = "Ingreso" },
                        colorPrincipal = azulPrincipal
                    )
                    Spacer(modifier = Modifier.width(30.dp))

                    Pestaña(
                        title = "Gastos",
                        isSelected = selectedTab == "Gasto",
                        onSelect = { selectedTab = "Gasto" },
                        colorPrincipal = azulPrincipal
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))


                if (estaCargando && registros.isEmpty()) {

                    CircularProgressIndicator(modifier = Modifier.padding(20.dp), color = azulPrincipal)
                } else if (filteredRegistros.isEmpty()) {
                    Text(
                        text = "No hay ${if (selectedTab == "Ingreso") "Ingresos" else "Gastos"} registrados.",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredRegistros) { registro ->
                            RegistroCard(
                                registro = registro,
                                tipo = registro.tipo,
                                colorFondo = if (registro.tipo == "Ingreso") colorFondoIngreso else colorFondoGasto,
                                colorAzul = azulPrincipal,
                                onEditar = { /* Lógica de Edición */ },
                                onEliminar = { showDeleteDialog = it },
                                onSumarMonto = { showUpdateMontoDialog = it }
                            )
                        }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { registro ->
        ConfirmacionDialogo(
            registro = registro,
            onConfirm = {
                eliminarRegistro(context, registro) {
                    reloadRegistros()
                    showDeleteDialog = null
                }
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    showUpdateMontoDialog?.let { registro ->
        UpdateMontoDialogo(
            registro = registro,
            onConfirm = { montoAdicional ->
                actualizarMontoRegistro(context, registro, montoAdicional) {
                    reloadRegistros()
                }
                showUpdateMontoDialog = null
            },
            onDismiss = { showUpdateMontoDialog = null }
        )
    }
}




@Composable
fun Pestaña(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colorPrincipal: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) colorPrincipal else Color.Gray.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Divider(
            color = if (isSelected) colorPrincipal else Color.Transparent,
            thickness = 2.dp,
            modifier = Modifier.width(IntrinsicSize.Min)
        )
    }
}

@Composable
fun RegistroCard(
    registro: Registro,
    tipo: String,
    colorFondo: Color,
    colorAzul: Color,
    onEditar: (Registro) -> Unit,
    onEliminar: (Registro) -> Unit,
    onSumarMonto: (Registro) -> Unit
) {

    val colorMonto = if (tipo == "Ingreso") colorAzul else Color(0xFF3F51B5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = registro.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colorAzul
                )
                Text(
                    text = "Cat: ${registro.categoria}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.DarkGray.copy(alpha = 0.8f)
                )
                Text(
                    text = formatFecha(registro.fecha),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatMonto(registro.monto),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colorMonto
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Icon(
                        painter = painterResource(id = R.drawable.outline_add_circle_24),
                        contentDescription = "Agregar",
                        tint = colorAzul,
                        modifier = Modifier.size(24.dp).clickable { onSumarMonto(registro) }
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.outline_x_circle_24),
                        contentDescription = "Eliminar",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp).clickable { onEliminar(registro) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmacionDialogo(
    registro: Registro,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar el ${registro.tipo.lowercase()} '${registro.nombre}'?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMontoDialogo(
    registro: Registro,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var montoAdicionalText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val azulPrincipal = Color(0xFF5C6BC0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a ${registro.nombre}") },
        text = {
            Column {
                Text(
                    text = "Monto Actual: ${formatMonto(registro.monto)}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = montoAdicionalText,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filterIndexed { index, char ->
                            char.isDigit() || (char == '.' && newValue.indexOf('.') == index)
                        }
                        montoAdicionalText = filteredValue
                    },
                    label = { Text("Monto Adicional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = azulPrincipal) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulPrincipal,
                        cursorColor = azulPrincipal
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montoAdicional = montoAdicionalText.toDoubleOrNull()
                    if (montoAdicional != null && montoAdicional > 0) {
                        onConfirm(montoAdicional)
                    } else {
                        Toast.makeText(context, "Ingresa un monto válido para sumar.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal)
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun HistorialBottomNavigationBar(
    colorPrincipal: Color,
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    val iconos = remember {
        listOf(
            Triple(R.drawable.outline_home_24, "Inicio", onInicioClick),
            Triple(R.drawable.outline_notifications_24, "Alertas", onAlertasClick),
            Triple(R.drawable.outline_assignment_turned_in_24, "Metas", onMetasClick),
            Triple(R.drawable.outline_person_24, "Asistente IA", onAsistenteClick)
        )
    }

    NavigationBar(containerColor = Color.White) {
        iconos.forEach { (iconoResId, texto, onClickAction) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = iconoResId),
                        contentDescription = texto,
                        tint = colorPrincipal
                    )
                },
                label = { Text(texto, fontSize = 12.sp, color = colorPrincipal) },
                selected = (texto == "Historial"),
                onClick = onClickAction,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorPrincipal,
                    selectedTextColor = colorPrincipal,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

fun actualizarMontoRegistro(
    context: android.content.Context,
    registro: Registro,
    montoAdicional: Double,
    onComplete: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Toast.makeText(context, "Inicia sesión para actualizar movimientos", Toast.LENGTH_SHORT).show()
        return
    }

    val database = FirebaseDatabase.getInstance()
    val refPath = if (registro.tipo == "Ingreso") "ingresos" else "gastos"
    val ref = database.getReference(refPath).child(registro.id)

    ref.child("monto").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val montoActual = snapshot.getValue(Double::class.java) ?: registro.monto

            val nuevoMonto = montoActual + montoAdicional

            ref.child("monto").setValue(nuevoMonto)
                .addOnSuccessListener {
                    Toast.makeText(context, "Monto de '${registro.nombre}' actualizado con éxito.", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar monto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(context, "Fallo la lectura del monto actual: ${error.message}", Toast.LENGTH_LONG).show()
        }
    })
}


fun cargarRegistros(
    onSuccess: (List<Registro>) -> Unit,
    onError: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onError("Usuario no autenticado.")
        return
    }

    val uid = user.uid
    val database = FirebaseDatabase.getInstance()
    val registros = mutableListOf<Registro>()
    val tipos = listOf("ingresos", "gastos")

    var consultasPendientes = tipos.size

    tipos.forEach { tipo ->

        val ref = database.getReference(tipo)

        ref.orderByChild("uidUsuario").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNullTo(registros) { registroSnapshot ->
                        try {
                            Registro(
                                id = registroSnapshot.key ?: "",
                                nombre = registroSnapshot.child("nombre").getValue(String::class.java) ?: "Sin Nombre",
                                fecha = registroSnapshot.child("fecha").getValue(String::class.java) ?: "N/A",
                                // El monto DEBE estar guardado como número (Double)
                                monto = registroSnapshot.child("monto").getValue(Double::class.java) ?: 0.0,
                                categoria = registroSnapshot.child("categoria").getValue(String::class.java) ?: "Sin Categoría",
                                tipo = if (tipo == "ingresos") "Ingreso" else "Gasto"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    consultasPendientes--
                    if (consultasPendientes == 0) {
                        onSuccess(registros)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    consultasPendientes--
                    if (consultasPendientes == 0) {
                        onSuccess(registros)
                    }
                    onError("Fallo la lectura de $tipo: ${error.message}")
                }
            })
    }
}


fun eliminarRegistro(
    context: android.content.Context,
    registro: Registro,
    onComplete: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Toast.makeText(context, "Inicia sesión para eliminar movimientos", Toast.LENGTH_SHORT).show()
        return
    }

    val database = FirebaseDatabase.getInstance()
    val refPath = if (registro.tipo == "Ingreso") "ingresos" else "gastos"
    val ref = database.getReference(refPath).child(registro.id)

    ref.removeValue()
        .addOnSuccessListener {
            Toast.makeText(context, "${registro.tipo} '${registro.nombre}' eliminado", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}











