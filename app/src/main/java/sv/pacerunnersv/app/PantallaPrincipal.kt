
package sv.pacerunnersv.app

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private val Fondo = Color(0xFF111111)
private val Verde = Color(0xFFB6FF00)
private val TextoClaro = Color.White
private val TextoSecundario = Color(0xFFBDBDBD)

private data class Producto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val talla: String,
    val color: String,
    val stock: Int,
    val activo: Boolean
)

@Composable
fun PantallaPrincipal(
    cerrarSesion: () -> Unit
) {
    var seccion by remember { mutableStateOf("Inicio") }
    var esAdministrador by remember { mutableStateOf(false) }
    var rolConsultado by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var productoEditar by remember { mutableStateOf<Producto?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    // Consultar el rol del usuario autenticado.
    DisposableEffect(uid) {
        if (uid == null) {
            rolConsultado = true
        } else {
            val listener = db.collection("usuarios")
                .document(uid)
                .addSnapshotListener { documento, error ->
                    if (error != null) {
                        mensaje = "No se pudo consultar el perfil."
                        rolConsultado = true
                    } else {
                        esAdministrador =
                            documento?.getString("rol") == "administrador"
                        rolConsultado = true
                    }
                }

            onDispose { listener.remove() }
        }

        onDispose { }
    }

    // Escuchar cambios en la colección de productos.
    DisposableEffect(Unit) {
        val listener = db.collection("productos")
            .addSnapshotListener { resultado, error ->
                cargando = false

                if (error != null) {
                    mensaje = "Error al consultar los productos."
                } else {
                    productos = resultado?.documents
                        ?.mapNotNull { documento ->
                            val precio = documento.getDouble("precio")
                                ?: documento.getLong("precio")?.toDouble()
                                ?: 0.0

                            val stock = documento.getLong("stock")?.toInt() ?: 0

                            Producto(
                                id = documento.id,
                                nombre = documento.getString("nombre") ?: "",
                                categoria = documento.getString("categoria") ?: "",
                                descripcion = documento.getString("descripcion") ?: "",
                                precio = precio,
                                talla = documento.getString("talla") ?: "",
                                color = documento.getString("color") ?: "",
                                stock = stock,
                                activo = documento.getBoolean("activo") ?: true
                            )
                        } ?: emptyList()
                }
            }

        onDispose { listener.remove() }
    }

    val productosActivos = productos.filter { it.activo }

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
                        "PACE RUNNER SV",
                        color = Verde,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "EQUIPAMIENTO PARA RUNNING",
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
                containerColor = Color(0xFF202020)
            ) {
                val opciones = if (esAdministrador) {
                    listOf("Inicio", "Catálogo", "Inventario", "Perfil")
                } else {
                    listOf("Inicio", "Catálogo", "Perfil")
                }

                opciones.forEach { opcion ->
                    NavigationBarItem(
                        selected = seccion == opcion,
                        onClick = { seccion = opcion },
                        icon = {
                            Text(
                                when (opcion) {
                                    "Inicio" -> "⌂"
                                    "Catálogo" -> "▦"
                                    "Inventario" -> "▤"
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
                productos = productosActivos,
                modifier = Modifier.padding(padding),
                mostrarCatalogo = { seccion = "Catálogo" }
            )

            "Catálogo" -> CatalogoContenido(
                productos = productosActivos,
                modifier = Modifier.padding(padding),
                cargando = cargando
            )

            "Inventario" -> {
                if (esAdministrador && rolConsultado) {
                    InventarioContenido(
                        productos = productos,
                        modifier = Modifier.padding(padding),
                        abrirNuevo = {
                            productoEditar = null
                            mostrarFormulario = true
                        },
                        editar = { producto ->
                            productoEditar = producto
                            mostrarFormulario = true
                        },
                        cambiarEstado = { producto, nuevoEstado ->
                            val puedeCambiar = if (nuevoEstado) {
                                producto.stock > 0
                            } else {
                                producto.stock == 0
                            }

                            if (!puedeCambiar) {
                                mensaje = if (nuevoEstado) {
                                    "No puedes activar un producto sin existencias."
                                } else {
                                    "No puedes marcar como agotado un producto que tiene existencias."
                                }
                            } else {
                                db.collection("productos")
                                    .document(producto.id)
                                    .update("activo", nuevoEstado)
                                    .addOnSuccessListener {
                                        mensaje = if (nuevoEstado) {
                                            "Producto activado correctamente."
                                        } else {
                                            "Producto marcado como agotado."
                                        }
                                    }
                                    .addOnFailureListener {
                                        mensaje = "No se pudo actualizar el estado del producto."
                                    }
                            }
                        }
                    )
                }
            }

            "Perfil" -> PerfilContenido(
                modifier = Modifier.padding(padding),
                correo = auth.currentUser?.email ?: "",
                cerrarSesion = cerrarSesion
            )
        }
    }

    if (mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mensaje = "" },
            title = { Text("PaceRunnerSV") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { mensaje = "" }) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (mostrarFormulario && esAdministrador) {
        ProductoFormularioDialog(
            producto = productoEditar,
            cerrar = { mostrarFormulario = false },
            guardar = { nombre, categoria, descripcion, precio,
                        talla, color, stock, productoId ->

                val datos = hashMapOf<String, Any>(
                    "nombre" to nombre,
                    "categoria" to categoria,
                    "descripcion" to descripcion,
                    "precio" to precio,
                    "talla" to talla,
                    "color" to color,
                    "stock" to stock,
                    "activo" to (stock > 0)
                )

                val operacion = if (productoId == null) {
                    db.collection("productos").add(datos)
                } else {
                    db.collection("productos")
                        .document(productoId)
                        .update(datos)
                }

                operacion
                    .addOnSuccessListener {
                        mostrarFormulario = false
                        mensaje = if (productoId == null) {
                            "Producto registrado correctamente."
                        } else {
                            "Producto actualizado correctamente."
                        }
                    }
                    .addOnFailureListener {
                        mensaje = "No se pudo guardar el producto. Verifica tus permisos."
                    }
            }
        )
    }
}

@Composable
private fun InicioContenido(
    productos: List<Producto>,
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
                    "TU PRÓXIMO KILÓMETRO EMPIEZA AQUÍ",
                    color = Verde,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Corre con tu propio ritmo.",
                    color = TextoClaro,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Descubrí ropa y accesorios para tus entrenamientos.",
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
                    Text("EXPLORAR CATÁLOGO")
                }
            }
        }

        item {
            Text(
                "Productos destacados",
                color = TextoClaro,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(productos.take(2), key = { it.id }) { producto ->
            TarjetaProducto(producto)
        }
    }
}

@Composable
private fun CatalogoContenido(
    productos: List<Producto>,
    modifier: Modifier = Modifier,
    cargando: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            "Nuestro catálogo",
            color = TextoClaro,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            "Ropa y accesorios deportivos",
            color = TextoSecundario
        )

        when {
            cargando -> CircularProgressIndicator(
                color = Verde,
                modifier = Modifier.padding(24.dp)
            )

            productos.isEmpty() -> Text(
                "Todavía no hay productos disponibles.",
                color = TextoSecundario,
                modifier = Modifier.padding(20.dp)
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(producto)
                }
            }
        }
    }
}

