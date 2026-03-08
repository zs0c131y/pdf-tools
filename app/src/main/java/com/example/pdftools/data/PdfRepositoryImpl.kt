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

package com.example.pdftools.data

import com.example.pdftools.data.local.LocalPdfDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class PdfRepositoryImpl : PdfRepository {
    
    private val pdfsFlow = MutableStateFlow(LocalPdfDataProvider.allPdfs)
    
    override fun getAllPdfs(): Flow<List<PdfDocument>> = pdfsFlow
    
    override fun getPdfById(id: Long): Flow<PdfDocument?> = pdfsFlow.map { pdfs ->
        pdfs.find { it.id == id }
    }
    
    override suspend fun addPdf(pdf: PdfDocument) {
        val currentPdfs = pdfsFlow.value.toMutableList()
        currentPdfs.add(pdf)
        pdfsFlow.value = currentPdfs
    }
    
    override suspend fun removePdf(id: Long) {
        val currentPdfs = pdfsFlow.value.toMutableList()
        currentPdfs.removeAll { it.id == id }
        pdfsFlow.value = currentPdfs
    }
    
    override suspend fun updatePdf(pdf: PdfDocument) {
        val currentPdfs = pdfsFlow.value.toMutableList()
        val index = currentPdfs.indexOfFirst { it.id == pdf.id }
        if (index != -1) {
            currentPdfs[index] = pdf
            pdfsFlow.value = currentPdfs
        }
    }
}
