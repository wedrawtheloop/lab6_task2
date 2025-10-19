package com.example.lab6_task2.models

import kotlinx.serialization.Serializable

@Serializable
class Client {
    var id_client: Long? = null
    var last_name: String
    var first_name: String
    var patronymic: String
    var phone_number: String
    var adress: String
    var email: String
    var date_registration: String
    var loyaltyProgram: LoyaltyProgram

    constructor(
        lastName: String,
        firstName: String,
        patronymic: String,
        phoneNumber: String,
        address: String,
        email: String,
        dateRegistration: String,
        loyaltyProgram: LoyaltyProgram
    ) {
        this.last_name = lastName
        this.first_name = firstName
        this.patronymic = patronymic
        this.phone_number = phoneNumber
        this.adress = address
        this.email = email
        this.date_registration = dateRegistration
        this.loyaltyProgram = loyaltyProgram
    }
}