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
        // Activation du bouton uniquement si les champs sont remplis
        binding.login.isEnabled = state.isButtonEnabled

        // Affiche ou cache la ProgressBar
        binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Affiche le toast uniquement si loginSuccess n'est pas null
        state.loginSuccess?.let { success ->
          if (success) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
          } else {
            Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    binding.identifier.doOnTextChanged { text, _, _, _ ->
      viewModel.onIdChanged(text.toString())
    }

    binding.password.doOnTextChanged { text, _, _, _ ->
      viewModel.onPasswordChanged(text.toString())
    }

    binding.login.setOnClickListener {
      viewModel.login()
    }
  }
}
