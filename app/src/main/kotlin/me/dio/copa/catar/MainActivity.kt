package me.dio.copa.catar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import me.dio.copa.catar.ui.theme.Copa2022Theme
import me.dio.copa.catar.viewmodels.MainViewModel
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import me.dio.copa.catar.extensions.observe
import me.dio.copa.catar.notification.scheduler.extensions.NotificationMatchWorker
import me.dio.copa.catar.viewmodels.MainUIAction

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeActions()
        setContent {
            Copa2022Theme {
                val state by viewModel.state.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                     MainScreen(matches = state.matches, viewModel::toggleNotification)
                }
            }
        }
    }

  private fun observeActions() {
    viewModel.action.observe(this) { action ->
      when(action) {
        is MainUIAction.DisableNotification -> NotificationMatchWorker.cancel(applicationContext, action.match)
        is MainUIAction.EnableNotification -> NotificationMatchWorker.start(applicationContext, action.match)
        is MainUIAction.MatchesNotFound -> TODO()
        MainUIAction.Unexpected -> TODO()
      }
    }
  }

}

