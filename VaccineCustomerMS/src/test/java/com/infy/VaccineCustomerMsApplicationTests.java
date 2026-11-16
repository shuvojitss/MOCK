package com.infy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.dto.VaccineCustomerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false"
})
class VaccineCustomerMsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Test
    void contextLoadsWithExpectedConfiguration() {
        assertThat(environment.getProperty("server.port")).isEqualTo("7575");
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("VaccineCustomerMS");
        assertThat(environment.getProperty("spring.cloud.consul.host")).isEqualTo("localhost");
        assertThat(environment.getProperty("spring.cloud.consul.port")).isEqualTo("8500");
        assertThat(environment.getProperty("spring.cloud.consul.discovery.service-name"))
                .isEqualTo("VaccineCustomerMS");
    }

    @Test
    void getSeededCustomer101ReturnsOk() throws Exception {
        mockMvc.perform(get("/vaccine/customers/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(101))
                .andExpect(jsonPath("$.firstName").value("Aarav"))
                .andExpect(jsonPath("$.lastName").value("Sharma"))
                .andExpect(jsonPath("$.email").value("aarav.sharma@infy.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getMissingCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(get("/vaccine/customers/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(404));
    }

    @Test
    void invalidPathVariableReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/vaccine/customers/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400));
    }

    @Test
    void crudLifecycleWorksEndToEnd() throws Exception {
        VaccineCustomerDTO createDto = new VaccineCustomerDTO(
                null,
                "Meera",
                "Kulkarni",
                "meera.kulkarni@infy.com",
                "9876501234",
                LocalDate.of(1995, 8, 21),
                null);

        String createdJson = mockMvc.perform(post("/vaccine/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(106))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        VaccineCustomerDTO created = objectMapper.readValue(createdJson, VaccineCustomerDTO.class);
        created.setStatus("ELIGIBLE");
        created.setLastName("Deshmukh");

        mockMvc.perform(put("/vaccine/customers/" + created.getCustomerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Deshmukh"))
                .andExpect(jsonPath("$.status").value("ELIGIBLE"));

        mockMvc.perform(delete("/vaccine/customers/" + created.getCustomerId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vaccine/customers/" + created.getCustomerId()))
                .andExpect(status().isNotFound());
    }
}
