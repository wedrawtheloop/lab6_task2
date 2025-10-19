package com.example.lab6_task2

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
        install(ContentNegotiation){
            json(Json{
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

    }
    private val baseUrl = "http://10.0.2.2:8080"

    suspend fun getAllClients(): List<Client>{
        return client.get("$baseUrl/clients").body()
    }

    suspend fun createClient(client: Client): Client {
        return this.client.post("$baseUrl/clients"){
            contentType(ContentType.Application.Json)
            setBody(client)
        }.body()
    }

    suspend fun patchClient(clientId: Long, updates: Map<String, Any>): Boolean {
        return try {
            val response = client.patch("$baseUrl/clients/$clientId") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            val isSuccess = response.status.value in 200..299
            isSuccess
        } catch (e: Exception) {
            println("Error patching client: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteClient(id: Long){
        client.delete("$baseUrl/clients/$id")
    }


    suspend fun getAllLoyaltyPrograms(): List<LoyaltyProgram> {
        return try {
            val programs = client.get("$baseUrl/lp").body<List<LoyaltyProgram>>()
            programs
        } catch (e: Exception) {
            println("Error loading programs: ${e.message}")
            emptyList()
        }
    }

    suspend fun createLoyaltyProgram(program: LoyaltyProgram): LoyaltyProgram {
        return client.post("$baseUrl/lp"){
            contentType(ContentType.Application.Json)
            setBody(program)
        }.body()
    }

    suspend fun patchLoyaltyProgram(programId: Long, updates: Map<String, Any>): Boolean {
        return try {
            val response = client.patch("$baseUrl/lp/$programId") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }

            val isSuccess = response.status.value in 200..299

            isSuccess
        } catch (e: Exception) {
            println("Error patching loyalty program: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    suspend fun deleteLoyaltyProgram(id: Long) {
        client.delete("$baseUrl/lp/$id")
    }


}