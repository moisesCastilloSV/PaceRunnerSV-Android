
package sv.pacerunnersv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sv.pacerunnersv.app.ui.theme.PaceRunnerSVTheme

private val Negro = Color(0xFF111111)
private val VerdeNeon = Color(0xFFB6FF00)
private val Blanco = Color(0xFFFFFFFF)
private val Gris = Color(0xFFBDBDBD)

private const val CORREO_PRUEBA = "demo@pacerunnersv.com"
private const val CLAVE_PRUEBA = "Runner123"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaceRunnerSVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Negro
                ) {
                    AppAutenticacion()
                }
            }
        }
    }
}

@Composable
fun AppAutenticacion() {
    var pantalla by remember { mutableStateOf("login") }

    when (pantalla) {
        "login" -> PantallaLogin(
            irRegistro = { pantalla = "registro" },
            iniciarSesion = { correo, clave ->
                if (
                    correo.trim().equals(CORREO_PRUEBA, ignoreCase = true) &&
                    clave == CLAVE_PRUEBA
                ) {
                    pantalla = "inicio"
                    true
                } else {
                    false
                }
            }
        )

        "registro" -> PantallaRegistro(
            volverLogin = { pantalla = "login" }
        )

        "inicio" -> PantallaPrincipal(
            cerrarSesion = { pantalla = "login" }
        )
    }
}

@Composable
fun EncabezadoPaceRunner() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "PACE RUNNER SV",
            color = VerdeNeon,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SUPERA TUS LÍMITES",
            color = Blanco,
            fontSize = 13.sp,
            letterSpacing = 3.sp
        )
    }
}

@Composable
fun CampoContrasena(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(
                    text = if (visible) "Ocultar" else "Ver",
                    color = VerdeNeon
                )
            }
        }
    )
}

@Composable
fun PantallaLogin(
    irRegistro: () -> Unit,
    iniciarSesion: (String, String) -> Boolean
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EncabezadoPaceRunner()
        Spacer(modifier = Modifier.height(38.dp))

        Text(
            text = "Bienvenido",
            color = Blanco,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Inicia sesión para continuar",
            color = Gris,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        CampoContrasena(
            valor = contrasena,
            alCambiar = {
                contrasena = it
                mensaje = ""
            },
            etiqueta = "Contraseña"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                mensaje = when {
                    correo.isBlank() ->
                        "Ingresa tu correo electrónico."
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim()).matches() ->
                        "Ingresa un correo válido."
                    contrasena.isBlank() ->
                        "Ingresa tu contraseña."
                    contrasena.length < 6 ->
                        "La contraseña debe tener al menos 6 caracteres."
                    iniciarSesion(correo, contrasena) -> ""
                    else ->
                        "Credenciales incorrectas. Verifica tus datos."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeNeon,
                contentColor = Negro
            )
        ) {
            Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = Color(0xFFFF7777),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = irRegistro) {
            Text(
                "¿No tienes cuenta? Regístrate",
                color = VerdeNeon
            )
        }
    }
}

@Composable
fun PantallaRegistro(
    volverLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EncabezadoPaceRunner()
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Crear cuenta",
            color = Blanco,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Regístrate en PaceRunnerSV",
            color = Gris,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; mensaje = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; mensaje = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CampoContrasena(
            valor = contrasena,
            alCambiar = { contrasena = it; mensaje = "" },
            etiqueta = "Contraseña"
        )

        Spacer(modifier = Modifier.height(12.dp))

        CampoContrasena(
            valor = confirmar,
            alCambiar = { confirmar = it; mensaje = "" },
            etiqueta = "Confirmar contraseña"
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                mensaje = when {
                    nombre.isBlank() -> "Ingresa tu nombre."
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim()).matches() ->
                        "Ingresa un correo válido."
                    contrasena.length < 6 ->
                        "La contraseña debe tener al menos 6 caracteres."
                    contrasena != confirmar ->
                        "Las contraseñas no coinciden."
                    else ->
                        "Formulario válido. Registro real pendiente."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeNeon,
                contentColor = Negro
            )
        ) {
            Text("CREAR CUENTA", fontWeight = FontWeight.Bold)
        }

        if (mensaje.isNotEmpty()) {
            Text(
                mensaje,
                color = if (mensaje.startsWith("Formulario válido")) {
                    VerdeNeon
                } else {
                    Color(0xFFFF7777)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = volverLogin) {
            Text("Ya tengo cuenta. Iniciar sesión", color = VerdeNeon)
        }
    }
}
