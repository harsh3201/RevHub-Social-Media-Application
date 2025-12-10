# 🚀 RevHub — Social Media Application(Monolithic)

A modern full-stack social media platform built with **Angular**, **Spring Boot**, **MySQL**, and **MongoDB**.  
RevHub supports user authentication, posting, likes/comments, real-time chat, and notifications — built with a clean, modular, and scalable architecture.

---

## 📌 Quick Summary
RevHub demonstrates full-stack development best practices:
- Frontend: Angular (components, services, routing)
- Backend: Spring Boot (REST APIs, security, WebSockets)
- Data: MySQL for relational data, MongoDB for chat/notifications
- Auth: JWT-based authentication
- Real-time: WebSockets for chat & live notifications

---

## 🧰 Tech Stack
- **Frontend:** Angular, TypeScript, RxJS  
- **Backend:** Spring Boot, Spring Security, Spring Data JPA, WebSockets  
- **Databases:** MySQL (users/posts/comments/likes), MongoDB (chat & notifications)  
- **Tools:** Maven, Node/npm, Docker (optional), Postman

---

## 🔁 Features
- User registration & login (JWT)
- Profile management
- Create/Read/Update/Delete posts (text + media)
- Like, comment, reply
- Global & personalized feed
- One-to-one real-time chat
- Real-time notifications for likes/comments/messages
- Clean code separation (controllers → services → repositories)

---

## 📂 Repository Structure
RevHub-Social-Media-Application/
├── revHubBack/ # Spring Boot backend
│ ├── src/main/java/...
│ └── pom.xml
└── RevHub/RevHub/ # Angular frontend
├── src/
└── package.json

---

## ⚙️ Prerequisites
- Java 17+ & Maven
- Angular18+ & npm
- MySQL (or change to H2 for quick testing)
- MongoDB (for chat/notifications)
- Git

---

## 🔧 Setup & Run (Development)

### Backend (Spring Boot)
1. Configure MySQL and MongoDB credentials in `revHubBack/src/main/resources/application.properties`.
2. From project root:
```bash
cd revHubBack
mvn clean install
mvn spring-boot:run

### Frontend (Angular18)
npm install
ng serve --open
