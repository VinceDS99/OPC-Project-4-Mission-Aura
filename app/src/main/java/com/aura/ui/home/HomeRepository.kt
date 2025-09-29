package com.aura.ui.home

import com.aura.ui.home.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL

sealed class HomeResult {
    data class Success(val accounts: List<Account>) : HomeResult()
    object NoConnection : HomeResult()
    object Failure : HomeResult()
}

object HomeRepository {

    suspend fun getAccounts(userId: String): HomeResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:9091/accounts/$userId") // ⚠️ Vérifie ton serveur
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")

                val code = connection.responseCode
                val response = connection.inputStream.bufferedReader().use { it.readText() }

                return@withContext if (code == 200) {
                    val accounts = Json.decodeFromString<List<Account>>(response)
                    HomeResult.Success(accounts)
                } else {
                    HomeResult.Failure
                }

            } catch (e: SocketException) {
                e.printStackTrace()
                HomeResult.NoConnection
            } catch (e: IOException) {
                e.printStackTrace()
                HomeResult.Failure
            } catch (e: Exception) {
                e.printStackTrace()
                HomeResult.Failure
            }
        }
    }
}
