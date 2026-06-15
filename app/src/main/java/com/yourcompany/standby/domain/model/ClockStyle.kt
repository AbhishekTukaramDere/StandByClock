package com.yourcompany.standby.domain.model

data class ClockStyle(
    val id: String,
    val name: String,
    val type: String, // "PORTRAIT" or "LANDSCAPE"
    val description: String
)
