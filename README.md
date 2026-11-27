# TUGMAMAT

### Web-Based Construction Material Reuse and Compatibility Matching System

---

## 📄 I. Project Overview

**TugmaMat** is a web-based platform designed to help people reuse available construction materials by connecting material providers with individuals who need those materials.

Providers can post available or leftover construction materials, while requesters can browse listings and submit requests based on their project needs. The system evaluates the compatibility between available materials and requested materials using a matching score.

By helping users discover reusable materials, TugmaMat aims to make material exchange more organized and encourage the practical reuse of construction resources.

---

## ⚠️ II. Problem Statement

Construction projects may leave behind usable materials that are stored, unused, or discarded. At the same time, other individuals may need those same materials for their own projects but have difficulty finding available supplies.

Without a centralized platform, providers and requesters may rely on informal communication, making it harder to discover materials, compare requirements, and coordinate requests.

TugmaMat addresses this problem by providing a shared platform for posting materials, finding compatible listings, submitting requests, communicating, and tracking material pickup.

---

## 🎯 III. Project Objectives

The project aims to:

- Develop a centralized platform for listing and discovering reusable construction materials.
- Allow providers to post material details, quantities, conditions, locations, and photos.
- Allow requesters to search for materials and submit requests.
- Provide a compatibility matching score between material listings and requests.
- Show alternative matches when an exact or close match is not available.
- Support communication between providers and requesters.
- Help users monitor request, reservation, and pickup progress.
- Provide administrators with tools for managing users and reported listings.

---

## 👥 IV. User Roles

### Providers

- Register and log in to the platform.
- Create, update, and manage material listings.
- Upload photos and provide material details.
- Review incoming material requests.
- Confirm or cancel pickup arrangements.
- Update material availability.

### Requesters

- Register and log in to the platform.
- Browse available construction materials.
- View compatibility scores and alternative matches.
- Submit requests for needed materials.
- Communicate with providers.
- Track request and pickup history.

### Administrators

- Access the administrative dashboard.
- Review reported material listings.
- Help maintain organized platform records.
- Assist with platform management.

---

## 🤖 V. Key Features

### User Registration and Login

- Provides registration and login for providers and requesters.
- Includes an administrator account for platform management.
- Uses role-based access to present the appropriate features to each user.

### Material Listing Management

- Allows providers to create, view, update, and manage material listings.
- Supports material descriptions, categories, quantities, conditions, locations, and image uploads.
- Tracks listing availability, including **Available**, **Reserved**, and **Given Away**.

### Material Request Management

- Allows requesters to submit requests for materials.
- Records requested material details and quantities.
- Supports provider review and request processing.

### Compatibility Matching

- Calculates a compatibility score between a material listing and a request.
- Evaluates category, name or type, size, quantity, condition, and location.
- Displays close matches and alternative options based on the calculated score.

### Messaging

- Provides a communication feature for providers and requesters.
- Helps users discuss material details and coordinate requests.

### Pickup and Request Tracking

- Allows requesters to choose the quantity they need.
- Allows providers or administrators to mark a pickup or cancel it.
- Helps users monitor request and pickup history.

### Admin Management

- Provides an administrative area for reviewing reported listings.
- Supports basic platform monitoring and listing management.

---

## 🏗 VI. System Design

### 🌐 Frontend

**Technologies:** Thymeleaf, HTML, CSS, and JavaScript

**Features:**

- Web-based user interface.
- Login and registration pages.
- Provider and requester dashboards.
- Material listings and detail pages.
- Material request screens.
- Compatibility results and alternative matches.
- Messaging and history pages.
- Administrative pages.
- Responsive interface elements and animations.

### 🖥 Backend

**Language:** Java  
**Framework:** Spring Boot

**Features:**

- User and role management.
- Material listing operations.
- Material request processing.
- Compatibility scoring through `MatchingService`.
- Messaging and pickup workflow.
- Data persistence through Spring Data JPA.
- Local image upload handling.
- Demo data initialization.

### 🗄 Database

**Database:** H2 file-based database

