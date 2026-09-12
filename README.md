# ExpenseTrackerApp 📊

A clean, modern, and intuitive Android Expense Tracker Application built using Kotlin and Jetpack libraries. This application helps users track their daily income and expenses, offering a visual summary of their total balance, total income, and total expenses.

---

## 🚀 Working Features

- **Dashboard / Summary Cards:** View real-time totals for:
  - 💰 **Total Balance** (Income - Expense)
  - 📈 **Total Income**
  - 📉 **Total Expenses**
- **Transaction History:** A scrollable list displaying all recorded transactions with icons indicating income vs. expense status, and a live search input.
- **Add Transactions:** A separate form to easily log Amount, Category, Date, Note, and Transaction Type.
- **Buy Now, Pay Later (BNPL) Splitter:** Split payments into equal monthly installments. The app automatically creates future-dated transactions representing each installment over the selected duration.
- **Background Push Notifications:** Automated background tasks powered by Android `WorkManager` that wake up once a day to check your database and notify you if any BNPL installments are due, even when the app is fully closed.
- **On-Device Receipt Scanner (Offline OCR):** Instantly parse prices from paper receipts using the device's camera and Google ML Kit Text Recognition, without sending any data to external servers.
- **Swipe-to-Delete:** Quickly delete transactions from the list using horizontal swipe gestures.
- **Financial Insights & Analytics:** View spending trends over time with customizable date ranges (Last 7 Days / Last 30 Days), visual category breakdowns, and the ability to generate/export PDF statements.
- **Settings & Currency Support:** Manage currency preferences (LKR/USD), toggle app themes (Light/Dark), enable daily background reminders, and manage the database.
- **Material 3 & Dynamic Theming:** Uses native Android Material You guidelines. The app will adapt its colors and tones natively to match the user's system wallpaper, offering a high-end, premium native look alongside the custom "Outfit" typography.
- **Data Persistence:** Offline capability powered by a local Room database.

---

## 🎨 Branding & UI Specifications

- **Branding Logo:** A brown leather wallet with a green credit card sticking out and a green checkmark badge in the bottom-right corner.
- **Design Aesthetic:** Clean White / Light background with a modern, spacious fintech visual hierarchy.
- **Color Tokens:**
  - Primary Action Blue : `#2563EB`
  - Primary Blue Dark : `#1D4ED8`
  - Inflow / Income : `#16A34A` (Green)
  - Outflow / Expense : `#EF4444` (Red)
  - BNPL Installments : `#F59E0B` (Amber)
  - Insights / Reports : `#7C3AED` (Purple)

---

## 🛠️ Technology Stack & Architecture

This project is built using the **MVVM (Model-View-ViewModel)** architectural pattern to ensure clean separation of concerns and maintainability.

- **Language:** Kotlin
- **Dependency Injection:** Dagger Hilt (`com.google.dagger:hilt-android`) for clean, modular, and testable dependency management across the app.
- **Local Database:** Room Database for local data storage and caching.
- **Background Processing:** WorkManager with Hilt-Work integration to reliably schedule and execute background push notifications.
- **Asynchronous Processing:** Kotlin Coroutines to handle database operations off the main thread.
- **Offline OCR:** Google ML Kit Text Recognition API (`com.google.mlkit:text-recognition`) for on-device document text extraction.
- **Architecture Components:**
  - `LiveData` to observe real-time database changes.
  - `ViewModel` to store and manage UI-related data in a lifecycle-conscious way.
  - `Repository` to abstract access to multiple data sources.
- **UI Components:**
  - `RecyclerView` with `ListAdapter` and `DiffUtil` for smooth, optimized list rendering.
  - Material Design components.

---

## 📂 Project Structure

Here is a breakdown of the key source files:

- [`MainActivity.kt`](app/src/main/java/com/example/expensetrackerapp/MainActivity.kt) - The main dashboard exhibiting total balances, transaction lists, and swipe-to-delete operations.
- [`AddTransactionActivity.kt`](app/src/main/java/com/example/expensetrackerapp/AddTransactionActivity.kt) - Form handling logic for entering, splitting, scanning, and persisting transactions.
- [`TransactionsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionsActivity.kt) - Displays a detailed, comprehensive view of all transactions.
- [`InsightsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/InsightsActivity.kt) - Provides analytics, visual trends, and report generation functionality.
- [`SettingsActivity.kt`](app/src/main/java/com/example/expensetrackerapp/SettingsActivity.kt) - Manages user preferences, currency formatting, and themes.
- [`BnplSplitterActivity.kt`](app/src/main/java/com/example/expensetrackerapp/BnplSplitterActivity.kt) - Contains the logic for calculating and generating Buy Now, Pay Later installment plans.
- [`OcrScannerDialog.kt`](app/src/main/java/com/example/expensetrackerapp/OcrScannerDialog.kt) - Handles ML Kit text recognition integration for parsing receipts.
- [`Transaction.kt`](app/src/main/java/com/example/expensetrackerapp/Transaction.kt) - The Room Entity representing a database table schema.
- [`TransactionDao.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionDao.kt) - The Data Access Object mapping Kotlin functions to SQLite queries.
- [`AppDatabase.kt`](app/src/main/java/com/example/expensetrackerapp/AppDatabase.kt) - The Room Database class providing the main database entry point.
- [`TransactionRepository.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionRepository.kt) - Manages data access operations and decouples the ViewModel from direct database queries.
- [`TransactionViewModel.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionViewModel.kt) - Exposes UI-ready data and maps interactions back to the database.
- [`TransactionAdapter.kt`](app/src/main/java/com/example/expensetrackerapp/TransactionAdapter.kt) - Handles populating and updating transaction items in the `RecyclerView`.

---

## 🚀 How to Set Up & Run the App

1. **Open in Android Studio:**
   - Launch Android Studio.
   - Click **Open** and select the project directory.
2. **Gradle Sync:**
   - Let Gradle sync download all dependencies.
3. **Build and Run:**
   - Connect an Android device or set up an Android Virtual Device (AVD) emulator.
   - Click the green **Run** button in Android Studio to build and install the app.
