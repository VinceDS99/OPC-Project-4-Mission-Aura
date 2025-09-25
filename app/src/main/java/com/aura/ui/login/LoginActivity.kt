package com.aura.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.flow.collect

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    lifecycleScope.launchWhenStarted {
      viewModel.uiState.collect { state ->
        // Bouton activé uniquement si formulaire complet et pas en chargement
        binding.login.isEnabled = state.isButtonEnabled && !state.isLoading

        // ProgressBar visible pendant le login
        binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Si login terminé
        state.loginSuccess?.let { success ->
          if (success) {
            // Passe l'identifiant à HomeActivity via Intent extras
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            intent.putExtra("USER_ID", state.id)
            startActivity(intent)
            finish()
          } else {
            state.errorMessage?.let { msg ->
              Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
            }
          }
        }
      }
    }

    // Écoute de la saisie
    binding.identifier.doOnTextChanged { text, _, _, _ ->
      viewModel.onIdChanged(text.toString())
    }
    binding.password.doOnTextChanged { text, _, _, _ ->
      viewModel.onPasswordChanged(text.toString())
    }

    // Click login
    binding.login.setOnClickListener {
      viewModel.login()
    }
  }
}
