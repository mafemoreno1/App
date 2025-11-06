package com.example.aplicacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

data class Ahorro(
    val id: String = "",
    val nombre: String = "",
    val monto: String = "0",
    val meta: String = "0",
    val fecha: String = "",
    val fondo: String = ""
)


fun cleanToDouble(montoStr: String): Double {
    val clean = montoStr.replace(".", "").replace(",", "")
    return clean.toDoubleOrNull() ?: 0.0
}

fun formatToCOP(number: Double): String {
    val symbols = DecimalFormatSymbols(Locale("es", "CO")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("'$'#,##0", symbols)
    return formatter.format(number)
}

fun formatToFirebaseString(number: Double): String {
    val symbols = DecimalFormatSymbols(Locale("es", "CO")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,##0", symbols)
    return formatter.format(number)
}


fun actualizarMontoEnFirebase(context: Context, ahorro: Ahorro, adicional: Double) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val ref = FirebaseDatabase.getInstance().getReference("ahorros").child(user.uid).child(ahorro.id)
    val alertasRef = FirebaseDatabase.getInstance().getReference("alertas").child(user.uid)

    val montoActualD = cleanToDouble(ahorro.monto)
    val nuevoMontoD = montoActualD + adicional
    val nuevoMontoStr = formatToFirebaseString(nuevoMontoD)
    val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date())
    val updates = hashMapOf<String, Any>(
        "monto" to nuevoMontoStr,
        "fecha" to fechaActual
    )
    ref.updateChildren(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "¡${formatToCOP(adicional)} agregados!", Toast.LENGTH_SHORT).show()


            val idAlerta = alertasRef.push().key
            if (idAlerta != null) {
                val alerta = mapOf(
                    "id" to idAlerta,
                    "titulo" to "Ahorro actualizado",
                    "mensaje" to "Agregaste ${formatToCOP(adicional)} al ahorro '${ahorro.nombre}'.",
                    "tipo" to "ahorro",
                    "fecha" to System.currentTimeMillis(),
                    "leida" to false
                )
                alertasRef.child(idAlerta).setValue(alerta)
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

fun eliminarAhorro(context: Context, id: String, nombre: String) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val ref = FirebaseDatabase.getInstance().getReference("ahorros").child(user.uid).child(id)
    val alertasRef = FirebaseDatabase.getInstance().getReference("alertas").child(user.uid)

    ref.removeValue()
        .addOnSuccessListener {
            Toast.makeText(context, "Ahorro '$nombre' eliminado.", Toast.LENGTH_SHORT).show()


            val idAlerta = alertasRef.push().key
            if (idAlerta != null) {
                val alerta = mapOf(
                    "id" to idAlerta,
                    "titulo" to "Ahorro eliminado",
                    "mensaje" to "El ahorro '$nombre' fue eliminado correctamente.",
                    "tipo" to "ahorro",
                    "fecha" to System.currentTimeMillis(),
                    "leida" to false
                )
                alertasRef.child(idAlerta).setValue(alerta)
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}



class AhorrosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            AhorrosScreen(
                onVolverClick = { finish() },
                onAgregarClick = { startActivity(Intent(this, AgregarAhorroActivity::class.java)) },
                onInicioClick = { startActivity(Intent(this, Inicio::class.java)) },
                onAlertasClick = {startActivity(Intent(this, AlertasActivity::class.java)) },
                onMetasClick = { startActivity(Intent(this, Metas::class.java)) },
                onAsistenteClick = {}
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AhorrosScreen(
    onVolverClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onInicioClick: () -> Unit,
    onAlertasClick: () -> Unit,
    onMetasClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    val context = LocalContext.current
    var ahorros by remember { mutableStateOf<List<Ahorro>>(emptyList()) }
    var ahorroToDelete by remember { mutableStateOf<Ahorro?>(null) }


    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val ref = FirebaseDatabase.getInstance().getReference("ahorros").child(uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Ahorro>()
                for (dato in snapshot.children) {
                    val ahorro = dato.getValue(Ahorro::class.java)?.copy(id = dato.key ?: "")
                    if (ahorro != null) lista.add(ahorro)
                }
                ahorros = lista
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al leer ahorros", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Color(0xFF3F4E9A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = "Ahorros",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F4E9A)
                )
            }
        },

        bottomBar = { BottomBar(onInicioClick, onAlertasClick, onMetasClick, onAsistenteClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(ahorros) { ahorro ->
                    AhorroItem(
                        ahorro = ahorro,
                        onEliminarClick = { ahorroToDelete = ahorro }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onAgregarClick,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(40.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F4E9A))
                ) {
                    Text("Agregar ahorro", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }


    ahorroToDelete?.let { ahorro ->
        DeleteConfirmationDialog(
            ahorroNombre = ahorro.nombre,
            onConfirm = { eliminarAhorro(context, ahorro.id, ahorro.nombre); ahorroToDelete = null },
            onDismiss = { ahorroToDelete = null }
        )
    }
}

@Composable
fun AhorroItem(
    ahorro: Ahorro,
    onEliminarClick: () -> Unit
) {
    val context = LocalContext.current
    val montoDouble = cleanToDouble(ahorro.monto)
    val metaDouble = cleanToDouble(ahorro.meta).coerceAtLeast(1.0)
    val progreso = (montoDouble / metaDouble).toFloat().coerceIn(0f, 1f)
    val montoStr = formatToCOP(montoDouble)
    val metaStr = formatToCOP(metaDouble)

    var mostrarAgregarDialog by remember { mutableStateOf(false) }
    var montoDialog by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCDD4FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(ahorro.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF3F4E9A))
            Text("Fecha: ${ahorro.fecha}", fontSize = 12.sp, color = Color.Gray)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(montoStr, fontWeight = FontWeight.SemiBold, color = Color(0xFF3F4E9A))
                Text(metaStr, color = Color(0xFF3F4E9A))
            }

            LinearProgressIndicator(
                progress = progreso,
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFD9D9D9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { mostrarAgregarDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A7FFF)),
                    modifier = Modifier.weight(1f).height(40.dp).padding(end = 8.dp)
                ) { Text("Agregar dinero", fontSize = 12.sp) }

                Button(
                    onClick = onEliminarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).height(40.dp).padding(start = 8.dp)
                ) { Text("Eliminar", fontSize = 12.sp) }
            }
        }
    }

    if (mostrarAgregarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarAgregarDialog = false },
            title = { Text("Agregar dinero a ${ahorro.nombre}") },
            text = {
                OutlinedTextField(
                    value = montoDialog,
                    onValueChange = { montoDialog = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monto a agregar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val adicional = montoDialog.toDoubleOrNull()
                    if (adicional != null && adicional > 0) {
                        actualizarMontoEnFirebase(context, ahorro, adicional)
                        montoDialog = ""
                        mostrarAgregarDialog = false
                    } else {
                        Toast.makeText(context, "Monto inválido.", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Agregar") }
            },
            dismissButton = {
                Button(onClick = { mostrarAgregarDialog = false; montoDialog = "" }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(ahorroNombre: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Seguro quieres eliminar '$ahorroNombre'?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Sí")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BottomBar(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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





































