# PDF Split & Merge App

This is an Android application for splitting and merging PDF files, built with [Jetpack Compose][compose] and Material 3 design.

To try out this app, use the latest stable version
of [Android Studio](https://developer.android.com/studio).

## Features

The PDF Tools app provides the following functionality:

### Split PDFs
- Select a PDF file from your device
- Split into individual pages
- Split by custom page ranges
- Save split PDFs to device storage

### Merge PDFs
- Select multiple PDF files
- Reorder files before merging
- Merge into a single PDF document
- Save merged PDF to device storage

### File Management
- View all processed PDF files
- Delete unwanted PDFs
- Share PDFs with other apps
- File metadata display (size, page count, modification date)

## Technology Stack

- **UI Framework**: Jetpack Compose with Material 3
- **Programming Language**: Kotlin
- **Architecture**: MVVM with StateFlow
- **PDF Library**: PDFBox Android (tom-roush)
- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 33 (Android 13)

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE` - To read PDF files from device storage
- `WRITE_EXTERNAL_STORAGE` - To save processed PDF files (Android 12 and below)
- `READ_MEDIA_IMAGES/VIDEO/AUDIO` - For media access on Android 13+

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/pdftools/
│   │   ├── data/
│   │   │   ├── PdfDocument.kt - Data models
│   │   │   ├── PdfRepository.kt - Data repository interface
│   │   │   ├── PdfRepositoryImpl.kt - Repository implementation
│   │   │   ├── PdfOperations.kt - PDF split/merge operations
│   │   │   └── local/
│   │   │       └── LocalPdfDataProvider.kt - Local data provider
│   │   └── ui/
│   │       ├── MainActivity.kt - Main activity
│   │       ├── PdfToolsApp.kt - Main composable UI
│   │       ├── PdfToolsViewModel.kt - ViewModel
│   │       └── theme/ - Material 3 theme configuration
│   ├── res/ - Resources (strings, drawables, etc.)
│   └── AndroidManifest.xml
```

## Building the App

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

## Dependencies

Key dependencies used in this project:
- Jetpack Compose BOM 2026.02.00
- Material 3 Components
- PDFBox Android 2.0.27.0
- Kotlin Coroutines
- AndroidX Lifecycle & ViewModel

## License

```
Copyright 2022 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[compose]: https://developer.android.com/jetpack/compose

