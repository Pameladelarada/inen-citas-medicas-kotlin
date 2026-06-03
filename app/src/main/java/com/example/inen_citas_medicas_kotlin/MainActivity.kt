package com.example.inen_citas_medicas_kotlin

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

    private val navy = Color.parseColor("#2C3E50")
    private val blue = Color.parseColor("#3498DB")
    private val green = Color.parseColor("#2ECC71")
    private val red = Color.parseColor("#E74C3C")
    private val orange = Color.parseColor("#E67E22")
    private val grayText = Color.parseColor("#718096")

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

    private fun showLogin() {
        clearScreen()
        header("INEN", "Citas medicas", "App movil del paciente")

        val username = input("Usuario", "paciente1")
        val password = input("Contrasena", "********", true)

        primaryButton("Iniciar sesion") {
            val user = username.text.toString().trim()
            val pass = password.text.toString()
            if (user.isBlank() || pass.isBlank()) {
                toast("Ingresa usuario y contrasena.")
                return@primaryButton
            }

            loading("Validando usuario...")
            ApiClient.service.login(LoginRequest(user, pass)).enqueueResult(
                onSuccess = { response ->
                    if (response.ok) {
                        pacienteNombre = response.user?.nombre?.takeIf { it.isNotBlank() } ?: user
                        showHome()
                    } else {
                        showLogin()
                        toast(response.error ?: "No se pudo iniciar sesion.")
                    }
                },
                onError = {
                    showLogin()
                    toast(it)
                }
            )
        }

        secondaryButton("Crear cuenta de paciente") { showRegister() }
    }

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
        val password = input("Contrasena", "********", true)
        val password2 = input("Confirmar contrasena", "********", true)

        primaryButton("Registrarme") {
            if (listOf(username, firstName, lastName, email, dni, fechaNac, password, password2).any { it.text.isBlank() }) {
                toast("Completa los campos obligatorios.")
                return@primaryButton
            }
            if (password.text.toString() != password2.text.toString()) {
                toast("Las contrasenas no coinciden.")
                return@primaryButton
            }

            loading("Creando cuenta...")
            val request = RegisterRequest(
                username = username.text.toString().trim(),
                first_name = firstName.text.toString().trim(),
                last_name = lastName.text.toString().trim(),
                email = email.text.toString().trim(),
                password1 = password.text.toString(),
                password2 = password2.text.toString(),
                dni = dni.text.toString().trim(),
                fecha_nac = fechaNac.text.toString().trim(),
                telefono = telefono.text.toString().trim()
            )
            ApiClient.service.registro(request).enqueueResult(
                onSuccess = { response ->
                    if (response.ok) {
                        pacienteNombre = response.user?.nombre?.takeIf { it.isNotBlank() } ?: request.first_name
                        showHome()
                    } else {
                        showRegister()
                        toast(response.error ?: "No se pudo registrar.")
                    }
                },
                onError = {
                    showRegister()
                    toast(it)
                }
            )
        }

        secondaryButton("Ya tengo cuenta") { showLogin() }
    }

    private fun showHome() {
        clearScreen()
        header("INEN", "Hola, $pacienteNombre", "Gestiona tus citas medicas")

        card {
            label("Que puedes hacer", bold = true, size = 18)
            small("Solicita una cita con triaje IA, revisa el estado de tus atenciones y consulta tus notificaciones.")
        }

        primaryButton("Solicitar cita") { showSolicitarCita() }
        primaryButton("Ver mis citas") { showMisCitas() }
        primaryButton("Notificaciones") { showNotificaciones() }
        secondaryButton("Cerrar sesion") { showLogin() }
    }

    private fun showSolicitarCita() {
        clearScreen()
        title("Solicitar cita")
        subtitle("Selecciona una especialidad y describe tus sintomas.")

        val spinner = Spinner(this)
        val sintomas = input("Sintomas", "Describe tus sintomas con detalle...")
        sintomas.minLines = 4
        sintomas.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

        var especialidades = emptyList<Especialidad>()
        addView(spinner)
        loadingInline("Cargando especialidades...")

        ApiClient.service.especialidades().enqueueResult(
            onSuccess = { response ->
                if (response.ok && !response.especialidades.isNullOrEmpty()) {
                    especialidades = response.especialidades
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        especialidades.map { it.nombre }
                    )
                    spinner.adapter = adapter
                } else {
                    toast("No hay especialidades disponibles.")
                }
            },
            onError = { toast(it) }
        )

        primaryButton("Enviar solicitud") {
            if (especialidades.isEmpty()) {
                toast("Espera a que carguen las especialidades.")
                return@primaryButton
            }
            if (sintomas.text.isBlank()) {
                toast("Describe tus sintomas.")
                return@primaryButton
            }

            val selected = especialidades[spinner.selectedItemPosition]
            loading("Enviando solicitud...")
            ApiClient.service.solicitarCita(
                SolicitarCitaRequest(selected.id, sintomas.text.toString().trim())
            ).enqueueResult(
                onSuccess = { response ->
                    if (response.ok) {
                        toast(response.message ?: "Cita solicitada.")
                        showMisCitas()
                    } else {
                        showSolicitarCita()
                        toast(response.error ?: "No se pudo solicitar la cita.")
                    }
                },
                onError = {
                    showSolicitarCita()
                    toast(it)
                }
            )
        }

        secondaryButton("Volver") { showHome() }
    }

    private fun showMisCitas() {
        clearScreen()
        title("Mis citas")
        subtitle("Consulta el estado de tus solicitudes y atenciones.")
        loadingInline("Cargando citas...")

        ApiClient.service.misCitas().enqueueResult(
            onSuccess = { response ->
                clearScreen()
                title("Mis citas")
                if (response.ok && !response.citas.isNullOrEmpty()) {
                    response.citas.forEach { citaCard(it) }
                } else {
                    card { small("Aun no tienes citas registradas.") }
                }
                secondaryButton("Volver") { showHome() }
            },
            onError = {
                clearScreen()
                title("Mis citas")
                card { small(it) }
                secondaryButton("Volver") { showHome() }
            }
        )
    }

    private fun showNotificaciones() {
        clearScreen()
        title("Notificaciones")
        subtitle("Mensajes sobre tus citas y prioridad de atencion.")
        loadingInline("Cargando notificaciones...")

        ApiClient.service.notificaciones().enqueueResult(
            onSuccess = { response ->
                clearScreen()
                title("Notificaciones")
                if (response.ok && !response.notificaciones.isNullOrEmpty()) {
                    response.notificaciones.forEach { notificationCard(it) }
                } else {
                    card { small("No tienes notificaciones por ahora.") }
                }
                secondaryButton("Volver") { showHome() }
            },
            onError = {
                clearScreen()
                title("Notificaciones")
                card { small(it) }
                secondaryButton("Volver") { showHome() }
            }
        )
    }

    private fun citaCard(cita: Cita) {
        card {
            label("#${cita.id} - ${cita.especialidad ?: "Sin especialidad"}", bold = true, size = 17)
            badge(cita.estado_display ?: cita.estado ?: "Pendiente", colorForStatus(cita.estado))
            small("Urgencia: ${cita.nivel_urgencia_display ?: cita.nivel_urgencia ?: "-"} | Puntaje: ${cita.puntaje_triage ?: 0}")
            small("Fecha cita: ${cita.fecha_cita ?: "Por confirmar"}")
            small("Medico: ${cita.medico ?: "Sin asignar"}")
            small("Sintomas: ${cita.sintomas ?: "-"}")
            small("Justificacion IA: ${cita.justificacion_ia ?: "Sin justificacion registrada"}")
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
                        onSuccess = { toast("Notificacion marcada como leida.") },
                        onError = { toast(it) }
                    )
                }
            }
        }
    }

    private fun clearScreen() {
        container.removeAllViews()
    }

    private fun header(code: String, title: String, subtitle: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = rounded(navy, dp(12))
        }
        val logo = TextView(this).apply {
            text = code
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        val main = TextView(this).apply {
            text = title
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        val sub = TextView(this).apply {
            text = subtitle
            textSize = 14f
            setTextColor(Color.parseColor("#DDE7F3"))
        }
        box.addView(logo)
        box.addView(main)
        box.addView(sub)
        addView(box, bottom = 18)
    }

    private fun title(text: String) {
        label(text, bold = true, size = 24, color = navy)
    }

    private fun subtitle(text: String) {
        label(text, size = 14, color = grayText)
    }

    private fun label(text: String, bold: Boolean = false, size: Int = 14, color: Int = Color.parseColor("#2D3748")) {
        addView(TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }, bottom = 8)
    }

    private fun small(text: String) {
        label(text, size = 13, color = grayText)
    }

    private fun input(label: String, hint: String, password: Boolean = false): EditText {
        label(label, bold = true, size = 13, color = navy)
        val edit = EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setSingleLine(!hint.contains("Describe"))
            inputType = if (password) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT
            }
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE, dp(8), Color.parseColor("#DDE3EC"))
        }
        addView(edit, bottom = 14)
        return edit
    }

    private fun primaryButton(text: String, action: () -> Unit) {
        button(text, blue, Color.WHITE, action)
    }

    private fun secondaryButton(text: String, action: () -> Unit) {
        button(text, Color.parseColor("#E8EEF7"), navy, action)
    }

    private fun button(text: String, bg: Int, fg: Int, action: () -> Unit) {
        val button = Button(this).apply {
            this.text = text
            setTextColor(fg)
            backgroundTintList = ColorStateList.valueOf(bg)
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener { action() }
        }
        addView(button, bottom = 10)
    }

    private fun card(content: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(Color.WHITE, dp(12), Color.parseColor("#DDE3EC"))
        }
        val previousParent = activeParent
        activeParent = box
        box.content()
        activeParent = previousParent
        addView(box, bottom = 14)
    }

    private fun badge(text: String, color: Int) {
        val view = TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(10), dp(4), dp(10), dp(4))
            background = rounded(color, dp(20))
        }
        addView(view, bottom = 8)
    }

    private fun loading(message: String) {
        clearScreen()
        title(message)
        addView(ProgressBar(this), bottom = 12)
    }

    private fun loadingInline(message: String) {
        small(message)
    }

    private fun addView(view: View, bottom: Int = 0) {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(bottom)
        }
        (activeParent ?: container).addView(view, params)
    }

    private fun rounded(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            strokeColor?.let { setStroke(dp(1), it) }
        }
    }

    private fun colorForStatus(status: String?): Int {
        return when (status) {
            "COMPLETADA" -> green
            "CANCELADA" -> red
            "REPROGRAMADA" -> orange
            "CONFIRMADA" -> blue
            else -> orange
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun <T> Call<T>.enqueueResult(onSuccess: (T) -> Unit, onError: (String) -> Unit) {
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    onSuccess(body)
                } else {
                    onError("Error ${response.code()}: no se pudo procesar la respuesta.")
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                onError("No se pudo conectar con el servidor: ${t.message}")
            }
        })
    }
}
