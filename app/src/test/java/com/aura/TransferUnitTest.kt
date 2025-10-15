package com.aura

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class TransferUnitTest {


    interface TransferRepository {
        suspend fun makeTransfer(senderId: String, recipientId: String, amount: Double): TransferResult
    }


    sealed class TransferResult {
        object Success : TransferResult()
        object NoConnection : TransferResult()
        data class Error(val message: String) : TransferResult()
    }


    class TransferViewModel(private val repository: TransferRepository) {
        var uiState = UiState()
            private set

        data class UiState(
            val success: Boolean? = null,
            val errorMessage: String? = null
        )

        private var recipientId: String = ""
        private var amount: Double = 0.0

        fun onRecipientChanged(newRecipient: String) {
            recipientId = newRecipient
        }

        fun onAmountChanged(newAmount: String) {
            amount = newAmount.toDoubleOrNull() ?: 0.0
        }

        suspend fun transfer(senderId: String, balance: Double) {
            if (recipientId.isBlank()) {
                uiState = UiState(success = false, errorMessage = "Destinataire vide")
                return
            }
            if (amount <= 0) {
                uiState = UiState(success = false, errorMessage = "Montant invalide")
                return
            }
            if (balance < amount) {
                uiState = UiState(success = false, errorMessage = "Fonds insuffisants")
                return
            }

            when (val result = repository.makeTransfer(senderId, recipientId, amount)) {
                is TransferResult.Success -> uiState = UiState(success = true)
                is TransferResult.NoConnection -> uiState = UiState(success = false, errorMessage = "Pas de connexion Internet")
                is TransferResult.Error -> uiState = UiState(success = false, errorMessage = result.message)
            }
        }
    }


    private lateinit var repository: TransferRepository
    private lateinit var viewModel: TransferViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = TransferViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Tests unitaires ---
    @Test
    fun `transfer succeeds with correct input`() = runTest {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 50.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        coEvery { repository.makeTransfer(senderId, recipientId, amount) } returns TransferResult.Success

        viewModel.transfer(senderId, balance)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue(state.success ?: false)
        assertNull(state.errorMessage)
    }

    @Test
    fun `transfer fails with insufficient balance`() = runTest {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 100.0
        val balance = 50.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        viewModel.transfer(senderId, balance)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.success ?: true)
        assertEquals("Fonds insuffisants", state.errorMessage)
    }

    @Test
    fun `transfer fails with empty recipient`() = runTest {
        val senderId = "1234"
        val recipientId = ""
        val amount = 50.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        viewModel.transfer(senderId, balance)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.success ?: true)
        assertEquals("Destinataire vide", state.errorMessage)
    }

    @Test
    fun `transfer fails with no internet`() = runTest {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = 50.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        coEvery { repository.makeTransfer(senderId, recipientId, amount) } returns TransferResult.NoConnection

        viewModel.transfer(senderId, balance)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertEquals("Pas de connexion Internet", state.errorMessage)
        assertFalse(state.success ?: true)
    }

    @Test
    fun `transfer fails with invalid amount`() = runTest {
        val senderId = "1234"
        val recipientId = "5678"
        val amount = -20.0
        val balance = 100.0

        viewModel.onRecipientChanged(recipientId)
        viewModel.onAmountChanged(amount.toString())

        viewModel.transfer(senderId, balance)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.success ?: true)
        assertEquals("Montant invalide", state.errorMessage)
    }
}
