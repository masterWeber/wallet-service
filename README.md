# Wallet Service

A microservice for managing user wallet balances. It supports creating wallets, performing deposit and withdrawal operations, and retrieving the current balance.

## Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA**
- **PostgreSQL**
- **Liquibase**
- **Docker & Docker Compose**
- **Lombok**

## Quick Start

### Requirements
- Docker and Docker Compose
- Java 17 (for local build)

### Environment Variables Configuration
The project uses a `.env` file for configuration. Create a `.env` file based on the example:
```bash
cp .env.example .env
```
In `.env`, you can configure database connection parameters and connection pool size.

### Run via Docker Compose
The easiest way to start the service along with the database:
```bash
docker compose up --build
```
The application will be available at `http://localhost:8080`.

### Local Run (Gradle)
1. Ensure PostgreSQL is running (you can start only the database via docker compose):
   ```bash
   docker compose up -d db
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## API Endpoints

### 1. Create Wallet
**POST** `/api/v1/wallets`

**Request Body:**
```json
{
  "balance": 1000
}
```

**Response:**
```json
{
  "walletId": "uuid",
  "balance": 1000
}
```

### 2. Perform Operation (DEPOSIT/WITHDRAW)
**POST** `/api/v1/wallet`

**Request Body:**
```json
{
  "walletId": "uuid",
  "operationType": "DEPOSIT",
  "amount": 500
}
```
* `operationType`: `DEPOSIT` (deposit) or `WITHDRAW` (withdrawal).
* `amount`: A positive number.

**Response:**
```json
{
  "status": "SUCCESS"
}
```

### 3. Get Balance
**GET** `/api/v1/wallets/{walletId}`

**Response:**
```json
{
  "walletId": "uuid",
  "balance": 1500
}
```

## Error Handling
- `404 Not Found`: Wallet not found.
- `400 Bad Request`: Insufficient funds or invalid data format.
- `500 Internal Server Error`: Internal server error.
