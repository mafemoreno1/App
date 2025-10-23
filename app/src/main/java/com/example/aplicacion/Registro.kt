package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp)
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Volver",
            tint = Color(0xFF3F51B5),
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = 56.dp)
                .clickable {
                    if (context is ComponentActivity) context.finish()
                }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Fin-IA",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F4E9A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crear una cuenta",
                fontSize = 20.sp,
                color = Color(0xFF5368C8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CampoTexto("Nombre", nombre) { nombre = it }
            CampoTexto("Apellidos", apellidos) { apellidos = it }
            CampoTexto("Correo", email, tipo = KeyboardType.Email) { email = it }
            CampoTexto("Contraseña", password, password = true) { password = it }
            CampoTexto("Confirmar contraseña", confirmPassword, password = true) { confirmPassword = it }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    when {
                        nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                        val userData = mapOf(
                                            "nombre" to nombre,
                                            "apellidos" to apellidos,
                                            "correo" to email
                                        )

                                        database.child("usuarios").child(userId).setValue(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                                context.startActivity(Intent(context, LoginActivity::class.java))
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text(
                    text = "Resgistrarse",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTexto(
    placeholder: String,
    value: String,
    tipo: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Color(0xFF888888)) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(bottom = 16.dp),
        textStyle = LocalTextStyle.current.copy(color = Color(0xFF3F51B5)),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            focusedBorderColor = Color(0xFF3F51B5),
            unfocusedBorderColor = Color(0xFF3F51B5),
            cursorColor = Color(0xFF3F51B5)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        shape = RoundedCornerShape(14.dp)
    )
}
