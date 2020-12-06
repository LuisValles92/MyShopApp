package com.example.myshop.ClienteActivity

import java.io.Serializable

class Producto(
    var NombreP: String? = null,
    var PrecioP: String? = null,
    var DescripcionP: String? = null,
    var TiendaN: String? = null,
    var TiendaCIF: String? = null,
    var IdP: String? = null
) : Serializable