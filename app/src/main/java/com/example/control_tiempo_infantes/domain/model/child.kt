package com.example.control_tiempo_infantes.domain.model

data class Child(
    val id: String = "",
    val circleId: String = "",
    val name: String = "",
    val age: Int = 0,

    // Código temporal para vincular dispositivo (opcional)
    val linkCode: String? = null,

    // Momento (en millis) en que expira el código temporal
    val linkCodeExpiration: Long? = null,

    // Bandera sencilla para saber si al menos un dispositivo ha sido vinculado
    val deviceLinked: Boolean = false
)
