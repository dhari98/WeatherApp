🌤️ WeatherApp — Android Kotlin (MVVM)

A simple and lightweight Android Weather App built with Kotlin and MVVM architecture.
The app fetches real-time temperature and weather information using Retrofit and a public Weather API, and displays the current conditions in a clean, minimal UI.

📱 Features

Current temperature display

Real-time weather data from a public API

Clean & minimal interface

Geocoding support to detect the city

MVVM architecture with Repository pattern

Automatically updates data when the app opens

🛠️ Tech Stack

Language: Kotlin
Architecture: MVVM
Networking: Retrofit + Gson
UI: XML + ViewModel + LiveData
Data Handling: Repository Pattern
APIs: Weather API + GeoCoding API

📂 Project Structure
com.example.weatherapp
│
├── City.kt
├── Current.kt
├── Daily.kt
├── GeoResponse.kt
├── GeoResult.kt
├── MainActivity.kt
├── WeatherApi.kt
├── WeatherRepository.kt
├── WeatherResponse.kt
└── WeatherViewModel.kt
