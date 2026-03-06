package com.cdodi.data.gameoflife

fun interface EvolutionEngine {
    fun evaluateNextGeneration(currentGeneration: Set<Cell>, rules: Collection<GameRule>): Set<Cell>
}
