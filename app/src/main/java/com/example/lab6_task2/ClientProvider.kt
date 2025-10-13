package com.example.lab6_task2

import com.example.lab6_task2.models.Client
import com.example.lab6_task2.models.LoyaltyProgram
import com.google.android.datatransport.runtime.logging.Logging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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

    suspend fun getClientById(id: Long): Client {
        return client.get("$baseUrl/clients/$id").body()
    }

    suspend fun createClient(client: Client): Client {
        return this.client.post("$baseUrl/clients"){
            contentType(ContentType.Application.Json)
            setBody(client)
        }.body()
    }

    suspend fun updateClient(id: Long, client: Client): Client {
        return this.client.put("$baseUrl/clients/$id") {
            contentType(ContentType.Application.Json)
            setBody(client)
        }.body()
    }

    suspend fun deleteClient(id: Long){
        client.delete("$baseUrl/clients/$id")
    }


    suspend fun getAllLoyaltyPrograms(): List<LoyaltyProgram> {
        return client.get("$baseUrl/lp").body()
    }

    suspend fun getLoyaltyProgram(id: Long): LoyaltyProgram {
        return client.get("$baseUrl/lp/$id").body()
    }

    suspend fun createLoyaltyProgram(program: LoyaltyProgram): LoyaltyProgram {
        return client.post("$baseUrl/lp"){
            contentType(ContentType.Application.Json)
            setBody(program)
        }.body()
    }

    suspend fun updateLoyaltyProgram(id: Long, program: LoyaltyProgram): LoyaltyProgram {
        return client.put("$baseUrl/lp/$id") {
            contentType(ContentType.Application.Json)
            setBody(program)
        }.body()
    }

    suspend fun deleteLoyaltyProgram(id: Long) {
        client.delete("$baseUrl/lp/$id")
    }
}