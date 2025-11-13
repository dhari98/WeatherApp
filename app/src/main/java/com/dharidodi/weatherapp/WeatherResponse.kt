@file:Suppress("PropertyName")

package com.dharidodi.weatherapp


data class WeatherResponse(
    val current: Current?,
    val daily: Daily?
)


