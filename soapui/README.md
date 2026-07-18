# SoapUI Test Project

Import `EcommerceApp-SoapUI-project.xml` into SoapUI or ReadyAPI.

## Before Running

Start the services locally on their default ports:

```powershell
mvn -pl auth-service spring-boot:run
mvn -pl product-service spring-boot:run
mvn -pl cart-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

The project uses project-level properties for base URLs. Update them in SoapUI if your services run elsewhere.

## Coverage

- Health checks for all services
- Auth register, duplicate register, login, invalid login, token validation
- Product list, search, create, get, update, not-found cases
- Cart get, add items, invalid item, clear
- Inventory list, upsert, reserve, insufficient stock, release
- Order list, create, get, confirm, cancel, not-found
- Payment success, failure, invalid request, list
- Notification send, invalid request, list
- API Gateway route tests
- End-to-end checkout flow
- Payment failure compensation flow
