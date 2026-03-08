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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pdftools.data.PdfDocument
import com.example.pdftools.ui.theme.PdfToolsTheme
import java.io.File
import java.net.URI

class MainActivity : ComponentActivity() {

    private val viewModel: PdfToolsViewModel by viewModels()

    // For Split — pick one PDF
    private val pickSplitPdf = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSplitPdf(createPdfDocumentFromUri(it)) }
    }

    // For Merge — pick one PDF at a time and accumulate
    private val pickMergePdf = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addMergePdf(createPdfDocumentFromUri(it)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PdfToolsTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PdfToolsApp(
                    uiState = uiState,
                    onTabSelected = viewModel::selectTab,
                    // Split
                    onPickSplitPdf = { pickSplitPdf.launch("application/pdf") },
                    onSplitAllPages = { viewModel.splitAllPages(this) },
                    onSplitCustomRange = { viewModel.splitCustomRange(this) },
                    onSplitRangeChanged = viewModel::setSplitRangeText,
                    onClearSplitSuccess = viewModel::clearSplitSuccess,
                    // Merge
                    onPickMergePdf = { pickMergePdf.launch("application/pdf") },
                    onRemoveMergePdf = viewModel::removeMergePdf,
                    onClearMergePdfs = viewModel::clearMergePdfs,
                    onMerge = { viewModel.mergePdfs(this) },
                    onClearMergeSuccess = viewModel::clearMergeSuccess,
                    // Files
                    onRemoveOutputFile = viewModel::removeOutputFile,
                    onOpenOutputFile = ::openFile,
                    onShareOutputFile = ::shareFile,
                    // Error
                    onErrorDismissed = viewModel::clearError
                )
            }
        }
    }

    private fun createPdfDocumentFromUri(uri: Uri): PdfDocument {
        var name = "Unknown.pdf"
        var size = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx != -1) name = cursor.getString(nameIdx)
                if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
            }
        }
        return PdfDocument(
            id = System.currentTimeMillis(),
            name = name,
            uriString = uri.toString(),
            sizeInBytes = size,
            pageCount = 0,
            lastModified = System.currentTimeMillis()
        )
    }

    private fun fileProviderUri(doc: PdfDocument): Uri? {
        return try {
            val file = File(URI(doc.uriString))
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) { null }
    }

    private fun openFile(doc: PdfDocument) {
        val uri = fileProviderUri(doc) ?: return
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }

    private fun shareFile(doc: PdfDocument) {
        val uri = fileProviderUri(doc) ?: return
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share PDF"
            )
        )
    }
}

