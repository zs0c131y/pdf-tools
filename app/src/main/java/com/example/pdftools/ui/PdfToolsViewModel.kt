/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.pdftools.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.PdfDocument
import com.example.pdftools.data.PdfRepository
import com.example.pdftools.data.PdfRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class PdfToolsUIState(
    val pdfs: List<PdfDocument> = emptyList(),
    val selectedPdfs: List<PdfDocument> = emptyList(),
    val currentPdfId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: PdfTab = PdfTab.SPLIT
)

enum class PdfTab {
    SPLIT,
    MERGE,
    FILES
}

class PdfToolsViewModel(
    private val pdfRepository: PdfRepository = PdfRepositoryImpl()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PdfToolsUIState())
    val uiState: StateFlow<PdfToolsUIState> = _uiState
    
    init {
        loadPdfs()
    }
    
    private fun loadPdfs() {
        viewModelScope.launch {
            pdfRepository.getAllPdfs()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
                .collect { pdfs ->
                    _uiState.value = _uiState.value.copy(
                        pdfs = pdfs,
                        isLoading = false
                    )
                }
        }
    }
    
    fun selectTab(tab: PdfTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }
    
    fun togglePdfSelection(pdfId: Long) {
        viewModelScope.launch {
            val currentPdfs = _uiState.value.pdfs
            val updatedPdfs = currentPdfs.map { pdf ->
                if (pdf.id == pdfId) {
                    pdf.copy(isSelected = !pdf.isSelected)
                } else {
                    pdf
                }
            }
            
            val selectedPdfs = updatedPdfs.filter { it.isSelected }
            _uiState.value = _uiState.value.copy(
                selectedPdfs = selectedPdfs
            )
        }
    }
    
    fun addPdf(pdf: PdfDocument) {
        viewModelScope.launch {
            pdfRepository.addPdf(pdf)
        }
    }
    
    fun removePdf(pdfId: Long) {
        viewModelScope.launch {
            pdfRepository.removePdf(pdfId)
        }
    }
    
    fun clearSelection() {
        viewModelScope.launch {
            val currentPdfs = _uiState.value.pdfs
            val updatedPdfs = currentPdfs.map { pdf ->
                pdf.copy(isSelected = false)
            }
            _uiState.value = _uiState.value.copy(
                selectedPdfs = emptyList()
            )
        }
    }
    
    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
    
    fun setError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
