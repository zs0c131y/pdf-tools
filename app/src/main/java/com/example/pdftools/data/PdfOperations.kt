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

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class PdfOperations(private val context: Context) {
    
    init {
        // Initialize PDFBox for Android
        PDFBoxResourceLoader.init(context)
    }
    
    suspend fun splitPdf(
        sourceUri: Uri,
        splitOptions: SplitOptions,
        outputDirectory: File
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = loadPdfDocument(sourceUri)
            
            val outputFiles = when (splitOptions.splitType) {
                SplitType.ALL_PAGES -> {
                    splitAllPages(pdfDocument, outputDirectory)
                }
                SplitType.CUSTOM_RANGE -> {
                    splitCustomRanges(pdfDocument, splitOptions.pageRanges, outputDirectory)
                }
            }
            
            pdfDocument.close()
            Result.success(outputFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun mergePdfs(
        sourceUris: List<Uri>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destination = PDDocument()
            val merger = PDFMergerUtility()
            try {
                for (uri in sourceUris) {
                    val stream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open URI: $uri")
                    val src = PDDocument.load(stream)
                    merger.appendDocument(destination, src)
                    src.close()
                    stream.close()
                }
                destination.save(outputFile)
            } finally {
                destination.close()
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPdfPageCount(uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = loadPdfDocument(uri)
            val pageCount = pdfDocument.numberOfPages
            pdfDocument.close()
            pageCount
        } catch (e: Exception) {
            0
        }
    }
    
    private fun loadPdfDocument(uri: Uri): PDDocument {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return PDDocument.load(inputStream)
    }
    
    private fun splitAllPages(document: PDDocument, outputDirectory: File): List<File> {
        val splitter = Splitter()
        splitter.setSplitAtPage(1)
        
        val pages = splitter.split(document)
        val outputFiles = mutableListOf<File>()
        
        pages.forEachIndexed { index, page ->
            val outputFile = File(outputDirectory, "page_${index + 1}.pdf")
            page.save(outputFile)
            page.close()
            outputFiles.add(outputFile)
        }
        
        return outputFiles
    }
    
    private fun splitCustomRanges(
        document: PDDocument,
        ranges: List<IntRange>,
        outputDirectory: File
    ): List<File> {
        val outputFiles = mutableListOf<File>()
        
        ranges.forEachIndexed { index, range ->
            val newDocument = PDDocument()
            
            for (pageNum in range) {
                if (pageNum <= document.numberOfPages) {
                    val page = document.getPage(pageNum - 1)
                    newDocument.addPage(page)
                }
            }
            
            val outputFile = File(outputDirectory, "split_${index + 1}.pdf")
            newDocument.save(outputFile)
            newDocument.close()
            outputFiles.add(outputFile)
        }
        
        return outputFiles
    }
}
