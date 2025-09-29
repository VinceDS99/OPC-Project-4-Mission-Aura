package com.aura.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.R
import com.aura.databinding.ActivityHomeBinding
import com.aura.ui.login.LoginActivity
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.flow.collect

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding
  private val viewModel: HomeViewModel by viewModels()
  private var userId: String? = null

  private val startTransferActivityForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      // TODO si nécessaire
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    userId = intent.getStringExtra("USER_ID")

    // Observer les changements du ViewModel
    lifecycleScope.launchWhenStarted {
      viewModel.uiState.collect { state ->
        // ProgressBar visible pendant chargement
        binding.loadingProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Retry button visible seulement si erreur
        binding.retryButton.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE

        // Titre visible uniquement si succès
        binding.title.visibility = if (!state.isLoading && state.errorMessage == null) View.VISIBLE else View.GONE

        // Balance ou message d'erreur centré
        binding.balance.visibility = if (state.isLoading) View.GONE else View.VISIBLE
        binding.balance.text = when {
          state.isLoading -> ""
          state.errorMessage != null -> "Erreur : ${state.errorMessage}"
          else -> "${state.balance}€"
        }
      }
    }

    // Charger les comptes si userId dispo
    userId?.let { viewModel.loadAccounts(it) }

    // Retry button pour relancer la requête
    binding.retryButton.setOnClickListener { userId?.let { viewModel.loadAccounts(it) } }

    // Bouton transfer
    binding.transfer.setOnClickListener {
      startTransferActivityForResult.launch(Intent(this@HomeActivity, TransferActivity::class.java))
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.home_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.disconnect -> {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
