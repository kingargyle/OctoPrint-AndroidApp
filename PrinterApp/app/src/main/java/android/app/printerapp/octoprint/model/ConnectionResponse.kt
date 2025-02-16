package android.app.printerapp.octoprint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConnectionResponse(
    @Json(name = "current") val current: Current?,
    @Json(name = "options") val options: Options?
)

@JsonClass(generateAdapter = true)
data class Current(
    @Json(name = "baudrate") val baudrate: Int?,
    @Json(name = "port") val port: String?,
    @Json(name = "printerProfile") val printerProfile: String?,
    @Json(name = "state") val state: String?
)

@JsonClass(generateAdapter = true)
data class Options(
    @Json(name = "baudratePreference") val baudratePreference: String?,
    @Json(name = "baudrates") val baudrates: List<Int>?,
    @Json(name = "portPreference") val portPreference: String?,
    @Json(name = "ports") val ports: List<String>?,
    @Json(name = "printerProfilePreference") val printerProfilePreference: String?,
    @Json(name = "printerProfiles") val printerProfiles: List<PrinterProfile>?
)

@JsonClass(generateAdapter = true)
data class PrinterProfile(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?
)