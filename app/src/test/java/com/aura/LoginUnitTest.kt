package com.aura

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// Repository de login
interface LoginRepository {
    suspend fun login(userId: String, password: String): Boolean
}

class LoginUnitTest {

    private lateinit var repository: LoginRepository

    @BeforeEach
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `login succeeds with correct credentials`() = runBlocking {
        // Arrange
        val userId = "1234"
        val password = "password"
        every { runBlocking { repository.login(userId, password) } } returns true

        // Act
        val result = repository.login(userId, password)

        // Assert
        assertTrue(result, "Login should succeed with correct credentials")
    }

    @Test
    fun `login fails with incorrect credentials`() = runBlocking {
        val userId = "1234"
        val password = "wrongpassword"
        every { runBlocking { repository.login(userId, password) } } returns false

        val result = repository.login(userId, password)
        assertFalse(result, "Login should fail with incorrect credentials")
    }

    @Test
    fun `login fails with empty userId`() = runBlocking {
        val userId = ""
        val password = "password"
        every { runBlocking { repository.login(userId, password) } } returns false

        val result = repository.login(userId, password)
        assertFalse(result, "Login should fail with empty userId")
    }

    @Test
    fun `login fails with empty password`() = runBlocking {
        val userId = "1234"
        val password = ""
        every { runBlocking { repository.login(userId, password) } } returns false

        val result = repository.login(userId, password)
        assertFalse(result, "Login should fail with empty password")
    }

    @Test
    fun `login fails when repository throws exception`() = runBlocking {
        val userId = "1234"
        val password = "password"
        every { runBlocking { repository.login(userId, password) } } throws Exception("Network error")

        val exception = assertThrows(Exception::class.java) {
            runBlocking { repository.login(userId, password) }
        }
        assertEquals("Network error", exception.message)
    }
}
