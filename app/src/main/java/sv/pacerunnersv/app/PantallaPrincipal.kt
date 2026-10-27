
package sv.pacerunnersv.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fondo = Color(0xFF111111)
private val Verde = Color(0xFFB6FF00)
private val TextoClaro = Color(0xFFFFFFFF)
private val TextoSecundario = Color(0xFFBDBDBD)

private data class ProductoDemo(
    val nombre: String,
    val categoria: String,
    val precio: String,
    val descripcion: String,
    val imagen: Int
)

@Composable
fun PantallaPrincipal(
    cerrarSesion: () -> Unit
) {
    var seccion by remember { mutableStateOf("Inicio") }

    val productos = listOf(
        ProductoDemo(
            nombre = "Short Running",
            categoria = "Running",
            precio = "$15.00",
            descripcion = "Short deportivo para entrenamientos.",
            imagen = R.drawable.producto_short
        ),
        ProductoDemo(
            nombre = "Top Deportivo",
            categoria = "Ropa deportiva",
            precio = "$18.00",
            descripcion = "Top deportivo para entrenamiento.",
            imagen = R.drawable.producto_top
        ),
        ProductoDemo(
            nombre = "Licra Running",
            categoria = "Running",
            precio = "$22.00",
            descripcion = "Licra deportiva para entrenamientos.",
            imagen = R.drawable.producto_licra
        ),
        ProductoDemo(
            nombre = "Camisa Deportiva",
            categoria = "Ropa deportiva",
            precio = "$16.00",
            descripcion = "Camisa ligera para actividad física.",
            imagen = R.drawable.producto_camisa
        )
    )

    Scaffold(
        containerColor = Fondo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Fondo)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PACE RUNNER SV",
                        color = Verde,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "EQUIPAMIENTO PARA RUNNING",
                        color = TextoSecundario,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(onClick = cerrarSesion) {
                    Text("Salir", color = Verde)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF202020),
                contentColor = TextoClaro
            ) {
                listOf("Inicio", "Catálogo", "Perfil").forEach { opcion ->
                    NavigationBarItem(
                        selected = seccion == opcion,
                        onClick = { seccion = opcion },
                        icon = {
                            Text(
                                text = when (opcion) {
                                    "Inicio" -> "⌂"
                                    "Catálogo" -> "▦"
                                    else -> "●"
                                },
                                fontSize = 20.sp
                            )
                        },
                        label = { Text(opcion) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Verde,
                            selectedTextColor = Verde,
                            indicatorColor = Color(0xFF343D20),
                            unselectedIconColor = TextoSecundario,
                            unselectedTextColor = TextoSecundario
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (seccion) {
            "Inicio" -> InicioContenido(
                productos = productos,
                modifier = Modifier.padding(padding),
                mostrarCatalogo = { seccion = "Catálogo" }
            )

            "Catálogo" -> CatalogoContenido(
                productos = productos,
                modifier = Modifier.padding(padding)
            )

            "Perfil" -> PerfilContenido(
                modifier = Modifier.padding(padding),
                cerrarSesion = cerrarSesion
            )
        }
    }
}

@Composable
private fun InicioContenido(
    productos: List<ProductoDemo>,
    modifier: Modifier = Modifier,
    mostrarCatalogo: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF252A1B),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(22.dp)
            ) {
                Text(
                    text = "TU PRÓXIMO KILÓMETRO EMPIEZA AQUÍ",
                    color = Verde,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Corre con tu propio ritmo.",
                    color = TextoClaro,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Descubrí ropa y accesorios para tus entrenamientos.",
                    color = TextoSecundario
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = mostrarCatalogo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Verde,
                        contentColor = Fondo
                    )
                ) {
                    Text(
                        text = "EXPLORAR CATÁLOGO",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = "Productos destacados",
                color = TextoClaro,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(productos.take(2)) { producto ->
            TarjetaProducto(producto)
        }
    }
}

@Composable
private fun CatalogoContenido(
    productos: List<ProductoDemo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(
                    start = 4.dp,
                    bottom = 4.dp
                )
            ) {
                Text(
                    text = "Nuestro catálogo",
                    color = TextoClaro,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ropa y accesorios deportivos",
                    color = TextoSecundario
                )
            }
        }

        items(productos) { producto ->
            TarjetaProducto(producto)
        }
    }
}

@Composable
private fun TarjetaProducto(producto: ProductoDemo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF202020),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = producto.imagen),
            contentDescription = producto.nombre,
            modifier = Modifier
                .width(104.dp)
                .height(124.dp)
                .background(
                    Color(0xFF303030),
                    RoundedCornerShape(10.dp)
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = producto.nombre,
                color = TextoClaro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = producto.categoria,
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = producto.descripcion,
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = producto.precio,
                color = Verde,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun PerfilContenido(
    modifier: Modifier = Modifier,
    cerrarSesion: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    Color(0xFF343D20),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PR",
                color = Verde,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Mi perfil",
            color = TextoClaro,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Perfil de demostración",
            color = TextoSecundario
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = cerrarSesion,
            colors = ButtonDefaults.buttonColors(
                containerColor = Verde,
                contentColor = Fondo
            )
        ) {
            Text(
                text = "CERRAR SESIÓN",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
