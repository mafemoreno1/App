package com.example.aplicacion

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class Perfil : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PerfilScreen() }
    }
}

data class UserProfile(
    val nombre: String? = "",
    val apellidos: String? = "",
    val correo: String? = "",
    val edad: String? = "",
    val genero: String? = "",
    val ingresoMensual: String? = "",
    val avatar: String? = null,
    val fotoPerfilBase64: String? = null
)

fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun PerfilScreen() {
    val azul = Color(0xFF3F51B5)
    val fondoCard = Color(0xFFD6D9FF)
    val context = LocalContext.current


    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseDatabase.getInstance().reference }

    var perfil by remember { mutableStateOf(UserProfile()) }
    var isLoading by remember { mutableStateOf(true) }
    val base64 = perfil.fotoPerfilBase64
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            error = "No has iniciado sesión"
            isLoading = false
            return@DisposableEffect onDispose { }
        }
        val ref = db.child("usuarios").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val p = snapshot.getValue(UserProfile::class.java)
                perfil = p ?: UserProfile()
                isLoading = false
            }
            override fun onCancelled(e: DatabaseError) {
                error = e.message
                isLoading = false
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Volver",
            tint = azul,
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .clickable { (context as? ComponentActivity)?.finish() }
        )

        when {
            isLoading -> {
                CircularProgressIndicator(color = azul, modifier = Modifier.align(Alignment.Center))
            }
            error != null -> {
                Text(text = error ?: "Error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!base64.isNullOrBlank()) {
                        decodeBase64ToBitmap(base64)?.let { imageBitmap ->
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(90.dp).padding(bottom = 8.dp)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = AvatarAssets.resFor(perfil.avatar)),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(90.dp).padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        text = "Tu Perfil",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = azul,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PerfilDato("Nombre", (perfil.nombre ?: "").ifBlank { "—" }, fondoCard, azul)
                    PerfilDato("Apellidos", (perfil.apellidos ?: "").ifBlank { "—" }, fondoCard, azul)
                    PerfilDato("Correo", (perfil.correo ?: "").ifBlank { "—" }, fondoCard, azul)
                    PerfilDato("Edad", (perfil.edad ?: "").ifBlank { "—" }, fondoCard, azul)
                    PerfilDato("Género", (perfil.genero ?: "").ifBlank { "—" }, fondoCard, azul)

                    val ingresoFmt = remember(perfil.ingresoMensual) {
                        val digits = (perfil.ingresoMensual ?: "").filter { it.isDigit() }
                        if (digits.isBlank()) "—"
                        else NumberFormat.getInstance(Locale("es", "CO")).apply {
                            isGroupingUsed = true
                            maximumFractionDigits = 0
                        }.format(digits.toLong())
                    }
                    PerfilDato("Ingreso mensual", ingresoFmt, fondoCard, azul)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { context.startActivity(Intent(context, EditarPerfil::class.java)) },
                        colors = ButtonDefaults.buttonColors(containerColor = azul),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth(0.9f).height(40.dp)
                    ) {
                        Text("Editar perfil", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().clickable {
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Logout, contentDescription = "Cerrar sesión", tint = azul, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontSize = 18.sp, color = azul, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilDato(label: String, valor: String, fondo: Color, colorTexto: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = fondo),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(70.dp).padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 14.sp, color = colorTexto.copy(alpha = 0.7f))
            Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        }
    }
}



