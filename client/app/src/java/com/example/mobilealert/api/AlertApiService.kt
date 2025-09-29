package com.example.mobilealert.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface AlertApiService {

    @GET("/api/status")
    suspend fun getStatus(): Response<ServerStatus>

    @GET("/api/info")
    suspend fun getServerInfo(): Response<ServerInfo>

    @POST("/api/activate")
    suspend fun activateAlert(): Response<ApiResponse>

    @POST("/api/deactivate")
    suspend fun deactivateAlert(): Response<ApiResponse>
}

data class ServerStatus(
    val hasAction: Boolean,
    val message: String,
    val timestamp: String,
    val serverTime: Long
)

data class ServerInfo(
    val name: String,
    val version: String,
    val status: String,
    val uptime: Double,
    val state: ServerState
)

data class ServerState(
    val actionEnabled: Boolean,
    val message: String,
    val lastActivated: String?,
    val clients: List<String>
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)