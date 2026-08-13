<div align="center">

# CuaHang

### Android Application for Store Management

A mobile application developed with Kotlin and Android to explore store management workflows, data handling, authentication, and cloud-based services.

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" />
</p>

</div>

---

## Overview

CuaHang is an Android application developed as a practical project for exploring mobile application development, store management workflows, data handling, and cloud-based services.

The project combines Kotlin and Android development with Firebase services for authentication and data management.

Several supporting libraries are also integrated for data visualization, image loading, and JSON processing.

---

## Tech Stack

| Category | Technologies |
|---|---|
| Platform | Android |
| Programming Language | Kotlin |
| Build System | Gradle Kotlin DSL |
| Authentication | Firebase Authentication |
| Cloud Database | Firebase Firestore |
| Realtime Data | Firebase Realtime Database |
| Data Visualization | MPAndroidChart |
| Image Loading | Glide |
| JSON Processing | Gson |
| UI | AndroidX, Material Components, ConstraintLayout |

---

## Core Technologies

### Android Development

The application is built with **Kotlin** and the **Android SDK**, with AndroidX and Material Components used to develop the application interface.

### Firebase Services

Firebase provides the cloud services used by the application, including:

- Firebase Authentication
- Firebase Firestore
- Firebase Realtime Database
- Firebase Analytics

### Supporting Libraries

The project uses several libraries to support application functionality:

- **MPAndroidChart** for data visualization
- **Glide** for image loading and display
- **Gson** for JSON processing
- **AndroidX CardView** for UI components

---

## Technology Overview

The main technologies work together across the application and cloud service layers.

```text
Android Application
        |
        v
      Kotlin
        |
        +-------------------+
        |                   |
        v                   v
Firebase Services      Supporting Libraries
        |                   |
        |                   +-- MPAndroidChart
        |                   +-- Glide
        |                   +-- Gson
        |
        +-- Authentication
        +-- Firestore
        +-- Realtime Database
        +-- Analytics
