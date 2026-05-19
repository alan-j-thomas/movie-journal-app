# 🎬 Movie Journal Microservices App

A full-stack **Movie Journal & Watchlist platform** built using **Spring Boot Microservices + React + Docker + CI/CD**, enhanced with **AI features** and **real-time chat**.

---

## 🚀 Overview

This project is a **scalable microservices-based application** that allows users to:

* 📓 Maintain a personal movie journal
* ⭐ Manage watchlists
* 🤖 Get AI-powered recommendations & insights
* 💬 Chat in real-time with other users
* 🔐 Authenticate securely

The system is designed using **modern backend architecture principles** with **independent services, service discovery, and API gateway routing**.

---

## 🧱 Architecture

```text
Frontend (React)
       ↓
API Gateway
       ↓
-----------------------------------
|  User Service                  |
|  Movie Service                 |
|  Watchlist Service             |
|  Journal Service               |
|  AI Service                    |
|  Chat Service (Realtime)       |
|  Notification Service          |
-----------------------------------
       ↓
Service Registry (Eureka)
       ↓
Independent Databases per Service
```

---

## ⚙️ Tech Stack

### 🖥️ Backend

* Java 17
* Spring Boot
* Spring Cloud (Eureka, Gateway)
* REST APIs

### 🌐 Frontend

* React.js
* Axios
* Responsive UI

### 🗄️ Database

* MySQL (per service database design)

### 🐳 DevOps & Deployment

* Docker
* Docker Compose
* CI/CD (Jenkins)
* Code Quality (SonarCloud)

### 🤖 Additional Features

* AI Service (recommendations / insights)
* Real-time Chat Service (WebSocket / messaging)

---

## 🧩 Microservices

| Service                   | Description             |
| ------------------------- | ----------------------- |
| 🔐 Authentication Service | User login & security   |
| 👤 User Service           | User profile management |
| 🎬 Movie Service          | Movie data & metadata   |
| ⭐ Watchlist Service       | Manage user watchlists  |
| 📓 Journal Service        | Personal movie journal  |
| 🤖 AI Service             | Smart recommendations   |
| 💬 Chat Service           | Real-time communication |
| 🔔 Notification Service   | Alerts & updates        |
| 🚪 API Gateway            | Central routing         |
| 🧭 Service Registry       | Service discovery       |

---

## 🐳 Docker Setup

All services are containerized using Docker.

### ▶️ Run the system

```bash
docker-compose up -d
```

## 🔄 CI/CD Pipeline

Automated pipeline using Jenkins:

```text
GitHub → Jenkins → Build → SonarCloud → Docker → Deploy
```

Features:

* Automated builds
* Docker image creation
* Continuous deployment

---

## 📂 Project Structure

```text
movie-microservices/
├── UserService/
├── MovieService/
├── WatchlistService/
├── JournalService/
├── AI-Service/
├── ChatService/
├── NotificationService/
├── ApiGateway/
├── ServiceRegistry/
├── frontend/
├── docker-compose.yml
└── Jenkinsfile
```

---

## ⚡ Key Features

* Microservices architecture
* Independent databases per service
* Service discovery using Eureka
* API Gateway routing
* Dockerized deployment
* CI/CD automation
* AI-powered features
* Real-time chat system

---

## 🧠 Design Principles

* Loose coupling
* High scalability
* Independent deployment
* Fault isolation
* Domain-driven service design

---

## 🚧 Future Improvements

* Kubernetes deployment
* Monitoring (Prometheus + Grafana)
* Centralized logging
* API rate limiting
* OAuth2 / social login

---

## 👨‍💻 Author

Developed as a full-stack microservices project showcasing:

* Backend architecture
* DevOps pipeline
* Scalable system design

---

## ⭐ If you like this project

Give it a star ⭐ on GitHub!

---
