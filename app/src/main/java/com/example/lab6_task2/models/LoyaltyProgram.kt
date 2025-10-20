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
        id_loyalty_program: Long?,
        loyalty_level: Int,
        discount_amount: Int,
        validity_period: Int,
        description: String
    ) {
        this.id_loyalty_program = id_loyalty_program
        this.loyalty_level = loyalty_level
        this.discount_amount = discount_amount
        this.validity_period = validity_period
        this.description = description
    }
}