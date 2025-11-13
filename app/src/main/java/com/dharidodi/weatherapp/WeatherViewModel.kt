package com.dharidodi.weatherapp


import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.util.Calendar




class WeatherViewModel : ViewModel() {

    private val repo = WeatherRepository(WeatherApi.create())

    private val _state = MutableLiveData<Result<WeatherResponse>>()
    val state: LiveData<Result<WeatherResponse>> = _state

    fun fetch(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val data = repo.loadCity(lat, lon)
                _state.postValue(Result.success(data))
            } catch (e: Exception) {
                _state.postValue(Result.failure(e))
            }
        }
    }

    fun fetchByCityName(city: String) {
        viewModelScope.launch {
            try {
                val geo = repo.getCoordinatesByCity(city)
                if (geo != null) fetch(geo.lat, geo.lon)
                else _state.postValue(Result.failure(Exception("City not found")))
            } catch (e: Exception) {
                _state.postValue(Result.failure(e))
            }
        }
    }

    fun isNightNow(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour !in 6..<18
    }
}
