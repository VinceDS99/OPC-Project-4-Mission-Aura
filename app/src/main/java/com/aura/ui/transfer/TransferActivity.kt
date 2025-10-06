package com.aura.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityTransferBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.flow.collect

class TransferActivity : AppCompatActivity() {

  private lateinit var binding: ActivityTransferBinding
  private val viewModel: TransferViewModel by viewModels()
  private lateinit var senderId: String
  private var userBalance: Double = 0.0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)

    senderId = intent.getStringExtra("USER_ID") ?: ""
    userBalance = intent.getDoubleExtra("USER_BALANCE", 0.0)

    // 🔹 Champ destinataire
    binding.recipient.addTextChangedListener {
      viewModel.onRecipientChanged(it.toString())
    }

    // 🔹 Champ montant limité à 2 chiffres après la virgule
    binding.amount.addTextChangedListener { editable ->
      editable?.let {
        var text = it.toString()
        if (text.contains(".")) {
          val parts = text.split(".")
          if (parts.size == 2 && parts[1].length > 2) {
            text = parts[0] + "." + parts[1].substring(0, 2)
            binding.amount.setText(text)
            binding.amount.setSelection(text.length)
          }
        }
        viewModel.onAmountChanged(text)
      }
    }

    // 🔹 Bouton de transfert
    binding.transfer.setOnClickListener {
      viewModel.transfer(senderId, userBalance)
    }

    // 🔹 Observer l’état UI
    lifecycleScope.launchWhenStarted {
      viewModel.uiState.collect { state ->
        binding.transfer.isEnabled = state.isTransferButtonEnabled
        binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        state.success?.let { success ->
          if (success) {
            Toast.makeText(this@TransferActivity, "Transfert réussi !", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@TransferActivity, HomeActivity::class.java)
            intent.putExtra("USER_ID", senderId)
            startActivity(intent)
            finish()
          } else {
            state.errorMessage?.let { msg ->
              Toast.makeText(this@TransferActivity, msg, Toast.LENGTH_SHORT).show()
            }
          }
        }
      }
    }
  }
}
