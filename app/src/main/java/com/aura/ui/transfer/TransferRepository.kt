package com.aura.ui.transfer

import android.util.Log
import com.aura.ui.home.HomeApiService
import com.aura.ui.transfer.model.TransferRequest
import com.aura.ui.transfer.model.TransferResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class TransferResult {
    object Success : TransferResult()
    object NoConnection : TransferResult()
    object InvalidRecipient : TransferResult()
    object InsufficientFunds : TransferResult()
    object InvalidAmount : TransferResult()
    object Failure : TransferResult()
}

class TransferRepository {

    private val api: TransferApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9091")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TransferApiService::class.java)
    }

    private val homeApi: HomeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9091")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomeApiService::class.java)
    }

    suspend fun makeTransfer(sender: String, recipient: String, amount: Double): TransferResult {
        return withContext(Dispatchers.IO) {
            try {
                if (amount <= 0.0) return@withContext TransferResult.InvalidAmount

                // 🔹 Vérifier si le destinataire existe
                try {
                    val accounts = homeApi.getAccounts(recipient)
                    if (accounts.isEmpty()) return@withContext TransferResult.InvalidRecipient
                } catch (e: HttpException) {
                    // Si le destinataire n'existe pas, API peut renvoyer 404
                    if (e.code() == 404) return@withContext TransferResult.InvalidRecipient
                    else throw e
                }

                Log.d("TransferRepository", "Tentative de transfert : sender=$sender, recipient=$recipient, amount=$amount")

                val response: TransferResponse = api.transfer(TransferRequest(sender, recipient, amount))

                Log.d("TransferRepository", "Réponse backend: result=${response.result}, message=${response.message}")

                return@withContext if (response.result) TransferResult.Success
                else TransferResult.Failure

            } catch (e: UnknownHostException) {
                Log.e("TransferRepository", "Erreur réseau : UnknownHostException")
                TransferResult.NoConnection
            } catch (e: SocketException) {
                Log.e("TransferRepository", "Erreur réseau : SocketException")
                TransferResult.NoConnection
            } catch (e: SocketTimeoutException) {
                Log.e("TransferRepository", "Erreur réseau : SocketTimeoutException")
                TransferResult.NoConnection
            } catch (e: IOException) {
                Log.e("TransferRepository", "Erreur réseau : IOException")
                TransferResult.NoConnection
            } catch (e: HttpException) {
                Log.e("TransferRepository", "Erreur HTTP ${e.code()} : ${e.message()}")
                TransferResult.Failure
            } catch (e: Exception) {
                Log.e("TransferRepository", "Exception inconnue : ${e.message}", e)
                TransferResult.Failure
            }
        }
    }
}
