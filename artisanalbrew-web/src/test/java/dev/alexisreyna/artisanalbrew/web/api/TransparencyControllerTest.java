package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.DatabaseSchemaDto;
import dev.alexisreyna.artisanalbrew.application.service.DatabaseSchemaService;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransparencyController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class TransparencyControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DatabaseSchemaService schemas;

    @Test
    void schemaAndStats() throws Exception {
        var schema = new DatabaseSchemaDto("2026-01-01T00:00:00Z", "PostgreSQL", 2, 3, 1, 10L,
                List.of("note"), List.of(
                        new dev.alexisreyna.artisanalbrew.application.dto.SchemaTableDto(
                                "Order", "Orders", "Commerce", 7L, List.of(), List.of()),
                        new dev.alexisreyna.artisanalbrew.application.dto.SchemaTableDto(
                                "Product", "Products", "Commerce", 3L, List.of(), List.of())));
        when(schemas.getSchema(true)).thenReturn(schema);

        mvc.perform(get("/api/transparency/schema"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=60")))
                .andExpect(jsonPath("$.tableCount").value(2));

        mvc.perform(get("/api/transparency/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(10))
                .andExpect(jsonPath("$.groups[0].group").value("Commerce"))
                .andExpect(jsonPath("$.groups[0].tables").value(2))
                .andExpect(jsonPath("$.groups[0].rows").value(10));
    }
}
