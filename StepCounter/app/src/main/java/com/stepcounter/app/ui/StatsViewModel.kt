package com.stepcounter.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepcounter.app.data.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * Данные для отображения одного дня в истории.
 * [shortLabel] — короткое название дня (например, "Пн").
 * [steps] — количество шагов.
 * [goal] — цель на этот день.
 * [progress] — процент выполнения цели (0.0..1.0).
 * [isGoalReached] — достигнута ли цель.
 */
data class DayHistoryItem(
    val shortLabel: String,
    val steps: Int,
    val goal: Int,
    val progress: Float,
    val isGoalReached: Boolean,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: StepsRepository,
) : ViewModel() {

    val lastSevenDays: StateFlow<List<DayHistoryItem>> = repository.observeLastSevenDays()
        .map { entities ->
            val today = LocalDate.now()
            val byDay = entities.associateBy { it.dateEpochDay }
            (0..6).map { index ->
                val date = today.minusDays(6L - index)
                val shortLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val entity = byDay[date.toEpochDay()]
                val steps = entity?.stepCount ?: 0
                val goal = entity?.goalSteps ?: 1000
                val progress = (steps.toFloat() / goal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                val isGoalReached = steps >= goal
                DayHistoryItem(
                    shortLabel = shortLabel,
                    steps = steps,
                    goal = goal,
                    progress = progress,
                    isGoalReached = isGoalReached,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
