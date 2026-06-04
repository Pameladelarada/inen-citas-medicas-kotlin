package com.example.inen_citas_medicas_kotlin

data class LoginRequest(val username: String, val password: String)
data class MedicoLoginRequest(val username: String, val password: String, val colegiatura: String)
data class RegisterRequest(
    val username: String, val first_name: String, val last_name: String,
    val email: String, val password1: String, val password2: String,
    val dni: String, val fecha_nac: String, val telefono: String
)
data class SolicitarCitaRequest(val especialidad: Int, val sintomas: String)
data class GestionarCitaRequest(
    val estado: String,
    val notas_medico: String = "",
    val fecha_cita: String = "",
    val motivo_reprogramacion: String = ""
)

data class MobileUser(
    val id: Int?, val username: String?, val nombre: String?,
    val email: String?, val dni: String?, val edad: Int?
)
data class MedicoUser(
    val id: Int?, val username: String?, val nombre: String?,
    val especialidad: String?, val colegiatura: String?
)
data class AuthResponse(val ok: Boolean, val message: String?, val error: String?, val user: MobileUser?)
data class MedicoAuthResponse(val ok: Boolean, val message: String?, val error: String?, val user: MedicoUser?)

data class Especialidad(val id: Int, val nombre: String, val descripcion: String?)
data class EspecialidadesResponse(val ok: Boolean, val especialidades: List<Especialidad>?)

data class Cita(
    val id: Int, val estado: String?, val estado_display: String?,
    val nivel_urgencia: String?, val nivel_urgencia_display: String?,
    val puntaje_triage: Int?, val especialidad: String?, val medico: String?,
    val fecha_cita: String?, val fecha_solicitada: String?,
    val sintomas: String?, val justificacion_ia: String?,
    val notas_medico: String?, val motivo_reprogramacion: String?,
    val paciente_nombre: String?, val paciente_dni: String?,
    val paciente_edad: Int?, val paciente_historial: String?
)
data class MedicoStats(val activas: Int, val pendientes: Int, val reprogramadas: Int, val completadas: Int)
data class CitasResponse(
    val ok: Boolean, val message: String?, val error: String?,
    val cita: Cita?, val citas: List<Cita>?
)
data class MedicoCitasResponse(
    val ok: Boolean, val error: String?,
    val stats: MedicoStats?, val citas: List<Cita>?, val historial: List<Cita>?
)

data class Notificacion(
    val id: Int, val cita_id: Int?, val tipo: String?,
    val mensaje: String?, val enviada: Boolean?, val fecha: String?
)
data class NotificacionesResponse(
    val ok: Boolean, val error: String?, val message: String?,
    val notificaciones: List<Notificacion>?
)
data class SimpleResponse(val ok: Boolean, val message: String?, val error: String?)