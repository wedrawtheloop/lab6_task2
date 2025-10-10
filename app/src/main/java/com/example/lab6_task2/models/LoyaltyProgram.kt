package com.example.lab6_task2.models

import kotlinx.serialization.Serializable

@Serializable
class LoyaltyProgram {

    val id: Long? = null
    val loyaltyLevel: Int
    val discountAmount: Int
    val validityPeriod: Int
    val description: String

    constructor(loyaltyLevel: Int, discountAmount: Int, validityPeriod: Int, description: String) {
        this.loyaltyLevel = loyaltyLevel
        this.discountAmount = discountAmount
        this.validityPeriod = validityPeriod
        this.description = description
    }
}