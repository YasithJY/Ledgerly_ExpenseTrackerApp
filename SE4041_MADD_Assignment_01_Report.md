# SE4041 MADD Assignment 01: Project Documentation Report

**Student Name:** R.K.M.J. Yasith Jayasundara  
**App Name:** Ledgerly (Local-First Personal Finance and Expense Tracker)

---

## 1. Planning & Requirement Analysis

### Problem Identification
Modern personal finance applications are increasingly reliant on cloud infrastructure, leading to complex user experiences, mandated account creations, and significant privacy concerns. Users are often required to surrender sensitive financial data to third-party servers. Furthermore, existing solutions often lack specialized tools for managing modern spending habits, such as 'Buy Now, Pay Later' (BNPL) schemes, leading to disjointed financial tracking.

### Target Audience
Ledgerly is designed for privacy-conscious individuals and students who require robust financial tracking without compromising their personal data. The application caters to users who prefer local data storage and need efficient tools to manage daily expenses and installment-based purchases.

### Feasibility Study
*   **Technical Feasibility:** The proposed tech stack (Kotlin, MVVM, Room, Coroutines, Dagger Hilt) is highly mature and fully supported by the Android ecosystem. Implementing offline OCR via Google ML Kit is technically viable and aligns with the offline-first requirement.
*   **Economic Feasibility:** The project utilizes open-source libraries and free SDKs (Google ML Kit), resulting in negligible development costs. The absence of backend server infrastructure eliminates recurring hosting expenses.
*   **Operational Feasibility:** The intuitive Material Design interface ensures a low learning curve for the target audience. The local-first approach guarantees high availability and operational reliability without dependency on internet connectivity.

### Scope
The project adheres to a strict offline-first scope. All data persistence, processing (including OCR), and business logic execution occur entirely on the device. Cloud synchronization and remote backups are explicitly excluded from the current scope to prioritize privacy and immediate performance.

---

## 2. Defining Requirements

### Functional Requirements
*   **Transaction Management:** Users must be able to add, edit, view, and delete income and expense transactions.
*   **Receipt Scanning (OCR):** The application must allow users to capture or upload receipt images and automatically parse key financial data (e.g., total amount, date) using on-device OCR processing.
*   **BNPL Splitting:** The system must include a dedicated tool to automatically split 'Buy Now, Pay Later' purchases into manageable monthly installments and project future liabilities.
*   **Categorization:** Users must be able to categorize transactions for detailed expenditure analysis.

### Non-Functional Requirements
*   **Real-time UI Updates:** The user interface must react immediately to underlying data changes utilizing `LiveData` (or `StateFlow`) to ensure a seamless experience.
*   **Offline Reliability:** The application must function flawlessly without any internet connection, relying entirely on the local Room database.
*   **Maintainability & Scalability:** The codebase must strictly follow the MVVM (Model-View-ViewModel) architectural pattern, utilizing Dagger Hilt for dependency injection to ensure loose coupling and testability.
*   **Design Aesthetics:** The UI must adhere to Material Design principles, specifically implementing the 60-30-10 color rule within a clean, modern fintech aesthetic.

---

## 3. Overview of the Application

Ledgerly is a sophisticated, privacy-centric personal finance management tool tailored for the Android platform. By leveraging a local-first architecture, it provides users with instantaneous access to their financial data while ensuring absolute privacy.

The core functionalities revolve around an intuitive dashboard that presents a holistic view of the user's financial health. Users can quickly input transactions manually or utilize the advanced On-Device Receipt Scanner to automate data entry. The unique 'Buy Now, Pay Later' (BNPL) module seamlessly integrates installment tracking into the user's overall budget, mitigating the risk of overextension.

The user interface employs a refined fintech aesthetic, emphasizing clarity and ease of use. The strict application of the 60-30-10 color rule ensures visual harmony across all screens, guiding the user's attention to critical financial metrics and actionable elements.

*[Insert Screenshot Here - Dashboard/Home Screen]*  
*[Insert Screenshot Here - Add Transaction/OCR Scanner]*  
*[Insert Screenshot Here - BNPL Splitter]*  

---

## 4. Challenges Faced

The development of Ledgerly presented several significant technical hurdles that required advanced problem-solving strategies:

*   **Dagger Hilt Integration:** Integrating Dagger Hilt for Dependency Injection midway through the development cycle proved highly complex. Refactoring existing tight couplings to utilize constructor injection required extensive architectural modifications. A specific challenge involved resolving recurring `@AndroidEntryPoint` annotation crashes, which were ultimately traced to improper module scoping and missing annotations on base fragment classes. This required a systematic audit of the component hierarchy to ensure correct dependency provision across the application lifecycle.
*   **Offline OCR Implementation:** Implementing Google ML Kit for on-device receipt scanning introduced significant performance challenges. Initial implementations resulted in main thread blocking during the OCR parsing phase, leading to UI freezes ("Application Not Responding" errors). This was resolved by aggressively offloading the ML Kit processing tasks to background threads utilizing Kotlin Coroutines (`Dispatchers.IO`), ensuring the main thread remained unblocked while maintaining responsive UI state updates.
*   **State Management:** Ensuring consistent state across various UI components while performing asynchronous database operations required careful orchestration of `LiveData` and Coroutines, particularly when handling complex aggregations for the dashboard view.

---

## 5. Testing Procedures and Results

To ensure the robustness and reliability of Ledgerly, a comprehensive testing strategy was implemented encompassing both Unit and Integration testing methodologies.

### Unit Testing
Unit tests were utilized to validate isolated business logic components, primarily focusing on ViewModels and utility classes.
*   **BNPL Logic:** Extensive unit testing was conducted on the mathematical splitting logic of the BNPL feature to ensure accurate calculation of installments across various durations and principal amounts, including edge cases with non-divisible numbers.
*   **ViewModel State:** ViewModels were tested to verify that state emissions (via `LiveData`) accurately reflected the intended business logic transitions, particularly during data loading and error handling scenarios.

### Integration Testing
Integration tests were designed to verify the interaction between different architectural layers, specifically the repository and the local database.
*   **Database Operations:** Tests were implemented to confirm that Room database read, write, update, and delete operations executed correctly and subsequently triggered the expected updates in the UI layer. This ensured data integrity and verified the reactive data flow from the database to the View.

---

## GitHub Repository
[https://github.com/YasithJY/Ledgerly_ExpenseTrackerApp.git]
