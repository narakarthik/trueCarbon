# TrueCarbon — Trading Service

Spring Boot 3 · Java 21 · PostgreSQL · WebSocket · Port 8082

Handles: orders, matching engine, trades, price history, notifications.
Calls User Service on port 8081 for user info and portfolio updates.

---

## Local Setup

### Step 1 — Create trading_schema in PostgreSQL

```sql
\c truecarbon
CREATE SCHEMA IF NOT EXISTS trading_schema;
```

Flyway creates all tables automatically on first startup.

### Step 2 — Update DB credentials

Edit `src/main/resources/application-local.yml`:
```yaml
spring:
  datasource:
    username: your_postgres_username
    password: your_postgres_password
```

### Step 3 — Make sure User Service is running

Trading Service calls User Service at `http://localhost:8081`.
Start User Service first, then Trading Service.

### Step 4 — Run in Eclipse

1. `File → Import → Existing Maven Projects` → select `trading-service/`
2. Right-click `TradingServiceApplication.java` → `Run As → Spring Boot App`
3. VM Arguments: `-Dspring.profiles.active=local`

### Step 4 — Run with Maven

```bash
cd trading-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Service starts at: **http://localhost:8082**

---

## API Endpoints

### Orders
```
POST  /api/orders/limit          Place limit BUY or SELL order (auth)
POST  /api/orders/instant        Instant buy at best price (auth)
GET   /api/orders/my             My orders (auth)
PATCH /api/orders/{id}/cancel    Cancel open order (auth)
GET   /api/orders/book/{certId}  Live order book (public)
```

### Trades
```
GET /api/trades/my    My trade history (auth)
```

### Prices
```
GET /api/prices/history?days=60    Price chart data (public)
```

### Notifications
```
GET   /api/notifications              My notifications (auth)
GET   /api/notifications/unread-count Unread count (auth)
PATCH /api/notifications/read-all     Mark all read (auth)
```

### WebSocket
```
Connect: ws://localhost:8082/ws  (SockJS)
Subscribe: /topic/notifications/{userId}
```

---

## How Matching Works

1. User places a LIMIT BUY at ₹1,300 for 100 tCO₂
2. Engine finds all OPEN SELL orders with price ≤ ₹1,300
3. Sorted: cheapest first, oldest first (Price-Time Priority)
4. Greedy fill — matches as much as possible
5. Trade created, portfolio updated via User Service
6. Both users notified via WebSocket

## Test with curl

```bash
# Get a JWT from User Service first
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"k@test.com","password":"password123"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# Place a BUY limit order
curl -X POST http://localhost:8082/api/orders/limit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"certificateId":"CERT_ID_HERE","side":"BUY","priceLimit":1300,"credits":100}'

# Check order book (public)
curl http://localhost:8082/api/orders/book/CERT_ID_HERE

# Get price history (public)
curl "http://localhost:8082/api/prices/history?days=30"
```