- Uses a local file database for application data.
- Persists records in the project's `data/` directory.
- Uploaded material images are stored in the `uploads/` directory.

---

## ⚙️ VII. Technology Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Main programming language |
| Spring Boot 3 | Backend application framework |
| Thymeleaf | Server-side webpage rendering |
| Spring Data JPA | Database access and persistence |
| H2 Database | Local file-based database |
| Maven | Dependency and project management |
| HTML | Webpage structure |
| CSS | Website styling and layout |
| JavaScript | Frontend interactions and animations |
| Git and GitHub | Version control and collaboration |

---

## 💻 VIII. OOP Concepts

### Encapsulation

Organizes related data and behavior within Java classes and controls access to object properties through methods and access modifiers.

### Abstraction

Separates application operations into focused components, such as models, repositories, services, and controllers, so users of a component do not need to know every implementation detail.

### Inheritance

Can be applied where shared behavior or properties are represented through parent and child classes. The exact use depends on the implemented class structure.

### Polymorphism

Allows related objects or methods to provide different implementations of shared behavior where applicable in the system.

### Classes and Objects

Represents application elements such as users, materials, material requests, and messages through Java classes and their objects.

---

## 🚀 IX. Installation and Setup

### Prerequisites

Before running TugmaMat, make sure you have:

- Java Development Kit (JDK) 17 or later.
- Apache Maven.
- A Java IDE (optional, but recommended).
- A web browser.

### Setup Instructions

**1. Clone the repository**

```bash
git clone <repository-url>
```

**2. Open the project**

Open the cloned project folder in your preferred Java IDE or terminal.

**3. Run the application**

From the project root directory, execute:

```bash
mvn spring-boot:run
```

Alternatively, run the `TugmaMatApplication` main class from your IDE.

**4. Open the application**

Visit the following address in your browser:

```text
http://localhost:8080
```

### Demo Accounts

Use the following accounts to explore the different user roles:

| Role | Email | Password |
|---|---|---|
| Provider | `provider@tugmamat.ph` | `provider123` |
| Requester | `requester@tugmamat.ph` | `requester123` |
| Admin | `admin@tugmamat.ph` | `admin123` |

---

## 📸 X. Screenshots

The following screenshots can be added to demonstrate the main interfaces and features of TugmaMat:

- Login and Registration Pages
- Provider Dashboard
- Requester Dashboard
- Admin Dashboard
- Material Listings
- Material Details
- Create and Edit Material Listing
- Material Request Form
- Compatibility Matching Results
- Alternative Material Matches
- Messaging Interface
- Request and Pickup History
- Reported Listings Management

---

## ⚠️ XI. Limitations

- The project uses an H2 file-based database intended for local application use.
- Uploaded images are stored locally in the `uploads/` directory.
- Demo accounts and sample data are intended for demonstration.
- Matching results depend on the accuracy and completeness of the material and request information entered by users.
- A compatibility score is an aid for comparing listings; users should still confirm material suitability and details with the provider.
- The project documentation does not establish that the application is configured for production deployment.

---

## 🔮 XII. Future Enhancements

Possible improvements for future development include:

- Adding more advanced material search and filtering options.
- Improving matching recommendations using additional material attributes.
- Adding map-based location browsing.
- Providing notifications for new requests and pickup updates.
- Improving reporting and analytics for administrators.
- Adding more detailed user verification and account security.
- Supporting deployment with a production-ready database.
- Improving accessibility and mobile usability.

---

## 📌 XIII. Contributors

**Development Team:** TugmaMat

| Contributor | Main Contribution |
|---|---|
| De Leon, Marriel N. | Front-End Development and Interface Design |
| Gutierrez, April E. | Back-End Development and System Logic |
| Mendoza, Tyra P. | System Testing and Project Documentation |

---

## 📚 XIV. Project Details

- **Project Name:** TugmaMat
- **Project Course:** OOP 101 - Object-Oriented Programming
- **Project Type:** Web-Based Application
- **Project Domain:** Construction Material Reuse and Matching

---

<p align="center">
  <strong>TugmaMat — Connecting available construction materials with the people who need them.</strong>
</p>
