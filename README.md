# App Creation: 2

## Np Airlines - Android Application

A modern Android flight booking application that allows users to search domestic flights, visually select seats, and manage bookings in real-time.

---

### 📚 Project Information

| Field | Details |
|-------|---------|
| **Project Type** | Class 11 Technical Project |
| **Assigned By** | Navraj Adhikhari Sir |
| **Submitted By** | Krishna Baral |

---

### 📱 About the App

Np Airlines is a comprehensive flight booking utility designed to streamline the travel experience in Nepal. Whether you're booking a quick flight to Pokhara or planning a trip to Biratnagar, this app provides real-time seat selection and instant confirmed ticketing.

---

### ✨ Features

- **Interactive Seat Selection** - visual seat map with real-time availability
- **Multi-Passenger Booking** - Select and book multiple seats at once
- **Real-time Status** - Instantly reflects booked, available, and business class seats
- **Verified Ticket Generation** - Creates premium, square (1:1) verified digital boarding passes
- **Flight Search** - Search flights between major Nepalese cities
- **Secure Authentication** - User login and registration powered by Supabase
- **Modern Material Design** - Clean, intuitive interface with premium colors and animations

---

### 🛠️ Technical Details

| Specification | Value |
|---------------|-------|
| **Language** | Java |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | MVVM Pattern |
| **Backend** | Supabase (PostgreSQL) |
| **UI Framework** | Material Design 3 |
| **Data Storage** | Supabase & SharedPreferences |

---

### 📦 Dependencies

- Supabase Client
- Retrofit & OkHttp
- Google Gson
- Material Components for Android
- ConstraintLayout

---

### ⚙️ Configuration

This project uses **Supabase** for backend services. To run the app, you must check the `local.properties` file in the project root (create it if it doesn't exist) and add your API credentials:

```properties
SUPABASE_URL=your_supabase_url_here
SUPABASE_KEY=your_supabase_anon_key_here
```

> **Note:** The `local.properties` file contains sensitive secrets and should **never** be committed to version control.

---

### 🚀 How to Run

1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

---

### 📸 App Preview

The app features:
- A "Verified" square ticket UI for confirmations
- Interactive color-coded seat map (Green=Available, Gold=Business)
- Real-time flight search and filtering
- "My Trips" booking history

---

### 📄 License

This project is created for educational purposes as part of Class 11 Technical curriculum.

---

*Developed with ❤️ by Krishna Baral*
