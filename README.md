# Row-Level Security (RLS) Demo Application

A Spring Boot application demonstrating Row-Level Security implementation with multi-tenant data isolation using PostgreSQL and MS SQL Server.

## 📋 Overview

This application showcases how to implement Row-Level Security (RLS) in a multi-tenant environment, ensuring that each tenant can only access their own data. The implementation uses database-level security policies combined with application-level session management.

## 🎯 Key Features

- **Tenant Isolation**: Each tenant can only see and modify their own orders
- **Database-Level Security**: RLS policies enforced at the database level
- **Multiple Database Support**: Works with both PostgreSQL and MS SQL Server
- **Session Management**: HTTP session-based tenant context persistence
- **RESTful API**: Complete CRUD operations for orders

## 🏛️ Test Data - Famous Philosophers

The application uses famous philosophers as customer names for demonstration:

| Tenant | Philosophers | Era |
|--------|-------------|-----|
| **Tenant 1** | Socrates, Plato, Aristotle | Ancient Greek Philosophy |
| **Tenant 2** | Kant, Hegel, Nietzsche | German Philosophy |
| **Tenant 3** | Descartes, Spinoza | Rationalist Philosophy |

## 🏗️ Architecture

### Components

1. **RlsSessionHolder**: ThreadLocal-based session management storing tenant context
2. **TenantAwareDataSource**: Custom DataSource that sets database session variables
3. **RlsSessionInterceptor**: Copies session from HTTP session to ThreadLocal per request
4. **DatabaseDialect**: Strategy pattern for database-specific RLS implementations
5. **LoginController**: Handles user authentication and session creation
6. **OrdersController**: RESTful endpoints for order management

### How It Works

```
1. User logs in with tenantId and username
   ↓
2. Session stored in HTTP session and ThreadLocal
   ↓
3. For each request:
   - Interceptor copies session to ThreadLocal
   - DataSource sets database session variable (app.tenant_id)
   - Database RLS policy filters queries
   ↓
4. Only data matching tenant_id is visible
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ or MS SQL Server 2019+

### Database Setup

#### PostgreSQL

```bash
psql -U postgres -d postgres -f CreateDatabase_PostgreSQL.sql
```

#### MS SQL Server

```bash
sqlcmd -S localhost -U sa -i CreateDatabase.sql
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🔐 API Endpoints

### Authentication

#### Login
```http
POST /rls/login
Content-Type: application/json

{
  "tenantId": 1,
  "username": "test1"
}
```

#### Logout
```http
POST /rls/logout
```

### Orders Management

#### Get Orders for Tenant
#### Get All Orders (for current tenant)
```http
GET /rls/orders
```

#### Create Order
```http
POST /rls/orders
Content-Type: application/json

{
  "orderId": 100,
  "orderNumber": "ORD-100",
  "tenantId": 1,
  "orderDate": "2025-01-15",
  "orderStatus": "PENDING",
  "customername": "Socrates"
}
```

#### Update Order
```http
PUT /rls/orders/{id}
Content-Type: application/json

{
  "orderId": 1,
  "orderNumber": "ORD-001",
  "tenantId": 1,
  "orderDate": "2025-01-15",
  "orderStatus": "COMPLETED",
  "customername": "Socrates"
}
```

#### Delete Order
```http
DELETE /rls/orders/{id}
```

## 📊 Database Schema

### Orders Table

```sql
CREATE TABLE orders (
    order_id bigint PRIMARY KEY,
    order_number varchar(50),
    tenant_id bigint NOT NULL,
    order_date date,
    order_status varchar(50),
    customername varchar(100)
);
```

### RLS Policy (PostgreSQL)

```sql
CREATE POLICY tenant_access_policy ON orders
    USING (tenant_id = COALESCE(NULLIF(current_setting('app.tenant_id', TRUE), '')::bigint, 0))
    WITH CHECK (tenant_id = COALESCE(NULLIF(current_setting('app.tenant_id', TRUE), '')::bigint, 0));
```

### RLS Policy (MS SQL Server)

```sql
CREATE FUNCTION TenantAccessPredicate(@tenant_id bigint)  
    RETURNS TABLE WITH SCHEMABINDING AS  
        RETURN SELECT 1 AS TenantAccessPredicateResult 
        WHERE @tenant_id = CAST(SESSION_CONTEXT(N'TenantId') AS bigint);

CREATE SECURITY POLICY TenantAccessPolicy
    ADD FILTER PREDICATE TenantAccessPredicate(tenant_id) ON orders,
    ADD BLOCK PREDICATE TenantAccessPredicate(tenant_id) ON orders;
```

## 🔒 Security Model

### Tenant Isolation Guarantees

1. **Database-Level Protection**: RLS policies enforce isolation at the database level
2. **Application-Level Validation**: Controllers validate tenant access before operations
3. **Session-Based Context**: Tenant context tied to HTTP session
4. **No Cross-Tenant Access**: Users cannot access or modify data from other tenants

### What's Protected

- ✅ SELECT queries - Only tenant's own data visible
- ✅ INSERT operations - Can only create data for own tenant
- ✅ UPDATE operations - Can only modify own tenant's data
- ✅ DELETE operations - Can only delete own tenant's data

## 📁 Project Structure

```
rowlevelsecurity/
├── src/
│   ├── main/
│   │   ├── java/it/wiesner/db/rls/
│   │   │   ├── config/          # DataSource and MVC configuration
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── datamodel/       # JPA entities and repositories
│   │   │   ├── datasource/      # TenantAwareDataSource
│   │   │   ├── dialect/         # Database-specific implementations
│   │   │   ├── interceptor/     # Session management interceptor
│   │   │   └── session/         # RLS session holder
│   │   └── resources/
│   │       ├── application.yml  # Application configuration
│   │       └── static/          # HTML frontend
├── CreateDatabase.sql           # MS SQL Server setup script
├── CreateDatabase_PostgreSQL.sql # PostgreSQL setup script
└── pom.xml                      # Maven dependencies
```

## 📝 Configuration

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
```

### Switching Database

To use MS SQL Server instead of PostgreSQL:

1. Update `application.yml` with MS SQL Server connection details
2. Change `database-type` in `DataSourceConfiguration.java` to `mssql`

## 🐛 Troubleshooting

### Issue: "Expected 3 but was 8"
**Cause**: Row-Level Security not working  
**Solution**: Ensure database RLS policies are enabled and session variables are set correctly

### Issue: "Connection refused"
**Cause**: Database not running  
**Solution**: Start PostgreSQL/SQL Server

```bash
# PostgreSQL
sudo systemctl start postgresql

# or with Docker
docker-compose up -d
```

### Issue: "Table 'orders' doesn't exist"
**Cause**: Database not initialized  
**Solution**: Run the database setup script

## 🤝 Contributing

This is a demonstration project for educational purposes. Feel free to use it as a reference for implementing RLS in your own applications.

## 📄 License

This project is provided as-is for educational and demonstration purposes.

## 🙏 Acknowledgments

- Demo application showcasing Row-Level Security patterns
- Test data uses names of famous philosophers for educational interest
- Supports both PostgreSQL and MS SQL Server implementations
