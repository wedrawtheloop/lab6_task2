package com.example.lab6_task2.network

import com.example.lab6_task2.models.Client
import com.example.lab6_task2.models.LoyaltyProgram
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class ClientProvider {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val baseUrl = "http://10.0.2.2:8080"

    // Клиенты
    suspend fun getAllClients(): Result<List<Client>> {
        return try {
            val clients = client.get("$baseUrl/clients").body<List<Client>>()
            Result.Success(clients)
        } catch (e: Exception) {
            Result.Error("Failed to load clients: ${e.message}", e)
        }
    }

    suspend fun createClient(client: Client): Result<Client> {
        return try {
            val createdClient = this.client.post("$baseUrl/clients") {
                contentType(ContentType.Application.Json)
                setBody(client)
            }.body<Client>()
            Result.Success(createdClient)
        } catch (e: Exception) {
            Result.Error("Failed to create client: ${e.message}", e)
        }
    }

    suspend fun patchClient(clientId: Long, updates: Map<String, Any>): Result<Boolean> {
        return try {
            val response = client.patch("$baseUrl/clients/$clientId") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            val isSuccess = response.status.value in 200..299
            Result.Success(isSuccess)
        } catch (e: Exception) {
            Result.Error("Failed to update client: ${e.message}", e)
        }
    }

    suspend fun deleteClient(id: Long): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/clients/$id")
            val isSuccess = response.status.value in 200..299
            Result.Success(isSuccess)
        } catch (e: Exception) {
            Result.Error("Failed to delete client: ${e.message}", e)
        }
    }

    // Программы лояльности
    suspend fun getAllLoyaltyPrograms(): Result<List<LoyaltyProgram>> {
        return try {
            val programs = client.get("$baseUrl/lp").body<List<LoyaltyProgram>>()
            Result.Success(programs)
        } catch (e: Exception) {
            Result.Error("Failed to load loyalty programs: ${e.message}", e)
        }
    }

    suspend fun createLoyaltyProgram(program: LoyaltyProgram): Result<LoyaltyProgram> {
        return try {
            val createdProgram = client.post("$baseUrl/lp") {
                contentType(ContentType.Application.Json)
                setBody(program)
            }.body<LoyaltyProgram>()
            Result.Success(createdProgram)
        } catch (e: Exception) {
            Result.Error("Failed to create loyalty program: ${e.message}", e)
        }
    }

    suspend fun patchLoyaltyProgram(programId: Long, updates: Map<String, Any>): Result<Boolean> {
        return try {
            val response = client.patch("$baseUrl/lp/$programId") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            val isSuccess = response.status.value in 200..299
            Result.Success(isSuccess)
        } catch (e: Exception) {
            Result.Error("Failed to update loyalty program: ${e.message}", e)
        }
    }

    suspend fun deleteLoyaltyProgram(id: Long): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/lp/$id")
            val isSuccess = response.status.value in 200..299
            Result.Success(isSuccess)
        } catch (e: Exception) {
            Result.Error("Failed to delete loyalty program: ${e.message}", e)
        }
    }
}