package android.app.printerapp.octoprint.api

import android.app.printerapp.octoprint.model.LoginRequest
import android.app.printerapp.octoprint.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthenticationService {

    @GET("/api/login")
    fun loginAPIKey(
        @Header("X-Api-Key") apiKey: String,
        @Body loginRequest: LoginRequest
        ): Call<LoginResponse>
}