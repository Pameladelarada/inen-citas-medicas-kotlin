package com.example.inen_citas_medicas_kotlin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/mobile/login/")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/mobile/registro/")
    fun registro(@Body request: RegisterRequest): Call<AuthResponse>

    @GET("api/mobile/especialidades/")
    fun especialidades(): Call<EspecialidadesResponse>

    @POST("api/mobile/citas/solicitar/")
    fun solicitarCita(@Body request: SolicitarCitaRequest): Call<CitasResponse>

    @GET("api/mobile/citas/")
    fun misCitas(): Call<CitasResponse>

    @GET("api/mobile/notificaciones/")
    fun notificaciones(): Call<NotificacionesResponse>

    @POST("api/mobile/notificaciones/{id}/leida/")
    fun marcarNotificacionLeida(@Path("id") id: Int): Call<NotificacionesResponse>
}

