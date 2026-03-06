package com.cdodi.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdodi.data.gameoflife.GameOfLifeManager
import com.cdodi.data.gameoflife.Grid

@Composable
fun GameOfLifePage() {
    val gameOfLifeManager = remember { GameOfLifeManager() }
    val cells by gameOfLifeManager.aliveCells.collectAsStateWithLifecycle()
    val rawCells = remember(cells) {
        cells.map { cell ->
            Offset(
                x = cell.x * GameOfLifeManager.CELL_SIZE_PX + GameOfLifeManager.CELL_SIZE_PX / 2,
                y = cell.y * GameOfLifeManager.CELL_SIZE_PX + GameOfLifeManager.CELL_SIZE_PX / 2,
            )
        }
    }

    LaunchedEffect(gameOfLifeManager) {
        gameOfLifeManager.loop()
    }

    Box {
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
            drawAliveCells(rawCells)
            drawGrid()
        }

        Button(onClick = gameOfLifeManager::playPause) {
            Text("Start")
        }
    }
}

private fun DrawScope.drawGrid() {
    val columns = (size.width / GameOfLifeManager.CELL_SIZE_PX).fastRoundToInt()
    val rows = (size.height / GameOfLifeManager.CELL_SIZE_PX).fastRoundToInt()

    for (i in 0..columns) {
        drawLine(
            color = Color.Green,
            start = Offset(x = i * GameOfLifeManager.CELL_SIZE_PX, y = 0f),
            end = Offset(x = i * GameOfLifeManager.CELL_SIZE_PX, y = size.height),
            strokeWidth = 1.dp.toPx()
        )
    }

    for (j in 0..rows) {
        drawLine(
            color = Color.Green,
            start = Offset(x = 0f, y = j * GameOfLifeManager.CELL_SIZE_PX),
            end = Offset(x = size.width, y = j * GameOfLifeManager.CELL_SIZE_PX),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawAliveCells(cells: List<Offset>) {
    drawPoints(
        points = cells,
        pointMode = PointMode.Points,
        color = Color.Red,
        strokeWidth = GameOfLifeManager.CELL_SIZE_PX,
        cap = StrokeCap.Square
    )
}

private fun getGridBounds(screenSize: IntSize): Grid {
    val columns = screenSize.width / GameOfLifeManager.CELL_SIZE_PX
    val rows = screenSize.height / GameOfLifeManager.CELL_SIZE_PX

    return columns.fastRoundToInt() to rows.fastRoundToInt()
}