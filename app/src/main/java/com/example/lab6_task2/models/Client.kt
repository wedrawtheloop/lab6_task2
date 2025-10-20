package com.example.lab6_task2.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
class Client {
    var id_client: Long? = null
    var last_name: String
    var first_name: String
    var patronymic: String
    var phone_number: String
    var adress: String
    var email: String
    @Contextual
    var date_registration: String
    var loyaltyProgram: LoyaltyProgram

    constructor(
        last_name: String,
        first_name: String,
        patronymic: String,
        phone_number: String,
        address: String,
        email: String,
        date_registration: String,
        loyalty_program: LoyaltyProgram
    ) {
        this.last_name = last_name
        this.first_name = first_name
        this.patronymic = patronymic
        this.phone_number = phone_number
        this.adress = address
        this.email = email
        this.date_registration = date_registration
        this.loyaltyProgram = loyalty_program
    }
}