@file:Suppress("PropertyName")

package com.dharidodi.weatherapp

data class Daily(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val weather_code: List<Int>?
)
