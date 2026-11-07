
package sv.pacerunnersv.app

import android.os.Bundle
import android.util.Patterns
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import sv.pacerunnersv.app.ui.theme.PaceRunnerSVTheme

private val Negro = Color(0xFF111111)
private val VerdeNeon = Color(0xFFB6FF00)
private val Blanco = Color(0xFFFFFFFF)
private val Gris = Color(0xFFBDBDBD)

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

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    when (pantalla) {
        "login" -> PantallaLogin(
            onLoginExitoso = {
                pantalla = "inicio"
            },
            irRegistro = {
                pantalla = "registro"
            }
        )

        "registro" -> PantallaRegistro(
            volverLogin = {
                pantalla = "login"
            }
        )

        "inicio" -> PantallaPrincipal(
            cerrarSesion = {
                auth.signOut()
                pantalla = "login"
            }
        )
    }
}

@Composable
fun EncabezadoPaceRunner() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
    var visible by remember {
        mutableStateOf(false)
    }

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
            TextButton(
                onClick = { visible = !visible }
            ) {
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
    onLoginExitoso: () -> Unit,
    irRegistro: () -> Unit
) {
    var correo by remember {
        mutableStateOf("")
    }

    var contrasena by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

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
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 24.dp
            )
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
            ),
            enabled = !cargando
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
                when {
                    correo.isBlank() -> {
                        mensaje = "Ingresa tu correo electrónico."
                    }

                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim()).matches() -> {
                        mensaje = "Ingresa un correo válido."
                    }

                    contrasena.isBlank() -> {
                        mensaje = "Ingresa tu contraseña."
                    }

                    contrasena.length < 6 -> {
                        mensaje =
                            "La contraseña debe tener al menos 6 caracteres."
                    }

                    else -> {
                        cargando = true
                        mensaje = ""

                        auth.signInWithEmailAndPassword(
                            correo.trim(),
                            contrasena
                        ).addOnCompleteListener { tarea ->

                            cargando = false

                            if (tarea.isSuccessful) {
                                onLoginExitoso()
                            } else {
                                val codigo = (
                                        tarea.exception as? FirebaseAuthException
                                        )?.errorCode

                                mensaje = when (codigo) {
                                    "ERROR_INVALID_EMAIL" ->
                                        "El correo electrónico no es válido."

                                    "ERROR_USER_NOT_FOUND",
                                    "ERROR_WRONG_PASSWORD",
                                    "ERROR_INVALID_CREDENTIAL" ->
                                        "Correo o contraseña incorrectos."

                                    "ERROR_USER_DISABLED" ->
                                        "Esta cuenta está deshabilitada."

                                    "ERROR_TOO_MANY_REQUESTS" ->
                                        "Demasiados intentos. Intenta más tarde."

                                    "ERROR_NETWORK_REQUEST_FAILED" ->
                                        "Error de conexión. Verifica tu internet."

                                    else ->
                                        "No fue posible iniciar sesión. Intenta nuevamente."
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !cargando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeNeon,
                contentColor = Negro
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Negro,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "INICIAR SESIÓN",
                    fontWeight = FontWeight.Bold
                )
            }
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

        TextButton(
            onClick = irRegistro,
            enabled = !cargando
        ) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = VerdeNeon
            )
        }
    }
}

@Composable
fun PantallaRegistro(
    volverLogin: () -> Unit
) {
    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var contrasena by remember {
        mutableStateOf("")
    }

    var confirmar by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

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
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 24.dp
            )
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        CampoContrasena(
            valor = contrasena,
            alCambiar = {
                contrasena = it
                mensaje = ""
            },
            etiqueta = "Contraseña"
        )

        Spacer(modifier = Modifier.height(12.dp))

        CampoContrasena(
            valor = confirmar,
            alCambiar = {
                confirmar = it
                mensaje = ""
            },
            etiqueta = "Confirmar contraseña"
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                mensaje = when {
                    nombre.isBlank() ->
                        "Ingresa tu nombre."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim()).matches() ->
                        "Ingresa un correo válido."

                    contrasena.length < 6 ->
                        "La contraseña debe tener al menos 6 caracteres."

                    contrasena != confirmar ->
                        "Las contraseñas no coinciden."

                    else ->
                        "Formulario válido. El registro no está habilitado."
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
            Text(
                "CREAR CUENTA",
                fontWeight = FontWeight.Bold
            )
        }

        if (mensaje.isNotEmpty()) {
            Text(
                mensaje,
                color = Color(0xFFFF7777),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = volverLogin) {
            Text(
                "Ya tengo cuenta. Iniciar sesión",
                color = VerdeNeon
            )
        }
    }
}
