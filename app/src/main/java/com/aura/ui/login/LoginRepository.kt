package com.aura.repository

import com.aura.model.login.Credentials
import com.aura.model.login.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object LoginRepository {

    /**
     * Appelle l'API locale pour effectuer la connexion
     * @param credentials : identifiant et mot de passe
     * @return true si login réussi, false sinon
     */
    suspend fun login(credentials: Credentials): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:9091/login") // 10.0.2.2 pour l'émulateur
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Sérialisation JSON
                val body = Json.encodeToString(credentials)
                println("Request body: $body")

                connection.outputStream.use { it.write(body.toByteArray()) }

                val code = connection.responseCode
                println("Response code: $code")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                println("Response body: $response")

                if (code == 200) {
                    val loginResponse = Json.decodeFromString<LoginResponse>(response)
                    loginResponse.granted
                } else {
                    false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
