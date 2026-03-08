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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdftools.R
import com.example.pdftools.data.PdfDocument

// AMOLED palette
private val BG = Color(0xFF000000)
private val Surface1 = Color(0xFF111111)
private val Surface2 = Color(0xFF1C1C1C)
private val Outline = Color(0xFF2C2C2C)
private val White = Color(0xFFFFFFFF)
private val WhiteMid = Color(0xFFAAAAAA)
private val WhiteDim = Color(0xFF666666)
private val Accent = Color(0xFFFFFFFF)
private val Danger = Color(0xFFFF4444)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PdfToolsApp(
    uiState: PdfToolsUIState,
    onTabSelected: (PdfTab) -> Unit,
    // Split
    onPickSplitPdf: () -> Unit,
    onSplitAllPages: () -> Unit,
    onSplitCustomRange: () -> Unit,
    onSplitRangeChanged: (String) -> Unit,
    onClearSplitSuccess: () -> Unit,
    // Merge
    onPickMergePdf: () -> Unit,
    onRemoveMergePdf: (Long) -> Unit,
    onClearMergePdfs: () -> Unit,
    onMerge: () -> Unit,
    onClearMergeSuccess: () -> Unit,
    // Files
    onRemoveOutputFile: (Long) -> Unit,
    onOpenOutputFile: (PdfDocument) -> Unit,
    onShareOutputFile: (PdfDocument) -> Unit,
    // Error
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BG)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(BG)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.app_name), color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            HorizontalDivider(color = Outline)

            // Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "tab"
                ) { tab ->
                    when (tab) {
                        PdfTab.SPLIT -> SplitScreen(
                            splitPdf = uiState.splitPdf,
                            rangeText = uiState.splitRangeText,
                            isLoading = uiState.isLoading,
                            successMsg = uiState.splitSuccess,
                            onPickPdf = onPickSplitPdf,
                            onSplitAll = onSplitAllPages,
                            onSplitCustom = onSplitCustomRange,
                            onRangeChanged = onSplitRangeChanged,
                            onDismissSuccess = onClearSplitSuccess
                        )
                        PdfTab.MERGE -> MergeScreen(
                            mergePdfs = uiState.mergePdfs,
                            isLoading = uiState.isLoading,
                            successMsg = uiState.mergeSuccess,
                            onPickPdf = onPickMergePdf,
                            onRemovePdf = onRemoveMergePdf,
                            onClear = onClearMergePdfs,
                            onMerge = onMerge,
                            onDismissSuccess = onClearMergeSuccess
                        )
                        PdfTab.FILES -> FilesScreen(
                            outputFiles = uiState.outputFiles,
                            onRemove = onRemoveOutputFile,
                            onOpen = onOpenOutputFile,
                            onShare = onShareOutputFile
                        )
                    }
                }
            }

            // Bottom nav
            HorizontalDivider(color = Outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BG)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(Icons.Default.ContentCut, stringResource(R.string.tab_split),
                    uiState.currentTab == PdfTab.SPLIT) { onTabSelected(PdfTab.SPLIT) }
                BottomNavItem(Icons.AutoMirrored.Filled.MergeType, stringResource(R.string.tab_merge),
                    uiState.currentTab == PdfTab.MERGE) { onTabSelected(PdfTab.MERGE) }
                BottomNavItem(Icons.Default.Folder, stringResource(R.string.tab_files),
                    uiState.currentTab == PdfTab.FILES) { onTabSelected(PdfTab.FILES) }
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White, strokeWidth = 3.dp)
            }
        }

        // Error banner
        uiState.error?.let { err ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = Surface2,
                contentColor = White,
                action = {
                    TextButton(onClick = onErrorDismissed) { Text("Dismiss", color = White) }
                }
            ) { Text(err) }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) White else WhiteDim,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) White else WhiteDim,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Split Screen ─────────────────────────────────────────────────────────────

