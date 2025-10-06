package com.aura.ui.home

import com.aura.ui.home.model.Account
import retrofit2.http.GET
import retrofit2.http.Path

interface HomeApiService {
    @GET("/accounts/{id}")
    suspend fun getAccounts(@Path("id") userId: String): List<Account>
}