@Composable
private fun InventarioContenido(
    productos: List<Producto>,
    modifier: Modifier = Modifier,
    abrirNuevo: () -> Unit,
    editar: (Producto) -> Unit,
    cambiarEstado: (Producto, Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Administración de inventario",
            color = TextoClaro,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Registra y actualiza tus productos",
            color = TextoSecundario
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = abrirNuevo,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Verde,
                contentColor = Fondo
            )
        ) {
            Text(
                "+ REGISTRAR PRODUCTO",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Productos registrados: ${productos.size}",
            color = Verde,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF202020),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        producto.nombre,
                        color = TextoClaro,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${producto.categoria} | ${producto.talla} | ${producto.color}",
                        color = TextoSecundario,
                        fontSize = 12.sp
                    )

                    Text(
                        "Precio: $${"%.2f".format(producto.precio)}",
                        color = Verde
                    )

                    Text(
                        "Existencias: ${producto.stock}",
                        color = TextoClaro
                    )

                    Text(
                        if (producto.activo) "Estado: ACTIVO" else "Estado: AGOTADO",
                        color = if (producto.activo) Verde else Color(0xFFFF7777),
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = { editar(producto) }
                    ) {
                        Text("EDITAR PRODUCTO", color = Verde)
                    }

                    TextButton(
                        onClick = {
                            cambiarEstado(producto, !producto.activo)
                        },
                        enabled = if (producto.activo) {
                            producto.stock == 0
                        } else {
                            producto.stock > 0
                        }
                    ) {
                        Text(
                            if (producto.activo) "MARCAR COMO AGOTADO"
                            else "ACTIVAR PRODUCTO",
                            color = if (
                                (producto.activo && producto.stock == 0) ||
                                (!producto.activo && producto.stock > 0)
                            ) Verde else TextoSecundario
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(producto: Producto) {
    val imagen = when {
        producto.nombre.contains("short", ignoreCase = true) ->
            R.drawable.producto_short
        producto.nombre.contains("top", ignoreCase = true) ->
            R.drawable.producto_top
        producto.nombre.contains("licra", ignoreCase = true) ->
            R.drawable.producto_licra
        producto.nombre.contains("camisa", ignoreCase = true) ->
            R.drawable.producto_camisa
        else -> R.drawable.producto_short
    }

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
            painter = painterResource(id = imagen),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                producto.nombre,
                color = TextoClaro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                producto.categoria,
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                producto.descripcion,
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Text(
                "Talla: ${producto.talla} | Color: ${producto.color}",
                color = TextoSecundario,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "$${"%.2f".format(producto.precio)}",
                color = Verde,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                "Disponibles: ${producto.stock}",
                color = TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProductoFormularioDialog(
    producto: Producto?,
    cerrar: () -> Unit,
    guardar: (
        String, String, String, Double,
        String, String, Int, String?
    ) -> Unit
) {
    var nombre by remember(producto?.id) {
        mutableStateOf(producto?.nombre ?: "")
    }
    var categoria by remember(producto?.id) {
        mutableStateOf(producto?.categoria ?: "")
    }
    var descripcion by remember(producto?.id) {
        mutableStateOf(producto?.descripcion ?: "")
    }
    var precio by remember(producto?.id) {
        mutableStateOf(producto?.precio?.toString() ?: "")
    }
    var talla by remember(producto?.id) {
        mutableStateOf(producto?.talla ?: "")
    }
    var color by remember(producto?.id) {
        mutableStateOf(producto?.color ?: "")
    }
    var stock by remember(producto?.id) {
        mutableStateOf(producto?.stock?.toString() ?: "")
    }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = cerrar,
        containerColor = Color(0xFF202020),
        title = {
            Text(
                if (producto == null) "Registrar producto"
                else "Actualizar producto",
                color = Verde
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = talla,
                    onValueChange = { talla = it },
                    label = { Text("Talla") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Existencias") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFFF7777))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioNumero = precio.toDoubleOrNull()
                    val stockNumero = stock.toIntOrNull()

                    error = when {
                        nombre.isBlank() -> "Ingresa el nombre."
                        categoria.isBlank() -> "Ingresa la categoría."
                        descripcion.isBlank() -> "Ingresa la descripción."
                        precioNumero == null || precioNumero < 0 ->
                            "Ingresa un precio válido."
                        talla.isBlank() -> "Ingresa la talla."
                        color.isBlank() -> "Ingresa el color."
                        stockNumero == null || stockNumero < 0 ->
                            "Ingresa existencias válidas."
                        else -> ""
                    }

                    if (error.isEmpty()) {
                        guardar(
                            nombre.trim(),
                            categoria.trim(),
                            descripcion.trim(),
                            precioNumero!!,
                            talla.trim(),
                            color.trim(),
                            stockNumero!!,
                            producto?.id
                        )
                    }
                }
            ) {
                Text("GUARDAR", color = Verde)
            }
        },
        dismissButton = {
            TextButton(onClick = cerrar) {
                Text("CANCELAR", color = TextoSecundario)
            }
        }
    )
}

@Composable
private fun PerfilContenido(
    modifier: Modifier = Modifier,
    correo: String,
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
                "PR",
                color = Verde,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Mi perfil",
            color = TextoClaro,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            correo,
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
                "CERRAR SESIÓN",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
