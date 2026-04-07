# NovaPay Backend (Microservices Architecture)

## 🚀 Overview
NovaPay is a digital payment platform built using **Spring Boot Microservices Architecture**. It supports wallet management, transactions, rewards, and notifications with secure and scalable backend services.

---

## 🏗️ Architecture
The backend is designed using microservices with the following components:

- API Gateway – Centralized routing and request handling
- User Service – User management and authentication
- Wallet Service – Wallet balance and operations
- Transaction Service – Handles transactions between users
- Reward Service – Reward points system
- Notification Service – Sends notifications

---

## 🛠️ Tech Stack
- Java, Spring Boot
- Spring Cloud (Eureka, API Gateway, OpenFeign)
- PostgreSQL / MySQL
- Docker & Docker Compose
- REST APIs
- JWT Authentication

---

## 🔐 Features
- Secure authentication using JWT
- Service discovery using Eureka
- Inter-service communication using OpenFeign
- Load balancing and scalable architecture
- Containerized services using Docker

---

## 🐳 Running with Docker

```bash
docker-compose up --build
