package com.infy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.dto.VaccineAppointmentDTO;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false"
})
class VaccineAppointmentMsApplicationTests {

    private static HttpServer mockCustomerServer;
    private static int mockCustomerPort;
    private static final AtomicBoolean FAIL_CUSTOMER_SERVICE = new AtomicBoolean(false);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeAll
    static void startMockCustomerServer() throws IOException {
        mockCustomerServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        mockCustomerPort = mockCustomerServer.getAddress().getPort();
        mockCustomerServer.createContext("/vaccine/customers/", exchange -> {
            if (FAIL_CUSTOMER_SERVICE.get()) {
                byte[] err = "{\"errorMessage\":\"Service Down\",\"errorCode\":503}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(503, err.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(err);
                }
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String idPart = path.substring(path.lastIndexOf('/') + 1);
            int customerId = Integer.parseInt(idPart);
            if (customerId == 9999) {
                byte[] notFound = "{\"errorMessage\":\"Customer not found\",\"errorCode\":404}"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound);
                }
                return;
            }

            String json = """
                    {
                      "customerId": %d,
                      "firstName": "Aarav",
                      "lastName": "Sharma",
                      "email": "customer%d@infy.com",
                      "phoneNumber": "9876543210",
                      "dateOfBirth": "1994-05-14",
                      "status": "ACTIVE"
                    }
                    """.formatted(customerId, customerId);
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        mockCustomerServer.start();
    }

    @AfterAll
    static void stopMockCustomerServer() {
        if (mockCustomerServer != null) {
            mockCustomerServer.stop(0);
        }
    }

    @BeforeEach
    void resetCircuitBreakerAndCustomerServer() {
        FAIL_CUSTOMER_SERVICE.set(false);
        circuitBreakerRegistry.circuitBreaker("vaccineAppointmentService").reset();
    }

    @TestConfiguration
    static class LoadBalancerTestConfig {
        @Bean
        @Primary
        ServiceInstanceListSupplier testServiceInstanceListSupplier() {
            return new ServiceInstanceListSupplier() {
                @Override
                public String getServiceId() {
                    return "VaccineCustomerMS";
                }

                @Override
                public Flux<List<ServiceInstance>> get() {
                    return Flux.just(List.of(
                            new DefaultServiceInstance(
                                    "customer-1", "VaccineCustomerMS", "localhost", mockCustomerPort, false)
                    ));
                }
            };
        }
    }

    @Test
    void contextLoadsWithExpectedConfigurationAndCircuitBreakerSettings() {
        assertThat(environment.getProperty("server.port")).isEqualTo("6565");
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("VaccineAppointmentMS");
        assertThat(environment.getProperty("spring.cloud.consul.host")).isEqualTo("localhost");
        assertThat(environment.getProperty("spring.cloud.consul.port")).isEqualTo("8500");
        assertThat(environment.getProperty("spring.cloud.consul.discovery.service-name"))
                .isEqualTo("VaccineAppointmentMS");

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("vaccineAppointmentService");
        CircuitBreakerConfig config = cb.getCircuitBreakerConfig();
        assertThat(config.getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getSlidingWindowType()).isEqualTo(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1))
                .isEqualTo(Duration.ofSeconds(30).toMillis());
        assertThat(config.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
    }

    @Test
    void scheduleAppointmentCreatesRecordAndEnrichesCustomer() throws Exception {
        VaccineAppointmentDTO requestDto = new VaccineAppointmentDTO();
        requestDto.setCustomerId(101);
        requestDto.setAppointmentDateTime(LocalDateTime.of(2026, 12, 18, 11, 30));
        requestDto.setVaccineType("Moderna");
        requestDto.setVaccinationCenter("Apollo City Center");
        requestDto.setDoseNumber(1);

        mockMvc.perform(post("/appointment/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(206))
                .andExpect(jsonPath("$.customerId").value(101))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.vaccineCustomerDTO.customerId").value(101))
                .andExpect(jsonPath("$.vaccineCustomerDTO.firstName").value("Aarav"));
    }

    @Test
    void scheduleAppointmentForMissingCustomerReturnsNotFound() throws Exception {
        VaccineAppointmentDTO requestDto = new VaccineAppointmentDTO();
        requestDto.setCustomerId(9999);
        requestDto.setAppointmentDateTime(LocalDateTime.of(2026, 12, 18, 11, 30));
        requestDto.setVaccineType("Covaxin");

        mockMvc.perform(post("/appointment/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(404));
    }

    @Test
    void getAvailableAppointmentsReturnsEnrichedAppointments() throws Exception {
        mockMvc.perform(get("/appointment/available")
                        .param("startDateTime", "2026-12-01T00:00:00")
                        .param("endDateTime", "2026-12-10T23:59:59")
                        .param("vaccineType", "Covaxin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].vaccineType").value("Covaxin"))
                .andExpect(jsonPath("$[0].vaccineCustomerDTO.customerId").value(101))
                .andExpect(jsonPath("$[1].vaccineCustomerDTO.customerId").value(102))
                .andExpect(jsonPath("$[2].vaccineCustomerDTO.customerId").value(105));
    }

    @Test
    void getAvailableAppointmentsTriggersFallbackAndOpensCircuitBreakerWhenCustomerMsFails() throws Exception {
        FAIL_CUSTOMER_SERVICE.set(true);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/appointment/available")
                            .param("startDateTime", "2026-12-01T00:00:00")
                            .param("endDateTime", "2026-12-10T23:59:59")
                            .param("vaccineType", "Covaxin"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.customerId").value(0))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.firstName").value("Service "))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.lastName").value("Unavailable "))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.email").value("service.unavailable@fallback.com"))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.phoneNumber").value("0000000000"))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.dateOfBirth").value("2000-01-01"))
                    .andExpect(jsonPath("$[0].vaccineCustomerDTO.status").value("FALLBACK"));
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("vaccineAppointmentService");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void getAvailableAppointmentsRejectsInvalidDateRangeWithoutTrippingCircuitBreaker() throws Exception {
        mockMvc.perform(get("/appointment/available")
                        .param("startDateTime", "2026-12-15T00:00:00")
                        .param("endDateTime", "2026-12-01T00:00:00")
                        .param("vaccineType", "Covaxin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Start date/time must be before or equal to end date/time"));
    }
}
