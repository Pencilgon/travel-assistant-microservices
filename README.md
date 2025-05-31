# Travel Assistant — Microservices Backend

This repository contains all backend microservices for the **AI-powered Smart Travel Guide** platform — an intelligent, modular system built to enhance the travel experience by providing tourists with real-time assistance, personalized AI-driven recommendations, up-to-date event listings, and convenient access to local services such as car rentals, cultural attractions, and essential travel information.

---

## 🧱 Microservices Overview

| Service Name          | Description                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| `eureka-server`     | Service registry (Eureka) for microservice discovery and registration     |
| `auth-service`        | Handles user authentication via Auth0 (Google/Apple sign-in)               |
| `gateway-service`     | Central API gateway for routing requests to the appropriate services       |
| `ai-chat-service`     | Connects to OpenAI for natural language interactions with tourists          |
| `information-service` | Provides multilingual country-specific data (visa, transport, safety, etc.)|
| `events-service`      | Manages events, activities, and local happenings                           |
| `car-rental-service`  | Supports vehicle listings and car rental bookings                          |
| `tourism-service`     | Offers tourism-related data such as attractions, cafes, museums, etc.      |

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/yourname/travel-assistant-microservices.git
cd travel-assistant-microservices
