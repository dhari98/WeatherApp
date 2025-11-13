@file:Suppress("PropertyName")

package com.dharidodi.weatherapp

data class Current(
    val temperature_2m: Double?,
    val relative_humidity_2m: Int?,
    val weather_code: Int?
)
