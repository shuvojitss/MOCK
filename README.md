# InfyVaccineScheduler Microservices

Full implementation of the **InfyVaccineScheduler** Spring Boot Microservices architecture consisting of three independent Maven projects:

- **VaccineCustomerMS** (`port 7575`) — Customer management REST API (`/vaccine/customers/**`) backed by H2 + Spring Data JPA, registered with Consul.
- **VaccineAppointmentMS** (`port 6565`) — Vaccination appointment scheduling & availability API (`/appointment/schedule`, `/appointment/available`) using load-balanced `WebClient.Builder` and Resilience4j `@CircuitBreaker` (`vaccineAppointmentService`) with fallback (`getCustomerDetailsFallBack`), registered with Consul.
- **VaccineGatewayMS** (`port 8100`) — Spring Cloud Gateway with Consul Service Discovery (`discovery.locator.enabled=true`) and load-balanced routes (`/vaccine/**` -> `lb://VaccineCustomerMS`, `/appointment/**` -> `lb://VaccineAppointmentMS`).

## Running Locally

1. Start Consul on `http://localhost:8500` (e.g., `consul agent -dev`).
2. Start `VaccineCustomerMS`:
   ```bash
   cd VaccineCustomerMS
   mvn spring-boot:run
   ```
3. Start `VaccineAppointmentMS`:
   ```bash
   cd VaccineAppointmentMS
   mvn spring-boot:run
   ```
4. Start `VaccineGatewayMS`:
   ```bash
   cd VaccineGatewayMS
   mvn spring-boot:run
   ```

## Sample Requests

See `VaccineAppointmentMS/src/main/resources/InputData.txt` for ready-to-use verification inputs, including:
- `GET http://localhost:8100/vaccine/customers/101`
- `POST http://localhost:8100/appointment/schedule`
- `GET http://localhost:8100/appointment/available?startDateTime=2026-12-01T00:00:00&endDateTime=2026-12-10T23:59:59&vaccineType=Covaxin`
- Circuit breaker fallback verification when `VaccineCustomerMS` is stopped.

## Testing

Run all microservice tests from the root directory:

```bash
mvn test
```

Or run any service's tests individually inside its folder (`mvn test`).
