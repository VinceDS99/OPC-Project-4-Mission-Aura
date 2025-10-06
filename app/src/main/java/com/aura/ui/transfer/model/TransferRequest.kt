package com.aura.ui.transfer.model

data class TransferRequest(
    val sender: String,
    val recipient: String,
    val amount: Double
)
