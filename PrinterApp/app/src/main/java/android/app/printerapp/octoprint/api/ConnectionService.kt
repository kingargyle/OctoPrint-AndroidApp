package android.app.printerapp.octoprint.api

import android.app.printerapp.octoprint.model.ConnectionResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ConnectionService {

    @GET("/api/connection")
    fun connection(@Query("apikey") apiKey: String): Call<ConnectionResponse>
}