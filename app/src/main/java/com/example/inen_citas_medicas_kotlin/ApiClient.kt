package com.example.inen_citas_medicas_kotlin

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Cambia esta URL si tu dominio Railway es diferente.
    private const val BASE_URL = "https://inen-citas-medicas-production.up.railway.app/"

    // OkHttp llama a saveFromResponse y loadForRequest desde sus propios hilos,
    // asi que el mapa tiene que soportar accesos concurrentes.
    private val cookies = java.util.concurrent.ConcurrentHashMap<String, List<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookiesFromResponse: List<Cookie>) {
            cookies[url.host] = cookiesFromResponse
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookies[url.host].orEmpty()
        }
    }

    // El registro de red solo en compilaciones de depuracion: en release
    // escribiria en logcat las rutas de cada peticion, y esta app consulta
    // historiales clinicos.
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(logging)
        .build()

    val service: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    /**
     * Borra la cookie de sesion guardada en memoria.
     *
     * El backend no expone un endpoint de cierre de sesion, asi que la sesion
     * sigue viva en el servidor hasta que expire. Esto es lo maximo que puede
     * hacer el cliente: dejar de presentar la credencial.
     */
    fun clearSession() {
        cookies.clear()
    }
}

