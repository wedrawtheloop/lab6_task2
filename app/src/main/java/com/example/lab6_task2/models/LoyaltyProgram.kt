package com.example.lab6_task2.models

import kotlinx.serialization.Serializable

@Serializable
class LoyaltyProgram {

    var id_loyalty_program: Long? = null
    val loyalty_level: Int
    val discount_amount: Int
    val validity_period: Int
    val description: String

    constructor(
        idLoyaltyProgram: Long?,
        loyaltyLevel: Int,
        discountAmount: Int,
        validityPeriod: Int,
        description: String
    ) {
        this.id_loyalty_program = idLoyaltyProgram
        this.loyalty_level = loyaltyLevel
        this.discount_amount = discountAmount
        this.validity_period = validityPeriod
        this.description = description
    }
}