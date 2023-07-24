package com.example.whackamole.ui.uiState

data class PlayGameUiState(
    val score: Int = 0,
    val lives: Int = 3,
    var buttonColorDuration: Long = 20L,
    var moleWaiting: Long = 1000L,
    val moles: Map<Int, Boolean> = mapOf
        (
        1 to false,
        2 to false,
        3 to false,
        4 to false,
        5 to false,
        6 to false,
        7 to false,
        8 to false,
        9 to false
    )

)

