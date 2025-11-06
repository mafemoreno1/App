package com.example.aplicacion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class Metas : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetasScreen()
        }
    }
}


data class TwelveQuoteResponse(
    val symbol: String?,
    val name: String?,
    val price: String?,
    val percent_change: String?,
    val currency: String?
)

interface TwelveDataApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): TwelveQuoteResponse
}

data class MarketRow(
    val displayName: String,
    val symbol: String,
    val price: String?,
    val change: String?
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen() {
    val context = LocalContext.current
    val azulPrincipal = Color(0xFF3F51B5)
    val verde = Color(0xFF00C853)
    val rojo = Color(0xFFD50000)
    val fondo = Color.White

    val symbols = listOf(
        Pair("S&P 500", "SPX"),
        Pair("NASDAQ", "IXIC"),
        Pair("Dow Jones", "DJI"),
        Pair("FTSE 100", "FTSE"),
        Pair("DAX", "DAX"),
        Pair("Nikkei 225", "N225"),
        Pair("COLCAP (Colombia)", "COLCAP"),
        Pair("Bitcoin", "BTC/USD"),
        Pair("USD/COP", "USD/COP"),
        Pair("EUR/USD", "EUR/USD"),
        Pair("GBP/USD", "GBP/USD"),
        Pair("USD/JPY", "USD/JPY")
    )

    var rows by remember { mutableStateOf(listOf<MarketRow>()) }
    var cargando by remember { mutableStateOf(false) }
    var consejo by remember { mutableStateOf("Cargando datos...") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    val client = OkHttpClient.Builder().addInterceptor(logging).build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.twelvedata.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api = retrofit.create(TwelveDataApi::class.java)
    val apiKey = "50c95f2eeda3498f8621c8fd6e70c7ee"

    suspend fun fetchAll(): List<MarketRow> = withContext(Dispatchers.IO) {
        val deferreds = symbols.map { (display, sym) ->
            async {
                try {
                    val resp = api.getQuote(sym, apiKey)
                    MarketRow(
                        displayName = display,
                        symbol = resp.symbol ?: sym,
                        price = resp.price ?: "--",
                        change = resp.percent_change ?: "0"
                    )
                } catch (e: Exception) {
                    MarketRow(display, sym, "--", null)
                }
            }
        }
        deferreds.awaitAll()
    }

    val scope = rememberCoroutineScope()

    fun actualizarDatosOnce() {
        scope.launch {
            cargando = true
            errorMsg = null
            try {
                val lista = fetchAll()
                val usdCopRow = lista.find { it.symbol.equals("USD/COP", ignoreCase = true) }
                val cambioUsdCop = usdCopRow?.change?.toDoubleOrNull() ?: 0.0

                consejo = when {
                    cambioUsdCop > 0.3 -> "El dólar sube — podrías vender."
                    cambioUsdCop < -0.3 -> "El dólar bajó — buena oportunidad para comprar."
                    else -> "El mercado está estable — mantén tu inversión."
                }

                rows = lista
            } catch (e: Exception) {
                errorMsg = "Error: ${e.message ?: "falló la petición"}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        actualizarDatosOnce()
        while (true) {
            delay(30_000L)
            actualizarDatosOnce()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp)) // espacio superior

            // Cabecera con flecha y título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    tint = azulPrincipal,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { context.startActivity(Intent(context, Inicio::class.java)) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Metas - Mercados",
                    color = azulPrincipal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Mercados & Divisas (tiempo real)",
                fontSize = 20.sp,
                color = azulPrincipal,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Card de encabezados
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activo", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("Precio", fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    Text("Cambio", fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
            }
        }

        // Items de mercados
        items(rows) { r ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(r.displayName, fontWeight = FontWeight.Medium, color = Color(0xFF2C2C54), modifier = Modifier.weight(2f))
                Text(r.price ?: "--", textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                val changeVal = r.change?.toDoubleOrNull()
                val changeText = r.change?.let {
                    if (it.startsWith("-")) it else if (!it.startsWith("+")) "+$it" else it
                } ?: "--"
                Text(
                    text = changeText,
                    textAlign = TextAlign.End,
                    color = when {
                        changeVal == null -> Color(0xFF7E7E7E)
                        changeVal > 0 -> verde
                        changeVal < 0 -> rojo
                        else -> Color(0xFF7E7E7E)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Divider(color = Color(0xFFE0E0E0))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Card de consejo
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = consejo, color = Color(0xFF4A4A4A))
                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = it, color = Color(0xFFD32F2F), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón centrado
            Button(
                onClick = { actualizarDatosOnce() },
                colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(text = "Actualizar ahora", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}





