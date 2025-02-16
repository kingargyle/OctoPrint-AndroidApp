package android.app.printerapp.octoprint.model

import com.squareup.moshi.Json

data class LoginResponse(
    @Json(name = "body") val body: String
)
data class LoginRequest(
    @Json(name = "passive") val passive: Boolean,
)