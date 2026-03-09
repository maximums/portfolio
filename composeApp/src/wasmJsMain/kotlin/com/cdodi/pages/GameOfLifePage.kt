package com.cdodi.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdodi.data.gameoflife.Cell
import com.cdodi.data.gameoflife.GameOfLifeManager
import com.cdodi.data.gameoflife.Grid

@Composable
fun GameOfLifePage() {
    val gameOfLifeManager = remember { GameOfLifeManager() }
    val cells = gameOfLifeManager.aliveCells.collectAsStateWithLifecycle()

    LaunchedEffect(gameOfLifeManager) {
        gameOfLifeManager.loop()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = gameOfLifeManager::playPause) {
                Text("Start")
            }

            Text(
                text = "Population: ${cells.value.size}",
                color = Color.White,
                fontSize = 20.sp
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
                .onSizeChanged { size ->
                    val grid = getGridBounds(size)
                    gameOfLifeManager.updateGridBounds(grid)
                }
                .pointerInput(gameOfLifeManager) {
                    detectTapGestures { offset -> gameOfLifeManager.addCell(offset) }
                }
        ) {
            drawAliveCells(cells.value)
            drawGrid()
        }
    }
}

private fun DrawScope.drawGrid() {
    val columns = (size.width / GameOfLifeManager.CELL_SIZE_PX).toInt()
    val rows = (size.height / GameOfLifeManager.CELL_SIZE_PX).toInt()
    val gridWidth = columns * GameOfLifeManager.CELL_SIZE_PX
    val gridHeight = rows * GameOfLifeManager.CELL_SIZE_PX

    for (i in 0..columns) {
        drawLine(
            color = Color.Green,
            start = Offset(x = i * GameOfLifeManager.CELL_SIZE_PX, y = 0f),
            end = Offset(x = i * GameOfLifeManager.CELL_SIZE_PX, y = gridHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    for (j in 0..rows) {
        drawLine(
            color = Color.Green,
            start = Offset(x = 0f, y = j * GameOfLifeManager.CELL_SIZE_PX),
            end = Offset(x = gridWidth, y = j * GameOfLifeManager.CELL_SIZE_PX),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawAliveCells(cells: Set<Cell>) {
    val cellSizePx = GameOfLifeManager.CELL_SIZE_PX

    for (cell in cells) {
        drawRect(
            color = Color.Red,
            topLeft = Offset(cell.x * cellSizePx, cell.y * cellSizePx),
            size = Size(cellSizePx, cellSizePx),
        )
    }
}

private fun getGridBounds(screenSize: IntSize): Grid {
    val columns = screenSize.width / GameOfLifeManager.CELL_SIZE_PX
    val rows = screenSize.height / GameOfLifeManager.CELL_SIZE_PX

    return columns.fastRoundToInt() to rows.fastRoundToInt()
}