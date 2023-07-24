package com.example.whackamole.ui.viewModel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.whackamole.presentation.GameState
import com.example.whackamole.ui.uiState.EndGameUiState
import com.example.whackamole.ui.uiState.EngineGameUiState
import com.example.whackamole.ui.uiState.PlayGameUiState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class GameViewModel(val context: Context) : ViewModel() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ranking_preferences", Context.MODE_PRIVATE)

    init {
        loadRanking()
    }


    private val _uiState = MutableStateFlow(EngineGameUiState(gameState = GameState.StartGame))
    val uiState: StateFlow<EngineGameUiState> = _uiState.asStateFlow()

    private val _uiStatePlayGame = MutableStateFlow(PlayGameUiState())
    val uiStatePlayGameUiState: StateFlow<PlayGameUiState> = _uiStatePlayGame.asStateFlow()

    private val _uiStateEndGame = MutableStateFlow(EndGameUiState(endScore()))
    val uiStateEndGame: StateFlow<EndGameUiState> = _uiStateEndGame.asStateFlow()

    //zmienia widok
    fun next(gameState: GameState) {
        _uiState.value = _uiState.value.copy(gameState = gameState)
    }

    // cos z tym nie dziala tak samo jak cos nie działa z koorutynami i nie wyłączają sie kiedy trzeba
    fun chooseLevel(lvl: Int) {
        when (lvl) {
            1 -> _uiStatePlayGame.value = _uiStatePlayGame.value.copy(buttonColorDuration = 5000L)
            2 -> _uiStatePlayGame.value = _uiStatePlayGame.value.copy(buttonColorDuration = 200L)
        }

    }

    //mechanika gry

    private var levelJob: Job? = null
    private var colorButtonJob: Job? = null

    suspend fun levelUp() {
        withContext(Dispatchers.Main) {
            _uiStatePlayGame.value = _uiStatePlayGame.value.copy(
                buttonColorDuration = _uiStatePlayGame.value.buttonColorDuration - 50L,
                moleWaiting = _uiStatePlayGame.value.moleWaiting - 50L
            )


        }
    }

    fun nextLevel() {

        levelJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {

                delay(1500)
                // Sprawdź, czy praca powinna być zakończona
                if (_uiState.value.gameState == GameState.EndGame) {
                    // Wyłącz funkcję nextLevel, jeśli gameState jest EndGame
                    levelJob?.cancel()
                }

                levelUp()
                if (_uiStatePlayGame.value.buttonColorDuration <= 0L) {
                    // Jeśli czas pokazywania przycisku jest mniejszy lub równy 0, zakończ grę
                    endGame()
                    levelJob?.cancel()
                }
            }
        }
    }


    fun randomlyColorButton() {
        // Zakończ poprzednią pracę colorButtonJob, jeśli istnieje
        colorButtonJob?.cancel()
        colorButtonJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(500)
                // Sprawdź, czy praca powinna być zakończona
                if (_uiState.value.gameState == GameState.EndGame) {
                    colorButtonJob?.cancel()
                    levelJob?.cancel()
                }
                val uncoloredMoles =
                    _uiStatePlayGame.value.moles.filterValues { value -> !value }.keys.toList()
                if (uncoloredMoles.isEmpty()) {
                    continue
                }

                val randomMoleIndex = Random.nextInt(uncoloredMoles.size)
                val mole = uncoloredMoles[randomMoleIndex]

                // Pokoloruj mole
                withContext(Dispatchers.Main) {
                    _uiStatePlayGame.value = _uiStatePlayGame.value.copy(moles = makeMap(mole))

                }

                // Poczekaj, aż czas trwania koloru mole minie
                delay(_uiStatePlayGame.value.moleWaiting)

                // Przywróć mole do pierwotnego stanu, jeśli nadal jest pokolorowane (nie zostało kliknięte)
                if (_uiStatePlayGame.value.moles[mole] == true) {
                    withContext(Dispatchers.Main) {
                        _uiStatePlayGame.value = _uiStatePlayGame.value.copy(moles = makeMap(0))
                        lostlife()
                    }
                }

            }
        }
    }

    fun makeMap(position: Int): Map<Int, Boolean> {
        return (1..9).map { it to (it == position) }.toMap()
    }

    fun restartGame() {

        _uiStatePlayGame.value = _uiStatePlayGame.value.copy(lives = 3, score = 0)
    }

    fun lostlife() {
        _uiStatePlayGame.value =
            _uiStatePlayGame.value.copy(lives = _uiStatePlayGame.value.lives - 1)
        Log.e("life", "stracone" + _uiStatePlayGame.value.lives)
        if (isLose()) endGame()
    }

    fun isLose(): Boolean {
        return _uiStatePlayGame.value.lives < 1

    }

    fun endScore(): Int {
        return _uiStatePlayGame.value.score
    }


    fun onButtonClicked(mole: Int) {
        val currentState = _uiStatePlayGame.value
        val currentMoles = currentState.moles

        val moleState = _uiStatePlayGame.value.moles[mole]
        if (currentMoles[mole] == true) {
            _uiStatePlayGame.value = currentState.copy(
                score = currentState.score + 1,
                moles = currentMoles.toMutableMap().apply { set(mole, false) }
            )
        } else {
            lostlife()
        }
    }


    fun endGame() {
        levelJob?.cancel()
        _uiStateEndGame.value.endScore = endScore()
        restartGame()
        next(GameState.EndGame)
    }

    class Gamer(val name: String, val score: Int)

    var ranking: MutableList<Gamer> = getRankingFromSharedPreferences()

    fun getRankingFromSharedPreferences(): MutableList<Gamer> {
        val rankingJson = sharedPreferences.getString("ranking", "")
        if (rankingJson == null || rankingJson == "") {
            return mutableListOf()
        }

        val type = object : TypeToken<List<Gamer>>() {}.type
        val ranking: MutableList<Gamer> = Gson().fromJson(rankingJson, type)
        return ranking
    }

    fun loadRanking() {

        val rankingJson = sharedPreferences.getString("ranking", "")
        if (!rankingJson.isNullOrEmpty()) {


            val type = object : TypeToken<List<Gamer>>() {}.type
            ranking = Gson().fromJson(rankingJson, type)
        }
    }

    fun saveRanking() {
        val rankingJson = Gson().toJson(ranking)
        sharedPreferences.edit().putString("ranking", rankingJson).apply()
    }


    fun addPlayerToRanking(nick: String, score: Int) {
        ranking.add(Gamer(nick, score))
        ranking.sortByDescending { it.score }
        saveRanking()
    }

}
