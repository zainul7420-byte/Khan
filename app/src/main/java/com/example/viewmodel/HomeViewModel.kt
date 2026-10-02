package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultProjectsIfEmpty()
        }
    }

    val projects: StateFlow<List<FurnitureProject>> = combine(
        repository.allProjects,
        _searchQuery
    ) { projectList, query ->
        if (query.isBlank()) {
            projectList
        } else {
            projectList.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun createNewProject(
        name: String,
        unit: MeasurementUnit = MeasurementUnit.CM,
        defaultThickness: Float = 1.8f,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val newProject = FurnitureProject(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "New project" },
                unit = unit,
                defaultThickness = defaultThickness,
                parts = emptyList()
            )
            repository.saveProject(newProject)
            onCreated(newProject.id)
        }
    }

    fun duplicateProject(project: FurnitureProject) {
        viewModelScope.launch {
            repository.duplicateProject(project)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    class Factory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
