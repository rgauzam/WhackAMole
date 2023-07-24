@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.whackamole.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whackamole.R
import com.example.whackamole.ui.viewModel.GameViewModel
import kotlinx.coroutines.cancel


enum class GameState {
    StartGame,
    LevelGame,
    PlayGame,
    Ranking,
    EndGame
}

@Composable
fun Game(gameViewModel: GameViewModel) {
    val state = gameViewModel.uiState.collectAsState()

    LaunchedEffect(state.value.gameState) {
        if (state.value.gameState == GameState.PlayGame) {
            gameViewModel.randomlyColorButton()
        } else coroutineContext.cancel()
    }

    when (state.value.gameState) {
        GameState.StartGame -> StartLayout(gameViewModel)

        GameState.LevelGame -> LevelLayout(gameViewModel)

        GameState.PlayGame -> GameLayout(gameViewModel)

        GameState.EndGame -> GameOverLayout(gameViewModel)

        GameState.Ranking -> RankingLayout(gameViewModel)
    }
}

@Composable
fun StartLayout(gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.whack_a_mole),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .size(40.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { gameViewModel.next(GameState.LevelGame) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
            )

        ) {
            Text(stringResource(R.string.start))
        }
        Button(
            onClick = { gameViewModel.next(GameState.Ranking) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
            )

        ) {
            Text(stringResource(R.string.ranking))
        }
        Spacer(modifier = Modifier.height(150.dp))
    }
}


@Composable
fun LevelLayout(gameViewModel: GameViewModel) {
    gameViewModel.restartGame()
    Column(
        modifier = Modifier
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(

            text = stringResource(R.string.level),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .size(40.dp),

            textAlign = TextAlign.Center
        )
        Button(
            onClick = {
                gameViewModel.chooseLevel(1)
                gameViewModel.next(GameState.PlayGame)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1")
        }
        Button(
            onClick = {
                gameViewModel.chooseLevel(2)
                gameViewModel.next(GameState.PlayGame)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("2")
        }

        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun GameLayout(gameViewModel: GameViewModel) {

    val uiStatePlayGameUiState by gameViewModel.uiStatePlayGameUiState.collectAsState()
    val moles = uiStatePlayGameUiState.moles

    Column(
        modifier = Modifier
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(

            text = "${stringResource(R.string.score)}: ${uiStatePlayGameUiState.score}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .size(40.dp),
            textAlign = TextAlign.Center,

            )
        Text(

            text = "${stringResource(R.string.lives)}: ${uiStatePlayGameUiState.lives}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .size(40.dp),
            textAlign = TextAlign.Center,

            )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.size(300.dp)
        ) {
            items(moles.keys.toList()) { mole ->
                Box(
                    modifier = Modifier
                        .size(75.dp)

                        .border(
                            2.dp,
                            Color(0xFF835803),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            gameViewModel.onButtonClicked(mole)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (moles[mole] == true) {
                        Image(
                            painter = painterResource(id = R.drawable.mole),
                            contentDescription = "Mole Icon"
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun GameOverLayout(gameViewModel: GameViewModel) {
    val uiStateEndGame by gameViewModel.uiStateEndGame.collectAsState()

    var nickInput by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .padding(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(

            text = stringResource(R.string.end),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .size(80.dp),
            textAlign = TextAlign.Center,

            )
        Text(

            text = "${stringResource(R.string.score)}: ${uiStateEndGame.endScore}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp)
                .size(40.dp),
            textAlign = TextAlign.Center,

            )
        EditField(label = R.string.enter_nickname, value = nickInput, { nickInput = it })
        Button(
            onClick = {
                gameViewModel.addPlayerToRanking(nickInput, uiStateEndGame.endScore)
                gameViewModel.next(GameState.StartGame)
            },
            modifier = Modifier.padding(20.dp)
        ) {
            Text(

                text = "${stringResource(R.string.done)}",
                modifier = Modifier,
                textAlign = TextAlign.Center,

                )
        }


    }
}

@Composable
fun RankingLayout(gameViewModel: GameViewModel) {

    val ranking = gameViewModel.ranking

    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ranking),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .size(40.dp),
            textAlign = TextAlign.Center
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(ranking.size) { index ->
                val gamer = ranking[index]
                Text(text = "${index + 1} ${gamer.name}   Score: ${gamer.score}")
            }
        }

        Button(
            onClick = { gameViewModel.next(GameState.StartGame) },
            modifier = Modifier
        ) {
            Text("Come back")
        }


    }
}

@Composable
fun EditField(
    @StringRes label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(stringResource(label)) },
        modifier = modifier
    )
}


