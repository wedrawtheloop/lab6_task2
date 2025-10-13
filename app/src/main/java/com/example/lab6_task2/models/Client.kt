package com.example.lab6_task2.models

import kotlinx.serialization.Serializable

@Serializable
class Client {
    var id: Long? = null
    var lastName: String
    var firstName: String
    var patronymic: String
    var phoneNumber: String
    var address: String
    var email: String
    var dateRegistration: String
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
        this.lastName = lastName
        this.firstName = firstName
        this.patronymic = patronymic
        this.phoneNumber = phoneNumber
        this.address = address
        this.email = email
        this.dateRegistration = dateRegistration
        this.loyaltyProgram = loyaltyProgram
    }
}