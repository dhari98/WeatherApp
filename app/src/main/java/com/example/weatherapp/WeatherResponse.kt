@file:Suppress("PropertyName")

package com.example.weatherapp


data class WeatherResponse(
    val current: Current?,
    val daily: Daily?
)


