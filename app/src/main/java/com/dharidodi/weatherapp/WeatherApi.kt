package com.dharidodi.weatherapp


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



interface WeatherApi {

    // 🔹 بيانات الطقس
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",
        @Query("timezone") tz: String = "auto"
    ): WeatherResponse

    // 🔹 تحويل اسم المدينة إلى إحداثيات
    @GET("v1/search")
    suspend fun getGeoLocation(@Query("name") name: String): GeoResponse

    companion object {

        // 🌦️ API الطقس
        fun create(): WeatherApi {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder().addInterceptor(logger).build()

            return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApi::class.java)
        }

        // 🗺️ API الجغرافيا (للبحث بالاسم)
        fun createGeo(): WeatherApi {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl("https://geocoding-api.open-meteo.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApi::class.java)
        }
    }
}
