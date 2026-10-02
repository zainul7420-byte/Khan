package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.FurniturePart
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit
import com.example.model.PartType
import com.example.model.WoodMaterial
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorTab(val label: String) {
    ASSEMBLY("Assembly"),
    PARTS_LIST("Parts list"),
    NOTES("Notes"),
    AR_VISION("AR Vision")
}

class EditorViewModel(
    private val projectId: String,
    private val repository: ProjectRepository
) : ViewModel() {

    private val _project = MutableStateFlow<FurnitureProject?>(null)
    val project: StateFlow<FurnitureProject?> = _project.asStateFlow()

    private val _selectedPartId = MutableStateFlow<String?>(null)
    val selectedPartId: StateFlow<String?> = _selectedPartId.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.ASSEMBLY)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _showAddPartSheet = MutableStateFlow(false)
    val showAddPartSheet: StateFlow<Boolean> = _showAddPartSheet.asStateFlow()

    private val _isSnapToGrid = MutableStateFlow(true)
    val isSnapToGrid: StateFlow<Boolean> = _isSnapToGrid.asStateFlow()

    val undoRedoManager = UndoRedoManager()
    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getProject(projectId).collect { loadedProject ->
                if (loadedProject != null && _project.value == null) {
                    _project.value = loadedProject
                    undoRedoManager.reset(loadedProject.parts)
                    // Select first part if available
                    if (loadedProject.parts.isNotEmpty()) {
                        _selectedPartId.value = loadedProject.parts.first().id
                    }
                }
            }
        }
    }

    val selectedPart: FurniturePart?
        get() {
            val id = _selectedPartId.value ?: return null
            return _project.value?.parts?.find { it.id == id }
        }

    fun setActiveTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun toggleSnapToGrid() {
        _isSnapToGrid.value = !_isSnapToGrid.value
    }

    fun setShowAddPartSheet(show: Boolean) {
        _showAddPartSheet.value = show
    }

    fun selectPart(partId: String?) {
        _selectedPartId.value = partId
    }

    fun updateProjectName(newName: String) {
        val current = _project.value ?: return
        if (newName.isBlank() || current.name == newName) return
        _project.value = current.copy(name = newName)
        scheduleSave()
    }

    fun changeUnit(newUnit: MeasurementUnit) {
        val current = _project.value ?: return
        if (current.unit == newUnit) return
        _project.value = current.copy(unit = newUnit)
        scheduleSave()
    }

    fun updateNotes(notes: String) {
        val current = _project.value ?: return
        _project.value = current.copy(notes = notes)
        scheduleSave()
    }

    // --- PART MODIFICATIONS WITH UNDO/REDO ---

    fun addPart(
        type: PartType,
        customWidth: Float? = null,
        customHeight: Float? = null,
        customThickness: Float? = null,
        customMaterial: WoodMaterial? = null
    ) {
        val current = _project.value ?: return
        undoRedoManager.pushState(current.parts)

        val w = customWidth ?: type.defaultWidth
        val h = customHeight ?: type.defaultHeight
        val t = customThickness ?: current.defaultThickness
        val mat = customMaterial ?: WoodMaterial.LIGHT_OAK

        // Place on ground or slightly staggered
        val existingCount = current.parts.size
        val posY = if (type == PartType.SHELF || type == PartType.BOARD) (h * 0.5f) else (h * 0.5f)
        val posX = (existingCount % 4 - 1.5f) * 10f

        val newPart = FurniturePart(
            id = UUID.randomUUID().toString(),
            name = "${type.title} ${existingCount + 1}",
            type = type,
            width = w,
            height = h,
            thickness = t,
            positionX = posX,
            positionY = posY,
            positionZ = 0f,
            material = mat
        )

        val updatedParts = current.parts + newPart
        _project.value = current.copy(parts = updatedParts)
        _selectedPartId.value = newPart.id
        _showAddPartSheet.value = false
        scheduleSave()
    }

    fun updateSelectedPartDimensions(
        width: Float? = null,
        height: Float? = null,
        thickness: Float? = null
    ) {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val newW = (width ?: currentSelected.width).coerceAtLeast(0.5f)
        val newH = (height ?: currentSelected.height).coerceAtLeast(0.5f)
        val newT = (thickness ?: currentSelected.thickness).coerceAtLeast(0.2f)

        val updatedPart = currentSelected.copy(
            width = newW,
            height = newH,
            thickness = newT
        )

        updatePartInProject(updatedPart)
    }

    fun updateSelectedPartPosition(
        x: Float? = null,
        y: Float? = null,
        z: Float? = null
    ) {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val snap = if (_isSnapToGrid.value) 1.0f else 0.1f // snap to nearest cm if enabled
        val rawX = x ?: currentSelected.positionX
        val rawY = y ?: currentSelected.positionY
        val rawZ = z ?: currentSelected.positionZ

        val finalX = if (_isSnapToGrid.value) Math.round(rawX / snap) * snap else rawX
        val finalY = if (_isSnapToGrid.value) Math.round(rawY / snap) * snap else rawY
        val finalZ = if (_isSnapToGrid.value) Math.round(rawZ / snap) * snap else rawZ

        val updatedPart = currentSelected.copy(
            positionX = finalX,
            positionY = finalY,
            positionZ = finalZ
        )

        updatePartInProject(updatedPart)
    }

    fun updateSelectedPartRotation(
        rx: Float? = null,
        ry: Float? = null,
        rz: Float? = null
    ) {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val updatedPart = currentSelected.copy(
            rotationX = (rx ?: currentSelected.rotationX) % 360f,
            rotationY = (ry ?: currentSelected.rotationY) % 360f,
            rotationZ = (rz ?: currentSelected.rotationZ) % 360f
        )

        updatePartInProject(updatedPart)
    }

    fun updateSelectedPartMaterial(material: WoodMaterial, customColor: Long? = null) {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val updatedPart = currentSelected.copy(
            material = material,
            customColor = customColor
        )

        updatePartInProject(updatedPart)
    }

    fun duplicateSelectedPart() {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val duplicated = currentSelected.copy(
            id = UUID.randomUUID().toString(),
            name = "${currentSelected.name} (Copy)",
            positionX = currentSelected.positionX + 5f,
            positionZ = currentSelected.positionZ + 5f
        )

        val updatedParts = current.parts + duplicated
        _project.value = current.copy(parts = updatedParts)
        _selectedPartId.value = duplicated.id
        scheduleSave()
    }

    fun deleteSelectedPart() {
        val current = _project.value ?: return
        val currentSelected = selectedPart ?: return
        undoRedoManager.pushState(current.parts)

        val updatedParts = current.parts.filter { it.id != currentSelected.id }
        _project.value = current.copy(parts = updatedParts)
        _selectedPartId.value = updatedParts.lastOrNull()?.id
        scheduleSave()
    }

    fun undo() {
        val current = _project.value ?: return
        val previousParts = undoRedoManager.undo(current.parts) ?: return
        _project.value = current.copy(parts = previousParts)
        if (_selectedPartId.value != null && previousParts.none { it.id == _selectedPartId.value }) {
            _selectedPartId.value = previousParts.lastOrNull()?.id
        }
        scheduleSave()
    }

    fun redo() {
        val current = _project.value ?: return
        val nextParts = undoRedoManager.redo(current.parts) ?: return
        _project.value = current.copy(parts = nextParts)
        if (_selectedPartId.value != null && nextParts.none { it.id == _selectedPartId.value }) {
            _selectedPartId.value = nextParts.lastOrNull()?.id
        }
        scheduleSave()
    }

    private fun updatePartInProject(updatedPart: FurniturePart) {
        val current = _project.value ?: return
        val updatedParts = current.parts.map { if (it.id == updatedPart.id) updatedPart else it }
        _project.value = current.copy(parts = updatedParts)
        scheduleSave()
    }

    private fun scheduleSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(300)
            _project.value?.let { proj ->
                repository.saveProject(proj)
            }
        }
    }

    class Factory(
        private val projectId: String,
        private val repository: ProjectRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(projectId, repository) as T
        }
    }
}
