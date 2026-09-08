# ExpenseTrackerApp 📊

A clean, modern, and intuitive Android Expense Tracker Application built using Kotlin and Jetpack libraries. This application helps users track their daily income and expenses, offering a visual summary of their total balance, total income, and total expenses.

---

## 🚀 Features

- **Dashboard / Summary Cards:** View real-time totals for:
  - 💰 **Total Balance** (Income - Expense)
  - 📈 **Total Income**
  - 📉 **Total Expenses**
- **Transaction History:** A scrollable list displaying all recorded transactions with icons indicating income vs. expense status.
- **Add Transactions:** A separate form to easily log Amount, Category, Date, Note, and Transaction Type.
- **Buy Now, Pay Later (BNPL) Splitter:** Split payments into monthly installments. The app automatically creates future-dated transactions representing each installment over the selected duration.
- **On-Device Receipt Scanner (Offline OCR):** Instantly parse prices from paper receipts using the device's camera and Google ML Kit Text Recognition, without sending any data to external servers.
- **Swipe-to-Delete:** Quickly delete transactions from the list using horizontal swipe gestures.
- **Financial Insights & Analytics:** View spending trends over time with customizable date ranges and generate/export PDF reports.
- **Settings & Currency Support:** Customize app settings, manage currency preferences, and toggle app features like OCR mode, reminders, and biometric lock.
- **Data Persistence:** Offline capability powered by a local Room database.

---

## 🎨 Branding & UI Specifications

- **Branding Logo:** A brown leather wallet with a green credit card sticking out and a green checkmark badge in the bottom-right corner.
- **Design Aesthetic:** Clean White / Light background with a modern, spacious fintech visual hierarchy.
- **Color Tokens:**
  - Primary Action Blue : `#2563EB`
  - Primary Blue Dark   : `#1D4ED8`
  - Inflow / Income     : `#16A34A` (Green)
  - Outflow / Expense   : `#EF4444` (Red)
  - BNPL Installments   : `#F59E0B` (Amber)
  - Insights / Reports  : `#7C3AED` (Purple)

---

## 🛠️ Technology Stack & Architecture

This project is built using the **MVVM (Model-View-ViewModel)** architectural pattern to ensure clean separation of concerns and maintainability.

- **Language:** Kotlin
- **Local Database:** Room Database for local data storage and caching.
- **Asynchronous Processing:** Kotlin Coroutines to handle database operations off the main thread.
- **Offline OCR:** Google ML Kit Text Recognition API (`com.google.mlkit:text-recognition`) for on-device document text extraction.
- **Camera Integration:** Modern Android Jetpack Activity Result API (`ActivityResultContracts.TakePicturePreview`) for lightweight snapshot capture.
- **Architecture Components:**
  - `LiveData` to observe real-time database changes.
  - `ViewModel` to store and manage UI-related data in a lifecycle-conscious way.
  - `Repository` to abstract access to multiple data sources.
- **UI Components:**
  - `RecyclerView` with `ListAdapter` and `DiffUtil` for smooth, optimized list rendering.
  - `FloatingActionButton` (FAB) to launch the transaction creation form.
  - Material Design components (`TextInputEditText`, custom cards, radio buttons).

---

## 📂 Project Structure

Here is a breakdown of the key source files:

- [`MainActivity.kt`](app/src/main/java/com/example/expensetrackerapp/MainActivity.kt) - The main dashboard exhibiting total balances, transaction lists, and swipe-to-delete operations.
- [`AddTransactionActivity.kt`](app/src/main/java/com/example/expensetrackerapp/AddTransactionActivity.kt) - Form handling logic for entering, splitting, scanning, and persisting transactions.
- [`TransactionsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionsActivity.kt) - Displays a detailed, comprehensive view of all transactions.
- [`InsightsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/InsightsActivity.kt) - Provides analytics, visual trends, and report generation functionality.
- [`SettingsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/SettingsActivity.kt) - Manages user preferences and currency formatting options.
- [`BnplSplitterActivity.kt`](app/src/main/java/com/example/expensetrackerapp/BnplSplitterActivity.kt) - Contains the logic for calculating and generating Buy Now, Pay Later installment plans.
- [`OcrScannerDialog.kt`](app/src/main/java/com/example/expensetrackerapp/OcrScannerDialog.kt) - Handles ML Kit text recognition integration for parsing receipts.
- [`Transaction.kt`](app/src/main/java/com/example/expensetrackerapp/Transaction.kt) - The Room Entity representing a database table schema.
- [`TransactionDao.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionDao.kt) - The Data Access Object mapping Kotlin functions (including batch insertion) to SQLite queries.
- [`AppDatabase.kt`](app/src/main/java/com/example/expensetrackerapp/AppDatabase.kt) - The Room Database class providing the main database entry point.
- [`TransactionRepository.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionRepository.kt) - Manages data access operations and decouples the ViewModel from direct database queries.
- [`TransactionViewModel.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionViewModel.kt) - Exposes UI-ready data and maps interactions back to the database.
- [`TransactionAdapter.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionAdapter.kt) - Handles populating and updating transaction items in the `RecyclerView`.

---

## 🚀 How to Set Up & Run the App

1. **Clone the Repository:**
   ```bash
   git clone <repository_url>
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Click **Open** and select the project directory: `Assignment_01_IT22136374`.
3. **Gradle Sync:**
   - Let Gradle sync download all dependencies.
4. **Build and Run:**
   - Connect an Android device with Developer Mode / USB Debugging enabled, or set up an Android Virtual Device (AVD) emulator.
   - Click the green **Run (Play)** button in Android Studio to build and install the app.
