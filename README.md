# NovaPay Backend (Microservices Architecture)

## 🚀 Overview
NovaPay is a digital payment platform built using **Spring Boot Microservices Architecture**. It enables secure and scalable operations such as wallet management, transactions, rewards, and notifications.

---

## 🏗️ Architecture
The system follows a distributed microservices architecture:

- **API Gateway** – Centralized routing and request handling  
- **User Service** – User registration and authentication  
- **Wallet Service** – Wallet balance and operations  
- **Transaction Service** – Handles money transfers  
- **Reward Service** – Manages reward points  
- **Notification Service** – Sends system notifications  

---

## 🔄 Request Flow
1. Client sends request to API Gateway  
2. API Gateway routes request to appropriate service  
3. Services communicate using REST and OpenFeign  
4. Response is returned via API Gateway  

**Flow Example:**  
Frontend → API Gateway → Transaction Service → Wallet Service → Response  

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
- Load balancing for scalability  
- Containerized deployment using Docker  

---

## 📡 API Endpoints

### User Service
- `POST /api/users/register`
- `POST /api/users/login`

### Wallet Service
- `GET /api/wallet/{userId}`
- `POST /api/wallet/add`

### Transaction Service
- `POST /api/transactions/send`
- `GET /api/transactions/{userId}`

---

## 🐳 Running with Docker
```bash
docker-compose up --build
⚙️ Local Setup
Clone repository
git clone https://github.com/RanjithRK14/novapay-sb.git
Start Eureka Server
Start all services:
user-service
wallet-service
transaction-service
reward-service
notification-service
Start API Gateway
Access APIs:
http://localhost:8080
📂 Project Structure
api-gateway/
user-service/
wallet-service/
transaction-service/
reward-service/
notification-service/
docker-compose.yml
📈 Key Highlights
Built distributed system with 6 independent microservices
Implemented API Gateway and service discovery using Eureka
Designed secure JWT-based authentication system
Enabled scalable communication using OpenFeign
Containerized entire application using Docker
📌 Future Improvements
Event-driven architecture using Kafka
Monitoring with Prometheus and Grafana
Caching and rate limiting
👨‍💻 Author

Ranjith Kumar