@Composable
fun SplitScreen(
    splitPdf: PdfDocument?,
    rangeText: String,
    isLoading: Boolean,
    successMsg: String?,
    onPickPdf: () -> Unit,
    onSplitAll: () -> Unit,
    onSplitCustom: () -> Unit,
    onRangeChanged: (String) -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Text("Split PDF", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Extract pages from a PDF document", color = WhiteDim, fontSize = 14.sp)
        }

        // Selected file card
        item {
            AmoledCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (splitPdf != null) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = White, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(splitPdf.name, color = White, fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp, maxLines = 2)
                            Text(formatSize(splitPdf.sizeInBytes), color = WhiteDim, fontSize = 12.sp)
                        }
                        TextButton(onClick = onPickPdf) {
                            Text("Change", color = WhiteMid, fontSize = 13.sp)
                        }
                    } else {
                        Icon(Icons.Default.FileOpen, null, tint = WhiteDim, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(14.dp))
                        Text("No file selected", color = WhiteDim, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        AmoledButton("Browse", onClick = onPickPdf)
                    }
                }
            }
        }

        // Only show split options when a file is selected
        if (splitPdf != null) {
            item {
                Text("Split Options", color = WhiteMid, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp)
            }
            item {
                AmoledCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Split every page", color = White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Creates one PDF per page", color = WhiteDim, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        AmoledButton("Split All Pages", fullWidth = true, onClick = onSplitAll)
                    }
                }
            }
            item {
                AmoledCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Custom range", color = White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("e.g.  1-3, 5, 8-10", color = WhiteDim, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rangeText,
                            onValueChange = onRangeChanged,
                            placeholder = { Text("1-5, 8, 10-15", color = WhiteDim) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = White,
                                unfocusedTextColor = White,
                                focusedBorderColor = White,
                                unfocusedBorderColor = Outline,
                                cursorColor = White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        AmoledButton("Split Range", fullWidth = true,
                            enabled = rangeText.isNotBlank(), onClick = onSplitCustom)
                    }
                }
            }
        }

        // Success message
        if (successMsg != null) {
            item {
                AmoledCard(borderColor = Color(0xFF2E7D32)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF66BB6A), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(successMsg, color = Color(0xFF66BB6A), modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismissSuccess, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = WhiteDim, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Merge Screen ─────────────────────────────────────────────────────────────

@Composable
fun MergeScreen(
    mergePdfs: List<PdfDocument>,
    isLoading: Boolean,
    successMsg: String?,
    onPickPdf: () -> Unit,
    onRemovePdf: (Long) -> Unit,
    onClear: () -> Unit,
    onMerge: () -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Merge PDFs", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Combine multiple PDFs into one", color = WhiteDim, fontSize = 14.sp)
                }
                if (mergePdfs.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear all", color = WhiteDim, fontSize = 13.sp)
                    }
                }
            }
        }

        // List files already added
        if (mergePdfs.isNotEmpty()) {
            item {
                Text(
                    "${mergePdfs.size} file${if (mergePdfs.size == 1) "" else "s"} added",
                    color = WhiteMid, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp
                )
            }
            items(mergePdfs, key = { it.id }) { pdf ->
                AmoledCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pdf.name, color = White, fontWeight = FontWeight.Medium,
                                fontSize = 14.sp, maxLines = 2)
                            Text(formatSize(pdf.sizeInBytes), color = WhiteDim, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { onRemovePdf(pdf.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, "Remove", tint = WhiteDim, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Add PDF button
        item {
            OutlinedButton(
                onClick = onPickPdf,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
            ) {
                Icon(Icons.Default.Add, null, tint = White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add PDF", color = White)
            }
        }

        // Merge button — only enabled when ≥2 files
        if (mergePdfs.size >= 2) {
            item {
                AmoledButton(
                    text = "Merge ${mergePdfs.size} PDFs",
                    fullWidth = true,
                    onClick = onMerge
                )
            }
        }

        // Success message
        if (successMsg != null) {
            item {
                AmoledCard(borderColor = Color(0xFF2E7D32)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF66BB6A), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(successMsg, color = Color(0xFF66BB6A), modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismissSuccess, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = WhiteDim, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Files Screen ─────────────────────────────────────────────────────────────

@Composable
fun FilesScreen(
    outputFiles: List<PdfDocument>,
    onRemove: (Long) -> Unit,
    onOpen: (PdfDocument) -> Unit,
    onShare: (PdfDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    if (outputFiles.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FolderOpen, null, tint = WhiteDim, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No output files yet", color = WhiteDim, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("Split or merge PDFs to see them here", color = WhiteDim.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                Text("Output Files", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${outputFiles.size} file${if (outputFiles.size == 1) "" else "s"}", color = WhiteDim, fontSize = 14.sp)
            }
            items(outputFiles, key = { it.id }) { pdf ->
                AmoledCard {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pdf.name, color = White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 2)
                                Text(formatSize(pdf.sizeInBytes), color = WhiteDim, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onRemove(pdf.id) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Delete, "Remove", tint = Danger, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onOpen(pdf) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Launch, null, tint = White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open", fontSize = 12.sp, color = White)
                            }
                            OutlinedButton(
                                onClick = { onShare(pdf) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Share, null, tint = White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp, color = White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
fun AmoledCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Outline,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
fun AmoledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = White,
            contentColor = BG,
            disabledContainerColor = Surface2,
            disabledContentColor = WhiteDim
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
