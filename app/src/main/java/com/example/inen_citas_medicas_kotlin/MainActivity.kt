package com.example.inen_citas_medicas_kotlin

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var activeParent: LinearLayout? = null
    private var pacienteNombre: String = "Paciente"
    private var medicoNombre: String = "Médico"
    private var medicoEspecialidad: String = ""

    private val navy = Color.parseColor("#2C3E50")
    private val blue = Color.parseColor("#3498DB")
    private val green = Color.parseColor("#2ECC71")
    private val red = Color.parseColor("#E74C3C")
    private val orange = Color.parseColor("#E67E22")
    private val grayText = Color.parseColor("#718096")
    private val teal = Color.parseColor("#1ABC9C")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        container = findViewById(R.id.contentContainer)
        showLogin()
    }

    // ─── LOGIN MULTI-ROL ─────────────────────────────────────────────────────

    private fun showLogin() {
        clearScreen()
        header("INEN", "Citas Médicas", "Selecciona tu tipo de acceso")

        // Tabs
        val tabRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }
        val tabPaciente = tabButton("Paciente", true)
        val tabMedico = tabButton("Médico", false)
        tabRow.addView(tabPaciente, LinearLayout.LayoutParams(0, dp(44), 1f))
        tabRow.addView(tabMedico, LinearLayout.LayoutParams(0, dp(44), 1f))
        addView(tabRow, bottom = 16)

        // Contenedor de formulario
        val formContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        addView(formContainer)

        fun showPacienteForm() {
            formContainer.removeAllViews()
            tabPaciente.backgroundTintList = ColorStateList.valueOf(blue)
            tabPaciente.setTextColor(Color.WHITE)
            tabMedico.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EEF7"))
            tabMedico.setTextColor(navy)

            val prevParent = activeParent
            activeParent = formContainer
            val username = input("Usuario", "paciente1")
            val password = input("Contraseña", "********", true)
            button("Iniciar sesión", blue, Color.WHITE) {
                val user = username.text.toString().trim()
                val pass = password.text.toString()
                if (user.isBlank() || pass.isBlank()) { toast("Ingresa usuario y contraseña."); return@button }
                loading("Validando...")
                ApiClient.service.login(LoginRequest(user, pass)).enqueueResult(
                    onSuccess = { response ->
                        if (response.ok) {
                            pacienteNombre = response.user?.nombre?.takeIf { it.isNotBlank() } ?: user
                            showHomePaciente()
                        } else { showLogin(); toast(response.error ?: "Error al iniciar sesión.") }
                    },
                    onError = { showLogin(); toast(it) }
                )
            }
            button("Crear cuenta de paciente", Color.parseColor("#E8EEF7"), navy) { showRegister() }
            activeParent = prevParent
        }

        fun showMedicoForm() {
            formContainer.removeAllViews()
            tabMedico.backgroundTintList = ColorStateList.valueOf(teal)
            tabMedico.setTextColor(Color.WHITE)
            tabPaciente.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EEF7"))
            tabPaciente.setTextColor(navy)

            val prevParent = activeParent
            activeParent = formContainer
            val username = input("Usuario", "dr_garcia")
            val password = input("Contraseña", "********", true)
            val colegiatura = input("N° Colegiatura", "CMP-XXXXX")
            button("Iniciar sesión como médico", teal, Color.WHITE) {
                val user = username.text.toString().trim()
                val pass = password.text.toString()
                val col = colegiatura.text.toString().trim()
                if (user.isBlank() || pass.isBlank() || col.isBlank()) { toast("Completa todos los campos."); return@button }
                loading("Validando...")
                ApiClient.service.medicoLogin(MedicoLoginRequest(user, pass, col)).enqueueResult(
                    onSuccess = { response ->
                        if (response.ok) {
                            medicoNombre = response.user?.nombre?.takeIf { it.isNotBlank() } ?: user
                            medicoEspecialidad = response.user?.especialidad ?: ""
                            showHomeMedico()
                        } else { showLogin(); toast(response.error ?: "Error al iniciar sesión.") }
                    },
                    onError = { showLogin(); toast(it) }
                )
            }
            activeParent = prevParent
        }

        tabPaciente.setOnClickListener { showPacienteForm() }
        tabMedico.setOnClickListener { showMedicoForm() }
        showPacienteForm()
    }

    // ─── PACIENTE ─────────────────────────────────────────────────────────────

    private fun showRegister() {
        clearScreen()
        title("Registro de paciente")
        subtitle("Crea tu cuenta para solicitar y consultar citas.")
        val username = input("Usuario", "ej: jperez")
        val firstName = input("Nombres", "Juan")
        val lastName = input("Apellidos", "Perez")
        val email = input("Correo", "correo@ejemplo.com")
        val dni = input("DNI", "12345678")
        val fechaNac = input("Fecha de nacimiento", "YYYY-MM-DD")
        val telefono = input("Telefono", "987654321")
        val password = input("Contraseña", "********", true)
        val password2 = input("Confirmar contraseña", "********", true)
        primaryButton("Registrarme") {
            if (listOf(username, firstName, lastName, email, dni, fechaNac, password, password2).any { it.text.isBlank() }) { toast("Completa los campos obligatorios."); return@primaryButton }
            if (password.text.toString() != password2.text.toString()) { toast("Las contraseñas no coinciden."); return@primaryButton }
            loading("Creando cuenta...")
            ApiClient.service.registro(RegisterRequest(
                username.text.toString().trim(), firstName.text.toString().trim(),
                lastName.text.toString().trim(), email.text.toString().trim(),
                password.text.toString(), password2.text.toString(),
                dni.text.toString().trim(), fechaNac.text.toString().trim(),
                telefono.text.toString().trim()
            )).enqueueResult(
                onSuccess = { response ->
                    if (response.ok) { pacienteNombre = response.user?.nombre?.takeIf { it.isNotBlank() } ?: username.text.toString(); showHomePaciente() }
                    else { showRegister(); toast(response.error ?: "No se pudo registrar.") }
                },
                onError = { showRegister(); toast(it) }
            )
        }
        secondaryButton("Ya tengo cuenta") { showLogin() }
    }

    private fun showHomePaciente() {
        clearScreen()
        header("INEN", "Hola, $pacienteNombre", "Gestiona tus citas médicas")
        card { label("Que puedes hacer", bold = true, size = 18); small("Solicita una cita con triaje IA, revisa el estado de tus atenciones y consulta tus notificaciones.") }
        primaryButton("Solicitar cita") { showSolicitarCita() }
        primaryButton("Ver mis citas") { showMisCitas() }
        primaryButton("Notificaciones") { showNotificaciones() }
        secondaryButton("Cerrar sesión") { showLogin() }
    }

    private fun showSolicitarCita() {
        clearScreen()
        title("Solicitar cita")
        subtitle("Selecciona una especialidad y describe tus síntomas.")
        val spinner = Spinner(this)
        val sintomas = input("Síntomas", "Describe tus síntomas con detalle...")
        sintomas.minLines = 4
        sintomas.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        var especialidades = emptyList<Especialidad>()
        addView(spinner)
        loadingInline("Cargando especialidades...")
        ApiClient.service.especialidades().enqueueResult(
            onSuccess = { response ->
                if (response.ok && !response.especialidades.isNullOrEmpty()) {
                    especialidades = response.especialidades
                    spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, especialidades.map { it.nombre })
                } else toast("No hay especialidades disponibles.")
            },
            onError = { toast(it) }
        )
        primaryButton("Enviar solicitud") {
            if (especialidades.isEmpty()) { toast("Espera a que carguen las especialidades."); return@primaryButton }
            if (sintomas.text.isBlank()) { toast("Describe tus síntomas."); return@primaryButton }
            loading("Enviando solicitud...")
            ApiClient.service.solicitarCita(SolicitarCitaRequest(especialidades[spinner.selectedItemPosition].id, sintomas.text.toString().trim())).enqueueResult(
                onSuccess = { response -> if (response.ok) { toast(response.message ?: "Cita solicitada."); showMisCitas() } else { showSolicitarCita(); toast(response.error ?: "No se pudo solicitar.") } },
                onError = { showSolicitarCita(); toast(it) }
            )
        }
        secondaryButton("Volver") { showHomePaciente() }
    }

    private fun showMisCitas() {
        clearScreen(); title("Mis citas"); subtitle("Consulta el estado de tus solicitudes."); loadingInline("Cargando citas...")
        ApiClient.service.misCitas().enqueueResult(
            onSuccess = { response -> clearScreen(); title("Mis citas")
                if (response.ok && !response.citas.isNullOrEmpty()) response.citas.forEach { citaCard(it) }
                else card { small("Aún no tienes citas registradas.") }
                secondaryButton("Volver") { showHomePaciente() }
            },
            onError = { clearScreen(); title("Mis citas"); card { small(it) }; secondaryButton("Volver") { showHomePaciente() } }
        )
    }

    private fun showNotificaciones() {
        clearScreen(); title("Notificaciones"); loadingInline("Cargando...")
        ApiClient.service.notificaciones().enqueueResult(
            onSuccess = { response -> clearScreen(); title("Notificaciones")
                if (response.ok && !response.notificaciones.isNullOrEmpty()) response.notificaciones.forEach { notificationCard(it) }
                else card { small("No tienes notificaciones.") }
                secondaryButton("Volver") { showHomePaciente() }
            },
            onError = { clearScreen(); title("Notificaciones"); card { small(it) }; secondaryButton("Volver") { showHomePaciente() } }
        )
    }

    private fun citaCard(cita: Cita) {
        card {
            label("#${cita.id} - ${cita.especialidad ?: "Sin especialidad"}", bold = true, size = 17)
            badge(cita.estado_display ?: cita.estado ?: "Pendiente", colorForStatus(cita.estado))
            small("Urgencia: ${cita.nivel_urgencia_display ?: "-"} | Puntaje: ${cita.puntaje_triage ?: 0}")
            small("Fecha cita: ${cita.fecha_cita ?: "Por confirmar"}")
            small("Medico: ${cita.medico ?: "Sin asignar"}")
            small("Síntomas: ${cita.sintomas ?: "-"}")
            small("Justificacion IA: ${cita.justificacion_ia ?: "Sin justificacion"}")
        }
    }

    private fun notificationCard(notification: Notificacion) {
        card {
            label(notification.tipo ?: "Notificacion", bold = true, size = 17)
            small(notification.mensaje ?: "")
            small("Cita #${notification.cita_id ?: "-"} | ${notification.fecha ?: ""}")
            if (notification.enviada != true) {
                secondaryButton("Marcar como leida") {
                    ApiClient.service.marcarNotificacionLeida(notification.id).enqueueResult(
                        onSuccess = { toast("Notificacion marcada como leida."); showNotificaciones() },
                        onError = { toast(it) }
                    )
                }
            }
        }
    }

    // ─── MÉDICO ───────────────────────────────────────────────────────────────

    private fun showHomeMedico() {
        clearScreen()
        header("INEN", "Dr. $medicoNombre", medicoEspecialidad)
        loadingInline("Cargando citas...")
        ApiClient.service.medicoCitas().enqueueResult(
            onSuccess = { response ->
                clearScreen()
                header("INEN", "Dr. $medicoNombre", medicoEspecialidad)
                if (response.ok && response.stats != null) {
                    card {
                        label("Panel de citas", bold = true, size = 18)
                        small("Activas: ${response.stats.activas}  |  Pendientes: ${response.stats.pendientes}")
                        small("Reprogramadas: ${response.stats.reprogramadas}  |  Completadas: ${response.stats.completadas}")
                    }
                }
                if (!response.citas.isNullOrEmpty()) {
                    label("Citas activas", bold = true, size = 16, color = navy)
                    response.citas.forEach { citaMedicoCard(it) }
                } else {
                    card { small("No tienes citas activas asignadas.") }
                }
                if (!response.historial.isNullOrEmpty()) {
                    label("Historial", bold = true, size = 16, color = grayText)
                    response.historial.forEach { citaMedicoCard(it, esHistorial = true) }
                }
                secondaryButton("Cerrar sesión") { showLogin() }
            },
            onError = { clearScreen(); header("INEN", "Dr. $medicoNombre", medicoEspecialidad); card { small(it) }; secondaryButton("Cerrar sesión") { showLogin() } }
        )
    }

    private fun citaMedicoCard(cita: Cita, esHistorial: Boolean = false) {
        card {
            label("#${cita.id} - ${cita.paciente_nombre ?: "Paciente"}", bold = true, size = 17)
            small("DNI: ${cita.paciente_dni ?: "-"}  |  Edad: ${cita.paciente_edad ?: "-"} años")
            badge(cita.estado_display ?: cita.estado ?: "Pendiente", colorForStatus(cita.estado))
            badge(cita.nivel_urgencia_display ?: "-", colorForUrgencia(cita.nivel_urgencia))
            small("Especialidad: ${cita.especialidad ?: "-"}")
            small("Fecha cita: ${cita.fecha_cita ?: "Por confirmar"}")
            small("Síntomas: ${cita.sintomas ?: "-"}")
            small("Justificación IA: ${cita.justificacion_ia ?: "-"}")
            if (!cita.notas_medico.isNullOrBlank()) small("Notas: ${cita.notas_medico}")
            if (!cita.paciente_historial.isNullOrBlank()) {
                secondaryButton("Ver historial del paciente") {
                    showHistorialPaciente(cita.paciente_nombre ?: "Paciente", cita.paciente_historial)
                }
            }
            if (!esHistorial) {
                primaryButton("Gestionar cita") { showGestionarCita(cita) }
            }
        }
    }

    private fun showHistorialPaciente(nombre: String, historial: String) {
        clearScreen()
        title("Historial de $nombre")
        card { small(historial.ifBlank { "Sin historial registrado." }) }
        secondaryButton("Volver") { showHomeMedico() }
    }

    private fun showGestionarCita(cita: Cita) {
        clearScreen()
        title("Gestionar Cita #${cita.id}")
        card {
            label(cita.paciente_nombre ?: "Paciente", bold = true, size = 17)
            small("DNI: ${cita.paciente_dni ?: "-"}  |  Edad: ${cita.paciente_edad ?: "-"} años")
            badge(cita.nivel_urgencia_display ?: "-", colorForUrgencia(cita.nivel_urgencia))
            small("Síntomas: ${cita.sintomas ?: "-"}")
            small("Justificación IA: ${cita.justificacion_ia ?: "-"}")
        }
        val notas = input("Notas médicas (opcional)", "Observaciones...")
        notas.minLines = 3
        notas.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

        // Botón Completar
        button("✔ Completar cita", green, Color.WHITE) {
            loading("Actualizando...")
            ApiClient.service.gestionarCita(cita.id, GestionarCitaRequest("COMPLETADA", notas.text.toString().trim())).enqueueResult(
                onSuccess = { response -> toast(response.message ?: "Cita completada."); showHomeMedico() },
                onError = { showHomeMedico(); toast(it) }
            )
        }

        // Botón Cancelar
        button("✘ Cancelar cita", red, Color.WHITE) {
            loading("Actualizando...")
            ApiClient.service.gestionarCita(cita.id, GestionarCitaRequest("CANCELADA", notas.text.toString().trim())).enqueueResult(
                onSuccess = { response -> toast(response.message ?: "Cita cancelada."); showHomeMedico() },
                onError = { showHomeMedico(); toast(it) }
            )
        }

        // Sección Reprogramar
        label("Reprogramar cita", bold = true, size = 15, color = orange)
        val fechaNueva = input("Nueva fecha y hora", "YYYY-MM-DDTHH:MM")
        val motivo = input("Motivo de reprogramación", "Explica el motivo...")
        button("↺ Reprogramar", orange, Color.WHITE) {
            if (fechaNueva.text.isBlank() || motivo.text.isBlank()) { toast("Ingresa fecha y motivo."); return@button }
            loading("Actualizando...")
            ApiClient.service.gestionarCita(cita.id, GestionarCitaRequest("REPROGRAMADA", notas.text.toString().trim(), fechaNueva.text.toString().trim(), motivo.text.toString().trim())).enqueueResult(
                onSuccess = { response -> toast(response.message ?: "Cita reprogramada."); showHomeMedico() },
                onError = { showHomeMedico(); toast(it) }
            )
        }

        secondaryButton("Volver") { showHomeMedico() }
    }

    // ─── UI HELPERS ───────────────────────────────────────────────────────────

    private fun clearScreen() { container.removeAllViews() }

    private fun header(code: String, title: String, subtitle: String) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(22), dp(20), dp(22)); background = rounded(navy, dp(12)) }
        box.addView(TextView(this).apply { text = code; textSize = 26f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD) })
        box.addView(TextView(this).apply { text = title; textSize = 24f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD) })
        box.addView(TextView(this).apply { text = subtitle; textSize = 14f; setTextColor(Color.parseColor("#DDE7F3")) })
        addView(box, bottom = 18)
    }

    private fun tabButton(text: String, active: Boolean): Button {
        return Button(this).apply {
            this.text = text; textSize = 14f; setTypeface(typeface, Typeface.BOLD)
            backgroundTintList = ColorStateList.valueOf(if (active) blue else Color.parseColor("#E8EEF7"))
            setTextColor(if (active) Color.WHITE else navy)
        }
    }

    private fun title(text: String) { label(text, bold = true, size = 24, color = navy) }
    private fun subtitle(text: String) { label(text, size = 14, color = grayText) }

    private fun label(text: String, bold: Boolean = false, size: Int = 14, color: Int = Color.parseColor("#2D3748")) {
        addView(TextView(this).apply { this.text = text; textSize = size.toFloat(); setTextColor(color); if (bold) setTypeface(typeface, Typeface.BOLD) }, bottom = 8)
    }

    private fun small(text: String) { label(text, size = 13, color = grayText) }

    private fun input(label: String, hint: String, password: Boolean = false): EditText {
        label(label, bold = true, size = 13, color = navy)
        val edit = EditText(this).apply {
            this.hint = hint; textSize = 15f
            setSingleLine(!hint.contains("Describe") && !hint.contains("Observ") && !hint.contains("Explica"))
            inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE, dp(8), Color.parseColor("#DDE3EC"))
        }
        addView(edit, bottom = 14)
        return edit
    }

    private fun primaryButton(text: String, action: () -> Unit) { button(text, blue, Color.WHITE, action) }
    private fun secondaryButton(text: String, action: () -> Unit) { button(text, Color.parseColor("#E8EEF7"), navy, action) }

    private fun button(text: String, bg: Int, fg: Int, action: () -> Unit) {
        addView(Button(this).apply { this.text = text; setTextColor(fg); backgroundTintList = ColorStateList.valueOf(bg); setTypeface(typeface, Typeface.BOLD); setOnClickListener { action() } }, bottom = 10)
    }

    private fun card(content: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(Color.WHITE, dp(12), Color.parseColor("#DDE3EC")) }
        val prev = activeParent; activeParent = box; box.content(); activeParent = prev
        addView(box, bottom = 14)
    }

    private fun badge(text: String, color: Int) {
        addView(TextView(this).apply { this.text = text; textSize = 12f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD); setPadding(dp(10), dp(4), dp(10), dp(4)); background = rounded(color, dp(20)) }, bottom = 8)
    }

    private fun loading(message: String) { clearScreen(); title(message); addView(ProgressBar(this), bottom = 12) }
    private fun loadingInline(message: String) { small(message) }

    private fun addView(view: View, bottom: Int = 0) {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(bottom) }
        (activeParent ?: container).addView(view, params)
    }

    private fun rounded(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable {
        return GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat(); strokeColor?.let { setStroke(dp(1), it) } }
    }

    private fun colorForStatus(status: String?): Int = when (status) { "COMPLETADA" -> green; "CANCELADA" -> red; "REPROGRAMADA" -> orange; "CONFIRMADA" -> blue; else -> orange }
    private fun colorForUrgencia(urgencia: String?): Int = when (urgencia) { "CRITICO" -> red; "ALTO" -> orange; "MODERADO" -> blue; else -> green }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

    private fun <T> Call<T>.enqueueResult(onSuccess: (T) -> Unit, onError: (String) -> Unit) {
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError("Error ${response.code()}: no se pudo procesar la respuesta.")
            }
            override fun onFailure(call: Call<T>, t: Throwable) { onError("No se pudo conectar: ${t.message}") }
        })
    }
}