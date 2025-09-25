package com.aura.repository

import com.aura.model.login.Credentials
import com.aura.model.login.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL

sealed class LoginResult {
    object Success : LoginResult()
    object Failure : LoginResult()
    object NoConnection : LoginResult()
}

object LoginRepository {

    suspend fun login(credentials: Credentials): LoginResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:9091/login")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val body = Json.encodeToString(credentials)
                println("Request body: $body")
                connection.outputStream.use { it.write(body.toByteArray()) }

                val code = connection.responseCode
                println("Response code: $code")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                println("Response body: $response")

                if (code == 200) {
                    val loginResponse = Json.decodeFromString<LoginResponse>(response)
                    if (loginResponse.granted) LoginResult.Success else LoginResult.Failure
                } else {
                    LoginResult.Failure
                }

            } catch (e: SocketException) {
                e.printStackTrace()
                LoginResult.NoConnection
            } catch (e: IOException) {
                e.printStackTrace()
                LoginResult.Failure
            } catch (e: Exception) {
                e.printStackTrace()
                LoginResult.Failure
            }
        }
    }
}
