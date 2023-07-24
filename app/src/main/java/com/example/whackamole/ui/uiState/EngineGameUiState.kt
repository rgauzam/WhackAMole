package com.example.whackamole.ui.uiState

import com.example.whackamole.presentation.GameState

data class EngineGameUiState(
    val gameState: GameState,
    val isGame: Boolean = false
) {

}