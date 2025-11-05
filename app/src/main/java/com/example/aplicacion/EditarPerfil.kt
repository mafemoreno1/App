package com.example.aplicacion

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.InputStream
import java.text.NumberFormat
import java.util.*

class EditarPerfil : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent { EditarPerfilScreen() }
    }
}

fun formatGrouping(digits: String, locale: Locale = Locale("es", "CO")): String {
    val n = digits.toLongOrNull() ?: return ""
    return NumberFormat.getInstance(locale).apply {
        isGroupingUsed = true
        maximumFractionDigits = 0
    }.format(n)
}

fun encodeImageToBase64(context: Context, uri: Uri): String {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes() ?: return ""
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun base64ToImageBitmap(base64: String): ImageBitmap {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap.asImageBitmap()
}

@Composable
fun EditarPerfilScreen() {
    val azul = Color(0xFF3F51B5)
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseDatabase.getInstance().reference }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var ingresoRaw by remember { mutableStateOf("") }
    var ingresoFmt by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("ic_avatar_1") }
    var isLoading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var base64Image by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            base64Image = encodeImageToBase64(context, it)
        }
    }


    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            error = "No has iniciado sesión"
            isLoading = false
            return@LaunchedEffect
        }

        db.child("usuarios").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                apellidos = snapshot.child("apellidos").getValue(String::class.java) ?: ""
                correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                edad = snapshot.child("edad").getValue(String::class.java) ?: ""
                genero = snapshot.child("genero").getValue(String::class.java) ?: ""
                val ingresoDb = snapshot.child("ingresoMensual").getValue(String::class.java) ?: ""
                ingresoRaw = ingresoDb.filter { it.isDigit() }
                ingresoFmt = if (ingresoRaw.isBlank()) "" else formatGrouping(ingresoRaw)
                avatar = snapshot.child("avatar").getValue(String::class.java) ?: "ic_avatar_1"
                base64Image = snapshot.child("fotoPerfilBase64").getValue(String::class.java)
                isLoading = false
            }

            override fun onCancelled(error2: DatabaseError) {
                error = error2.message
                isLoading = false
            }
        })
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

        if (isLoading) {
            CircularProgressIndicator(
                color = azul,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(top = 40.dp, bottom = 16.dp)
                    .verticalScroll(scroll)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Editar Perfil", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = azul)
                Spacer(modifier = Modifier.height(20.dp))

                // Imagen de perfil o avatar actual
                if (!base64Image.isNullOrBlank()) {
                    Image(
                        bitmap = base64ToImageBitmap(base64Image!!),
                        contentDescription = "Imagen de Perfil",
                        modifier = Modifier.size(120.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = AvatarAssets.resFor(avatar)),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Seleccionar Imagen de Perfil")
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampoEditable("Nombre", nombre) { nombre = it.filter { c -> c.isLetter() || c == ' ' } }
                CampoEditable("Apellidos", apellidos) { apellidos = it.filter { c -> c.isLetter() || c == ' ' } }
                CampoSoloLectura("Correo", correo)
                CampoEditable("Edad", edad, KeyboardType.Number) { edad = it.filter { c -> c.isDigit() }.take(2) }

                SelectorGenero("Género", genero) { genero = it }

                OutlinedTextField(
                    value = ingresoFmt,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        ingresoRaw = digits
                        ingresoFmt = if (digits.isBlank()) "" else formatGrouping(digits)
                    },
                    label = { Text("Ingreso mensual", color = azul) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = azul,
                        unfocusedIndicatorColor = Color(0xFFB0B0B0)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(10.dp))
                Text("Elige tu avatar", color = azul, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                // 🔹 Aquí está el cambio importante
                AvatarPicker(selectedKey = avatar, onSelect = {
                    avatar = it
                    base64Image = null // 🔸 Si elige un avatar, se borra la imagen previa
                })

                Spacer(modifier = Modifier.height(28.dp))

                val edadInt = edad.toIntOrNull()
                val edadValida = edadInt != null && edadInt in 18..99
                val ingresoValido = ingresoRaw.isNotBlank()
                val nombresValidos = nombre.isNotBlank() && apellidos.isNotBlank()

                if (!nombresValidos) Text("Nombre y apellidos: solo letras y espacios", color = Color(0xFFD32F2F), fontSize = 12.sp)
                if (!edadValida) Text("Edad mínima 18 (2 dígitos)", color = Color(0xFFD32F2F), fontSize = 12.sp)
                if (!ingresoValido) Text("Ingreso mensual debe ser numérico", color = Color(0xFFD32F2F), fontSize = 12.sp)

                Button(
                    onClick = {
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            Toast.makeText(context, "No has iniciado sesión", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!nombresValidos || !edadValida || !ingresoValido) {
                            Toast.makeText(context, "Datos inválidos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        saving = true
                        val data = mutableMapOf(
                            "nombre" to nombre,
                            "apellidos" to apellidos,
                            "edad" to edad,
                            "genero" to genero,
                            "ingresoMensual" to ingresoRaw,
                            "avatar" to avatar
                        )

                        // 🔹 Si base64Image es null, elimina la foto anterior
                        if (base64Image != null) data["fotoPerfilBase64"] = base64Image!!
                        else data["fotoPerfilBase64"] = ""

                        db.child("usuarios").child(uid).updateChildren(data as Map<String, Any>)
                            .addOnSuccessListener {
                                saving = false
                                Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                                (context as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener { e ->
                                saving = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = azul),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(if (saving) "Guardando..." else "Guardar cambios", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoEditable(
    label: String,
    value: String,
    tipo: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    val azul = Color(0xFF3F51B5)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = azul) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = azul,
            unfocusedIndicatorColor = Color(0xFFB0B0B0)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = tipo)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSoloLectura(
    label: String,
    value: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label, color = Color(0xFF3F51B5)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color(0xFFF5F5F5),
            disabledTextColor = Color(0xFF666666),
            disabledIndicatorColor = Color(0xFFB0B0B0),
            disabledLabelColor = Color(0xFF3F51B5)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorGenero(
    label: String,
    value: String,
    onSelect: (String) -> Unit
) {
    val azul = Color(0xFF3F51B5)
    val opciones = listOf("Femenino", "Masculino", "Otro", "Prefiero no decir")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = azul) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = azul,
                unfocusedIndicatorColor = Color(0xFFB0B0B0)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSelect(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AvatarPicker(
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    // grilla 3 columnas con las claves centralizadas en AvatarAssets
    Column(Modifier.fillMaxWidth()) {
        for (row in AvatarAssets.keys.chunked(3)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key -> // key: String
                    val res = AvatarAssets.resFor(key)
                    val isSelected = key == selectedKey
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onSelect(key) },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) Color(0xFFD6D9FF) else Color(0xFFF7F7FF)
                        ),
                        elevation = CardDefaults.elevatedCardElevation(if (isSelected) 6.dp else 1.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = res),
                                contentDescription = key,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

