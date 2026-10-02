package com.example.viewmodel

import com.example.model.FurniturePart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UndoRedoManager(private val maxHistory: Int = 30) {
    private val undoStack = mutableListOf<List<FurniturePart>>()
    private val redoStack = mutableListOf<List<FurniturePart>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun reset(initialParts: List<FurniturePart>) {
        undoStack.clear()
        redoStack.clear()
        updateFlags()
    }

    fun pushState(currentState: List<FurniturePart>) {
        undoStack.add(currentState.map { it.copy() })
        if (undoStack.size > maxHistory) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateFlags()
    }

    fun undo(currentState: List<FurniturePart>): List<FurniturePart>? {
        if (undoStack.isEmpty()) return null
        val previousState = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(currentState.map { it.copy() })
        updateFlags()
        return previousState
    }

    fun redo(currentState: List<FurniturePart>): List<FurniturePart>? {
        if (redoStack.isEmpty()) return null
        val nextState = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(currentState.map { it.copy() })
        updateFlags()
        return nextState
    }

    private fun updateFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
}
