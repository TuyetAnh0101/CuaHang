<div align="center">

# CuaHang - Store Management App

A mobile application developed with Kotlin and Android to explore store management workflows, data handling, authentication, and cloud-based services.

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Firestore](https://img.shields.io/badge/Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/products/firestore)

[View Repository](https://github.com/TuyetAnh0101/CuaHang) • [Report Bug](https://github.com/TuyetAnh0101/CuaHang/issues)

</div>

---

## Overview

CuaHang is an Android application developed as a practical project for exploring mobile application development and store management workflows. The project combines native Android development with cloud services to deliver a robust user experience.

* **Authentication:** Secure user login and registration.
* **Cloud Data:** Real-time synchronization and reliable data storage.
* **Rich UI:** Data visualization, efficient image loading, and responsive design.

---

## Application Preview

<div align="center">

<table>
<tr>

<td align="center" width="25%">

<img src="./docs/screenshorts/home.jpg" width="180px">

<br><br>

<b>Home</b>

</td>

<td align="center" width="25%">

<img src="./docs/screenshorts/tk.jpg" width="180px">

<br><br>

<b>Account</b>

</td>

<td align="center" width="25%">

<img src="./docs/screenshorts/if.jpg" width="180px">

<br><br>

<b>Information</b>

</td>

<td align="center" width="25%">

<img src="./docs/screenshorts/add.jpg" width="180px">

<br><br>

<b>Add Item</b>

</td>

</tr>
</table>

</div>

---

## Tech Stack & Architecture

### Android Layer
* **Language:** Kotlin (JVM Toolchain 11)
* **SDK:** Min SDK 30 | Target SDK 35
* **UI Components:** AndroidX, Material Components, ConstraintLayout, CardView
* **Build System:** Gradle Kotlin DSL

### Cloud Layer (Firebase)
* **Authentication:** Secure user identity management.
* **Cloud Firestore:** Document-based database for store data.
* **Realtime Database:** Low-latency operations for dynamic content.
* **Analytics:** User behavior tracking.

### Supporting Libraries
* **[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart):** Data visualization and statistical charts.
* **[Glide](https://github.com/bumptech/glide):** Fast and efficient image loading.
* **[Gson](https://github.com/google/gson):** JSON processing and data serialization.

---

## Getting Started

### Prerequisites
* Android Studio
* JDK 11
* A Firebase Account for cloud configuration

### Installation

**1. Clone the repository**
```bash
git clone [https://github.com/TuyetAnh0101/CuaHang.git](https://github.com/TuyetAnh0101/CuaHang.git)
cd CuaHang
