package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.ProfileDashboardDto;
import dev.alexisreyna.artisanalbrew.application.dto.RobotAvatarDto;
import dev.alexisreyna.artisanalbrew.application.service.OrderService;
import dev.alexisreyna.artisanalbrew.application.service.ProfileService;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProfileService profiles;

    @MockitoBean
    private OrderService orders;

    private static final UUID PROFILE = UUID.randomUUID();

    private static org.springframework.security.core.Authentication auth() {
        return new UsernamePasswordAuthenticationToken("app-user-1", null, List.of());
    }

    private static ProfileDashboardDto dashboard() {
        return new ProfileDashboardDto(PROFILE, "Alex", "a@b.c", null, null, Instant.now(), "CUSTOMER", 0,
                new RobotAvatarDto("roast", "cream", "none", "dot", "none", "none"));
    }

    @Test
    void meFlows() throws Exception {
        mvc.perform(get("/api/profile/me")).andExpect(status().isUnauthorized());

        when(profiles.ensureProfileLinked("app-user-1")).thenReturn(PROFILE);
        when(profiles.getDashboard(PROFILE)).thenReturn(dashboard());
        mvc.perform(get("/api/profile/me").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alex"));

        when(profiles.updateDisplayName(any(), any())).thenAnswer(invocation ->
                new dev.alexisreyna.artisanalbrew.application.dto.UserProfileDto(PROFILE, "Alex", "a@b.c",
                        null, null, Instant.now(), "CUSTOMER", dashboard().avatar()));
        mvc.perform(patch("/api/profile/me").with(csrf()).with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Alex\"}"))
                .andExpect(status().isOk());

        when(profiles.updateAvatar(any(), any())).thenThrow(
                new dev.alexisreyna.artisanalbrew.application.validation.ValidationException("bad avatar"));
        mvc.perform(put("/api/profile/me/avatar").with(csrf()).with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"backdrop\":\"roast\",\"chassis\":\"x\",\"wear\":\"none\","
                                + "\"visor\":\"dot\",\"hat\":\"none\",\"hold\":\"none\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.avatar[0]").value("bad avatar"));

        mvc.perform(delete("/api/profile/me/avatar").with(csrf()).with(authentication(auth())))
                .andExpect(status().isOk());

        when(orders.getOrdersForUser(PROFILE)).thenReturn(List.of());
        mvc.perform(get("/api/profile/me/orders").with(authentication(auth())))
                .andExpect(status().isOk());
    }
}
