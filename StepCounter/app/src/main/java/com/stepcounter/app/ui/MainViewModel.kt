package com.stepcounter.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepcounter.app.data.StepsRepository
import com.stepcounter.app.data.local.StepsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val steps: Int = 0,
    val goal: Int = StepsPreferences.DEFAULT_GOAL,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: StepsRepository,
) : ViewModel() {

    private val previousSteps = MutableStateFlow<Int?>(null)

    private val _goalReachedWhileVisible = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val goalReachedWhileVisible = _goalReachedWhileVisible.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.observeTodaySteps(),
        repository.observeDailyGoal(),
    ) { steps, goal ->
        MainUiState(steps = steps, goal = goal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    init {
        viewModelScope.launch {
            combine(
                repository.observeTodaySteps(),
                repository.observeDailyGoal(),
            ) { steps, goal -> steps to goal }
                .collect { (steps, goal) ->
                    val prev = previousSteps.value
                    if (prev != null && prev < goal && steps >= goal) {
                        _goalReachedWhileVisible.emit(Unit)
                    }
                    previousSteps.update { steps }
                }
        }
    }
    
    /**
     * Увеличивает цель на [delta] шагов.
     */
    fun adjustGoal(delta: Int) {
        viewModelScope.launch {
            val currentGoal = repository.getDailyGoal()
            val newGoal = (currentGoal + delta).coerceAtLeast(100)
            repository.setDailyGoal(newGoal)
        }
    }
    
    /**
     * Устанавливает конкретное значение цели.
     */
    fun setGoal(value: Int) {
        viewModelScope.launch {
            repository.setDailyGoal(value.coerceAtLeast(100))
        }
    }
}
