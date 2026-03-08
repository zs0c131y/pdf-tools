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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsApp(
    pdfToolsUIState: PdfToolsUIState,
    onTabSelected: (PdfTab) -> Unit,
    onPdfSelected: (Long) -> Unit,
    onPdfAdded: (PdfDocument) -> Unit,
    onPdfRemoved: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { },
                    label = { Text(stringResource(R.string.tab_split)) },
                    selected = pdfToolsUIState.currentTab == PdfTab.SPLIT,
                    onClick = { onTabSelected(PdfTab.SPLIT) }
                )
                NavigationBarItem(
                    icon = { },
                    label = { Text(stringResource(R.string.tab_merge)) },
                    selected = pdfToolsUIState.currentTab == PdfTab.MERGE,
                    onClick = { onTabSelected(PdfTab.MERGE) }
                )
                NavigationBarItem(
                    icon = { },
                    label = { Text(stringResource(R.string.tab_files)) },
                    selected = pdfToolsUIState.currentTab == PdfTab.FILES,
                    onClick = { onTabSelected(PdfTab.FILES) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pdfToolsUIState.currentTab) {
                PdfTab.SPLIT -> SplitScreen(
                    pdfs = pdfToolsUIState.pdfs,
                    onPdfAdded = onPdfAdded
                )
                PdfTab.MERGE -> MergeScreen(
                    pdfs = pdfToolsUIState.pdfs,
                    selectedPdfs = pdfToolsUIState.selectedPdfs,
                    onPdfSelected = onPdfSelected,
                    onPdfAdded = onPdfAdded,
                    onClearSelection = onClearSelection
                )
                PdfTab.FILES -> FilesScreen(
                    pdfs = pdfToolsUIState.pdfs,
                    onPdfRemoved = onPdfRemoved
                )
            }
            
            // Loading indicator
            if (pdfToolsUIState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Error snackbar
            pdfToolsUIState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = onErrorDismissed) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun SplitScreen(
    pdfs: List<PdfDocument>,
    onPdfAdded: (PdfDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.split_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.split_description),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Implement PDF selection */ }
        ) {
            Text(stringResource(R.string.select_pdf))
        }
    }
}

@Composable
fun MergeScreen(
    pdfs: List<PdfDocument>,
    selectedPdfs: List<PdfDocument>,
    onPdfSelected: (Long) -> Unit,
    onPdfAdded: (PdfDocument) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.merge_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.merge_description),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (selectedPdfs.isNotEmpty()) {
            Text(
                text = "${selectedPdfs.size} PDFs selected",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* TODO: Implement merge */ }
                ) {
                    Text(stringResource(R.string.merge_pdfs))
                }
                OutlinedButton(onClick = onClearSelection) {
                    Text(stringResource(R.string.cancel))
                }
            }
        } else {
            Button(
                onClick = { /* TODO: Implement PDF selection */ }
            ) {
                Text(stringResource(R.string.add_pdf))
            }
        }
    }
}

@Composable
fun FilesScreen(
    pdfs: List<PdfDocument>,
    onPdfRemoved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (pdfs.isEmpty()) Arrangement.Center else Arrangement.Top
    ) {
        if (pdfs.isEmpty()) {
            Text(
                text = stringResource(R.string.no_pdfs_found),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = stringResource(R.string.pdf_files),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            // TODO: Add list of PDF files
            Text("${pdfs.size} files", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
