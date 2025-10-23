package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            LoginScreen(auth = auth)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                .align(Alignment.TopStart)
                .offset(y = 56.dp)
                .size(28.dp)
                .clickable {
                    if (context is ComponentActivity) context.finish()
                }
        )

        Column(
            modifier = Modifier
                .padding(top = 220.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Inicio de Sesión",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F4E9A),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Campo de correo
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo", color = Color(0xFF888888)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color(0xFF3F51B5),
                    unfocusedTextColor = Color(0xFF3F51B5),
                    cursorColor = Color(0xFF3F51B5),
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color(0xFF3F51B5)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña", color = Color(0xFF888888)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color(0xFF3F51B5),
                    unfocusedTextColor = Color(0xFF3F51B5),
                    cursorColor = Color(0xFF3F51B5),
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color(0xFF3F51B5)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F4E9A),
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .clickable {
                        val intent = Intent(context, Contraseña::class.java)
                        context.startActivity(intent)
                    }
            )

            // Botón Iniciar sesión
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, Inicio::class.java)
                                context.startActivity(intent)
                            } else {
                                val message = when (val e = task.exception) {
                                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta"
                                    is FirebaseAuthInvalidUserException -> "El correo no está registrado"
                                    else -> "Error al iniciar sesión. Verifica tus datos"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Botón Registrarse
            Button(
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text(
                    text = "Registrarse",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}



