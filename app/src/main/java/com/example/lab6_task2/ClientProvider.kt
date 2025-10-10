package com.example.lab6_task2

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class ClientProvider {
    fun instance(): HttpClient = HttpClient {
        install(ContentNegotiation){
            json(Json)
        }
    }
}