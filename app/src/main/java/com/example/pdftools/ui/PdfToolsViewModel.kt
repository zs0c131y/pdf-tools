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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.PdfDocument
import com.example.pdftools.data.PdfOperations
import com.example.pdftools.data.SplitOptions
import com.example.pdftools.data.SplitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PdfToolsUIState(
    // Split tab state
    val splitPdf: PdfDocument? = null,
    val splitRangeText: String = "",
    val splitSuccess: String? = null,
    // Merge tab state — ordered list of PDFs to merge
    val mergePdfs: List<PdfDocument> = emptyList(),
    val mergeSuccess: String? = null,
    // Files tab — all processed output files
    val outputFiles: List<PdfDocument> = emptyList(),
    // Global
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: PdfTab = PdfTab.SPLIT
)

enum class PdfTab { SPLIT, MERGE, FILES }

class PdfToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PdfToolsUIState())
    val uiState: StateFlow<PdfToolsUIState> = _uiState

    fun selectTab(tab: PdfTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    // ── Split ──────────────────────────────────────────────────────────────

    fun setSplitPdf(pdf: PdfDocument) {
        _uiState.value = _uiState.value.copy(splitPdf = pdf, splitSuccess = null)
    }

    fun setSplitRangeText(text: String) {
        _uiState.value = _uiState.value.copy(splitRangeText = text)
    }

    fun splitAllPages(context: Context) {
        val pdf = _uiState.value.splitPdf ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val ops = PdfOperations(context)
            val outDir = File(context.filesDir, "split").apply { mkdirs() }
            val result = ops.splitPdf(pdf.uri, SplitOptions(SplitType.ALL_PAGES), outDir)
            result.fold(
                onSuccess = { files ->
                    val newDocs = files.mapIndexed { i, f ->
                        PdfDocument(
                            id = System.currentTimeMillis() + i,
                            name = f.name,
                            uriString = f.toURI().toString(),
                            sizeInBytes = f.length(),
                            pageCount = 1,
                            lastModified = f.lastModified()
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        outputFiles = _uiState.value.outputFiles + newDocs,
                        splitSuccess = "Split into ${files.size} pages"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Split failed: ${e.message}"
                    )
                }
            )
        }
    }

    fun splitCustomRange(context: Context) {
        val pdf = _uiState.value.splitPdf ?: return
        val ranges = parseRanges(_uiState.value.splitRangeText)
        if (ranges.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid page range")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val ops = PdfOperations(context)
            val outDir = File(context.filesDir, "split").apply { mkdirs() }
            val result = ops.splitPdf(pdf.uri, SplitOptions(SplitType.CUSTOM_RANGE, ranges), outDir)
            result.fold(
                onSuccess = { files ->
                    val newDocs = files.mapIndexed { i, f ->
                        PdfDocument(
                            id = System.currentTimeMillis() + i,
                            name = f.name,
                            uriString = f.toURI().toString(),
                            sizeInBytes = f.length(),
                            pageCount = ranges.getOrElse(i) { 1..1 }.count(),
                            lastModified = f.lastModified()
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        outputFiles = _uiState.value.outputFiles + newDocs,
                        splitSuccess = "Split into ${files.size} parts"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Split failed: ${e.message}"
                    )
                }
            )
        }
    }

    fun clearSplitSuccess() {
        _uiState.value = _uiState.value.copy(splitSuccess = null)
    }

    // ── Merge ─────────────────────────────────────────────────────────────

    fun addMergePdf(pdf: PdfDocument) {
        val updated = _uiState.value.mergePdfs + pdf
        _uiState.value = _uiState.value.copy(mergePdfs = updated, mergeSuccess = null)
    }

    fun removeMergePdf(id: Long) {
        val updated = _uiState.value.mergePdfs.filter { it.id != id }
        _uiState.value = _uiState.value.copy(mergePdfs = updated)
    }

    fun clearMergePdfs() {
        _uiState.value = _uiState.value.copy(mergePdfs = emptyList(), mergeSuccess = null)
    }

    fun mergePdfs(context: Context) {
        val pdfs = _uiState.value.mergePdfs
        if (pdfs.size < 2) {
            _uiState.value = _uiState.value.copy(error = "Select at least 2 PDFs to merge")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val ops = PdfOperations(context)
            val outDir = File(context.filesDir, "merged").apply { mkdirs() }
            val outFile = File(outDir, "merged_${System.currentTimeMillis()}.pdf")
            val result = ops.mergePdfs(pdfs.map { it.uri }, outFile)
            result.fold(
                onSuccess = { file ->
                    val newDoc = PdfDocument(
                        id = System.currentTimeMillis(),
                        name = file.name,
                        uriString = file.toURI().toString(),
                        sizeInBytes = file.length(),
                        pageCount = pdfs.sumOf { it.pageCount },
                        lastModified = file.lastModified()
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mergePdfs = emptyList(),
                        outputFiles = _uiState.value.outputFiles + newDoc,
                        mergeSuccess = "Merged into ${file.name}"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Merge failed: ${e.message}"
                    )
                }
            )
        }
    }

    fun clearMergeSuccess() {
        _uiState.value = _uiState.value.copy(mergeSuccess = null)
    }

    // ── Files ─────────────────────────────────────────────────────────────

    fun removeOutputFile(id: Long) {
        _uiState.value = _uiState.value.copy(
            outputFiles = _uiState.value.outputFiles.filter { it.id != id }
        )
    }

    // ── Misc ──────────────────────────────────────────────────────────────

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun parseRanges(input: String): List<IntRange> {
        return try {
            input.split(",").mapNotNull { part ->
                val trimmed = part.trim()
                when {
                    trimmed.contains("-") -> {
                        val (a, b) = trimmed.split("-").map { it.trim().toInt() }
                        a..b
                    }
                    trimmed.isNotEmpty() -> {
                        val n = trimmed.toInt()
                        n..n
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
