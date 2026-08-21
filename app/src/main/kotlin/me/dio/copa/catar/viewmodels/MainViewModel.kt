package me.dio.copa.catar.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dio.copa.catar.core.BaseViewModel
import me.dio.copa.catar.domain.model.MatchDomain
import me.dio.copa.catar.domain.usecase.DisableNotificationsUseCase
import me.dio.copa.catar.domain.usecase.EnableNotificationsUseCase
import me.dio.copa.catar.domain.usecase.GetMatchesUseCase
import me.dio.copa.catar.remote.NotFoundException
import me.dio.copa.catar.remote.UnexpectedException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
  private val matchesUseCase: GetMatchesUseCase,
  private val enableNotificationsUseCase: EnableNotificationsUseCase,
  private val disableNotificationsUseCase: DisableNotificationsUseCase
): BaseViewModel<MainUIState, MainUIAction>(MainUIState()) {
  init {
      loadMatches()
  }

  fun loadMatches() =
    viewModelScope.launch {
      matchesUseCase()
        .flowOn(Dispatchers.Main).catch {
          when(it) {
            is NotFoundException -> sendAction(MainUIAction.MatchesNotFound(it.message ?: "Erro sem mensagem"))
            is UnexpectedException -> sendAction(MainUIAction.Unexpected)
          }
      }.collect { matches ->
        setState {
          copy(matches = matches)
        }
        }
    }

  fun toggleNotification(match: MatchDomain) {
    viewModelScope.launch {
      runCatching {
        withContext(Dispatchers.Main) {
          val action = if (match.notificationEnabled) {
            disableNotificationsUseCase(match.id)
            MainUIAction.DisableNotification(match)
          } else {
            enableNotificationsUseCase(match.id)
            MainUIAction.EnableNotification(match)
          }

          sendAction(action)
        }
      }
    }
  }
}

data class MainUIState(
  val matches: List<MatchDomain> = emptyList()
  )

sealed interface MainUIAction {
  data class MatchesNotFound(val message: String) : MainUIAction
  data class EnableNotification(val match: MatchDomain) : MainUIAction
  data class DisableNotification(val match: MatchDomain) : MainUIAction
  object Unexpected: MainUIAction
}
