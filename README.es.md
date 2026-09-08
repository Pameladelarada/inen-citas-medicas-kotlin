[English](README.md) · [Español](README.es.md)

# INEN Citas Médicas — cliente Android

Cliente Android nativo para un sistema de citas médicas del INEN, el Instituto
Nacional de Enfermedades Neoplásicas. Los pacientes solicitan citas y siguen su
estado; los médicos las clasifican, programan y gestionan.

**Es un proyecto universitario de dos personas.** La app se desarrolló junto con
[Adrián Torres](https://github.com/AdrianTorres2021), que además es autor del
[backend en Django](https://github.com/AdrianTorres2021/inen-citas-medicas) que
esta app consume — por eso en este repositorio no hay código de servidor.

## Quién escribió qué

Medido con `git blame` sobre las fuentes Kotlin:

| Archivo | Mío | De Adrián |
|---|---|---|
| `ApiClient.kt` | 43 / 43 | — |
| `ApiService.kt` | 30 / 41 | 11 |
| `ApiModels.kt` | 20 / 60 | 40 |
| `MainActivity.kt` | 190 / 549 | 359 |

Mi parte es la capa de red: el cliente Retrofit, el `CookieJar` propio que
mantiene la sesión de Django entre peticiones, y la mayor parte del contrato de
la API. Adrián hizo el panel del médico, el flujo multi-rol y la navegación
inferior.

## Qué hace

- Los **pacientes** se registran, inician sesión, consultan especialidades,
  solicitan una cita describiendo sus síntomas y leen notificaciones.
- Los **médicos** entran con su número de colegiatura, ven las citas agrupadas
  por estado y las aprueban, reprograman o cierran.
- El backend asigna a cada solicitud un **puntaje de triaje y un nivel de
  urgencia**, que la app muestra junto a la justificación que devuelve.

## Stack

Kotlin · Android SDK 24–36 · AGP 9.2.1 · Retrofit 2.11 · OkHttp · Gson

La interfaz se construye por código dentro de `MainActivity` — sin layouts XML
más allá de un contenedor, sin fragments. Es lo principal que cambiaría: 549
líneas en una sola Activity son difíciles de probar y de ampliar.

## Cómo ejecutarlo

```bash
git clone https://github.com/Pameladelarada/inen-citas-medicas-kotlin.git
```

Se abre en Android Studio y se ejecuta. La app apunta al backend desplegado, así
que no hace falta levantar un servidor local. Para apuntar a otro, se cambia
`BASE_URL` en `ApiClient.kt`.

## Tratamiento de datos personales

La app transmite **datos de salud de personas identificables**: DNI, síntomas,
historial clínico y puntajes de triaje. Bajo la Ley 29733 son *datos personales
sensibles*, con exigencias mayores que los datos personales comunes.

Lo que la app hace hoy:

- **No guarda nada** en el dispositivo: ni `SharedPreferences` ni base de datos
  local. El estado de sesión vive en memoria y muere con el proceso.
- Se comunica con el backend **solo por HTTPS**.
- **No registra tráfico de red fuera de las compilaciones de depuración**, así
  que las rutas de las peticiones nunca llegan a logcat en una versión de
  release.

Lo que haría falta antes de cualquier despliegue real, y **no está**:

- Un aviso de privacidad y consentimiento explícito en el registro, como exige
  la Ley 29733 para datos sensibles.
- Inscripción del banco de datos personales ante la ANPD.
- Un endpoint de cierre de sesión en el servidor — ver la limitación de abajo.
- Reducción y ofuscación de código en release. `isMinifyEnabled` está en `false`
  a propósito: activar R8 con `proguard-rules.pro` vacío ofuscaría los nombres
  de campo de los `data class`, y Gson mapea el JSON por nombre, así que todos
  los campos llegarían nulos solo en release y sin error de compilación. Primero
  hay que escribir las reglas de conservación y probar un APK de release en un
  dispositivo.

Este es un proyecto universitario que funciona con datos de prueba. La lista de
arriba es lo que necesitaría un despliegue en producción, no una afirmación de
que cumple.

## Limitaciones conocidas

- **El cierre de sesión es solo del lado del cliente.** El backend no expone un
  endpoint de logout, así que la sesión sigue válida en el servidor hasta que
  expire. La app borra su cookie y su estado en memoria, que es hasta donde
  puede llegar el cliente.
- Sin pruebas automatizadas. Los dos archivos bajo `app/src/test` y
  `app/src/androidTest` son las plantillas de Android Studio.
- La sesión no se persiste, así que hay que iniciar sesión en cada arranque.

## Licencia

Sin archivo de licencia. Consúltame antes de reutilizarlo.
