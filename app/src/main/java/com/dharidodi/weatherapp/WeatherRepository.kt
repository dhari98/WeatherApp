package com.dharidodi.weatherapp


class WeatherRepository(private val api: WeatherApi) {

    // 🌍 جلب حالة الطقس حسب الإحداثيات
    suspend fun loadCity(lat: Double, lon: Double) = api.getForecast(lat, lon)

    // 🔍 البحث عن مدينة وإرجاع إحداثياتها
    suspend fun getCoordinatesByCity(city: String): City? {
        val geoResponse = WeatherApi.createGeo().getGeoLocation(city)
        val first = geoResponse.results?.firstOrNull()
        return first?.let { City(it.name, it.latitude, it.longitude) }
    }
}
