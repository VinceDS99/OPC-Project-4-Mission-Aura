package com.aura

import com.aura.ui.transfer.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransferUnitTest {

    private lateinit var repository: TransferRepository
    private lateinit var viewModel: TransferViewModel

    @BeforeEach
    fun setup() {
        repository = mockk()
        viewModel = TransferViewModel(repository)
    }

    @Test
    fun `transfer fails when sending to self`() = runBlocking {
        val senderId = "1234"
        val recipientId = "1234"
        val amount = 10.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())
        viewModel.transfer(senderId, balance)

        val state = viewModel.uiState.value
        assertEquals("Vous ne pouvez pas transférer vers votre propre compte", state.errorMessage)
        assertFalse(state.success ?: true)
    }

    @Test
    fun `transfer fails with invalid amount`() = runBlocking {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 0.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())
        viewModel.transfer(senderId, balance)

        val state = viewModel.uiState.value
        assertEquals("Montant invalide (doit être supérieur à 0)", state.errorMessage)
        assertFalse(state.success ?: true)
    }

    @Test
    fun `transfer fails when amount exceeds balance`() = runBlocking {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 150.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())
        viewModel.transfer(senderId, balance)

        val state = viewModel.uiState.value
        assertEquals("Solde insuffisant pour effectuer ce transfert", state.errorMessage)
        assertFalse(state.success ?: true)
    }

    @Test
    fun `transfer succeeds with correct input`() = runBlocking {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 50.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        // On mocke directement TransferRepository
        coEvery { repository.makeTransfer(senderId, recipientId, amount) } returns TransferResult.Success

        viewModel.transfer(senderId, balance)

        val state = viewModel.uiState.value
        assertTrue(state.success ?: false)
        assertNull(state.errorMessage)
    }

    @Test
    fun `transfer fails with no internet`() = runBlocking {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 50.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        coEvery { repository.makeTransfer(senderId, recipientId, amount) } returns TransferResult.NoConnection

        viewModel.transfer(senderId, balance)

        val state = viewModel.uiState.value
        assertEquals("Pas de connexion Internet", state.errorMessage)
        assertFalse(state.success ?: true)
    }
}
