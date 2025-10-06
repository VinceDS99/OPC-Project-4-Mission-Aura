package com.aura.ui.transfer

import com.aura.ui.transfer.model.TransferRequest
import com.aura.ui.transfer.model.TransferResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TransferApiService {
    @POST("/transfer")
    suspend fun transfer(@Body request: TransferRequest): TransferResponse
}
