package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Inicio : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PantallaInicio()
        }
    }
}

@Composable
fun PantallaInicio() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Atrás",
            tint = Color(0xFF3F51B5),
            modifier = Modifier
                .size(45.dp)
                .align(Alignment.TopStart)
                .padding(top = 20.dp)
                .clickable {
                    (context as? ComponentActivity)?.finish()
                }
        )


        Icon(
            painter = painterResource(id = R.drawable.outline_account_circle_24),
            contentDescription = "Perfil",
            tint = Color(0xFF3F51B5),
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.TopEnd)
                .padding(top = 20.dp)
                .clickable {
                    context.startActivity(Intent(context, Perfil::class.java))
                }
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido!",
                color = Color(0xFF3F51B5),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(25.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CuadroPrincipal(
                    icon = R.drawable.outline_attach_money_24,
                    texto = "Finanzas",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable {
                            context.startActivity(Intent(context, Finanzas::class.java))
                        }
                )
                CuadroPrincipal(
                    icon = R.drawable.ic_piggy_bank,
                    texto = "Ahorros",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, AhorrosActivity::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xA63F51B5)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .width(330.dp)
                    .height(100.dp)
                    .clickable {
                        // context.startActivity(Intent(context, ConsejoFinancieroActivity::class.java))
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_lightbulb_24),
                        contentDescription = "Consejo",
                        tint = Color.White,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Consejo Financiero",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_add_circle_24),
                    contentDescription = "Registro Gasto",
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            context.startActivity(Intent(context, RegistroGasto::class.java))
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Registro Gasto",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 24.dp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.outline_add_circle_24),
                    contentDescription = "Registro Ingreso",
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            context.startActivity(Intent(context, RegistroIngreso::class.java))
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Registro Ingreso",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xA13F51B5)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Resumen Financiero",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(55.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ItemBarraInferior(R.drawable.outline_home_24, "Inicio") {
                context.startActivity(Intent(context, Inicio::class.java))
            }
            ItemBarraInferior(R.drawable.outline_notifications_24, "Alertas") {
                //context.startActivity(Intent(context, AlertasActivity::class.java))
            }
            ItemBarraInferior(R.drawable.outline_assignment_turned_in_24, "Metas") {
                // context.startActivity(Intent(context, MetasActivity::class.java))
            }
            ItemBarraInferior(R.drawable.outline_person_24, "Asistente IA") {
                //context.startActivity(Intent(context, AsistenteIAActivity::class.java))
            }
        }
    }
}


@Composable
fun CuadroPrincipal(icon: Int, texto: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xA63F51B5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .width(150.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = texto,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = texto,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun ItemBarraInferior(icon: Int, texto: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = texto,
            tint = Color(0xFF3F51B5),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = texto,
            color = Color(0xFF3F51B5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}