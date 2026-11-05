# TrueCarbon — User Service

Spring Boot 3 · Java 21 · PostgreSQL · Port 8081

---

## Local Setup (No Docker needed)

### Step 1 — PostgreSQL

Install PostgreSQL locally and create the database:

```sql
CREATE DATABASE truecarbon;
CREATE SCHEMA IF NOT EXISTS user_schema;
```

Or using psql:
```bash
psql -U postgres -c "CREATE DATABASE truecarbon;"
psql -U postgres -d truecarbon -c "CREATE SCHEMA IF NOT EXISTS user_schema;"
```

### Step 2 — Update DB credentials

Edit `src/main/resources/application-local.yml`:
```yaml
spring:
  datasource:
    username: your_postgres_username
    password: your_postgres_password
```

### Step 3 — Run in Eclipse

1. Import as Maven project → `File → Import → Existing Maven Projects`
2. Select `user-service/` folder
3. Right-click `UserServiceApplication.java` → `Run As → Spring Boot App`
4. Add VM arg: `-Dspring.profiles.active=local`

   OR in Eclipse Run Configuration → Arguments → VM arguments:
   ```
   -Dspring.profiles.active=local
   ```

### Step 3 — Run with Maven (terminal)

```bash
cd user-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Service starts at: **http://localhost:8081**

---

## API Endpoints

### Auth (Public)
```
POST /api/auth/register    { name, email, password }
POST /api/auth/login       { email, password }
POST /api/auth/refresh     { refreshToken }
POST /api/auth/logout      { refreshToken }
```

### Certificates (Public GET, Auth for POST)
```
GET  /api/certificates             Browse all VERIFIED
GET  /api/certificates/{id}        Single certificate
GET  /api/certificates/my          My own certificates (auth)
POST /api/certificates             Submit new certificate (multipart, auth)
PATCH /api/certificates/{id}/verify  Verify certificate (auth, stub - auto-approves)
```

### Portfolio (Auth)
```
GET /api/portfolio    My holdings
```

### Internal (Trading Service only — X-Internal-Key header required)
```
GET  /internal/users/{userId}
GET  /internal/certificates/{certId}
GET  /internal/portfolio/{userId}/{certId}
POST /internal/portfolio/credit
POST /internal/portfolio/debit
POST /internal/portfolio/reserve
POST /internal/portfolio/release
POST /internal/certificates/{certId}/deduct
```

---

## Test it with curl

```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Karthik","email":"karthik@test.com","password":"password123"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"karthik@test.com","password":"password123"}'

# Use the accessToken from login response in subsequent requests
curl http://localhost:8081/api/certificates \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Future additions (when ready)
- Replace local file storage with AWS S3 in `CertificateService.saveFileLocally()`
- Add Kafka event publishing after certificate verification
- Add Redis caching for certificate listings
- Add Docker + docker-compose
