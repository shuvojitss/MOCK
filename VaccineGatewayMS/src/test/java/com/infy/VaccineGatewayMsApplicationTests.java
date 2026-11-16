package com.infy;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false"
})
class VaccineGatewayMsApplicationTests {

    private static HttpServer customerServer;
    private static HttpServer appointmentServer;
    private static int customerPort;
    private static int appointmentPort;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private Environment environment;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @BeforeAll
    static void startDownstreamServers() throws IOException {
        customerServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        customerPort = customerServer.getAddress().getPort();
        customerServer.createContext("/vaccine/customers/101", exchange -> {
            byte[] body = "{\"customerId\":101,\"firstName\":\"Aarav\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        customerServer.start();

        appointmentServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        appointmentPort = appointmentServer.getAddress().getPort();
        appointmentServer.createContext("/appointment/available", exchange -> {
            byte[] body = "[{\"appointmentId\":201,\"vaccineType\":\"Covaxin\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        appointmentServer.start();
    }

    @AfterAll
    static void stopDownstreamServers() {
        if (customerServer != null) {
            customerServer.stop(0);
        }
        if (appointmentServer != null) {
            appointmentServer.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.VaccineCustomerMS[0].uri",
                () -> "http://localhost:" + customerPort);
        registry.add("spring.cloud.discovery.client.simple.instances.VaccineAppointmentMS[0].uri",
                () -> "http://localhost:" + appointmentPort);
    }

    @Test
    void contextLoadsWithExpectedGatewayConfiguration() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties yamlProps = yaml.getObject();
        assertThat(yamlProps).isNotNull();
        assertThat(yamlProps.getProperty("server.port")).isEqualTo("8100");

        assertThat(environment.getProperty("spring.application.name")).isEqualTo("VaccineGatewayMS");
        assertThat(environment.getProperty("spring.cloud.consul.host")).isEqualTo("localhost");
        assertThat(environment.getProperty("spring.cloud.consul.port")).isEqualTo("8500");
        assertThat(environment.getProperty("spring.cloud.consul.discovery.service-name"))
                .isEqualTo("VaccineGatewayMS");
        assertThat(environment.getProperty("spring.cloud.gateway.discovery.locator.enabled")).isEqualTo("true");

        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        assertThat(routes).isNotNull();
        assertThat(routes).anySatisfy(route -> {
            assertThat(route.getId()).isEqualTo("VaccineCustomerMS");
            assertThat(route.getUri().toString()).isEqualTo("lb://VaccineCustomerMS");
        });
        assertThat(routes).anySatisfy(route -> {
            assertThat(route.getId()).isEqualTo("VaccineAppointmentMS");
            assertThat(route.getUri().toString()).isEqualTo("lb://VaccineAppointmentMS");
        });
    }

    @Test
    void gatewayRoutesVaccineRequestsToCustomerService() {
        webTestClient.get()
                .uri("/vaccine/customers/101")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo(101)
                .jsonPath("$.firstName").isEqualTo("Aarav");
    }

    @Test
    void gatewayRoutesAppointmentRequestsToAppointmentService() {
        webTestClient.get()
                .uri("/appointment/available")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].appointmentId").isEqualTo(201)
                .jsonPath("$[0].vaccineType").isEqualTo("Covaxin");
    }
}
