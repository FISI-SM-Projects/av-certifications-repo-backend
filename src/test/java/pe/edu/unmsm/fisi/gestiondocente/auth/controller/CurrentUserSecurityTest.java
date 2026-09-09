package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.FilterChainProxy;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.CurrentUserResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.config.SecurityConfig;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(CurrentUserSecurityTest.Config.class)
@WebAppConfiguration
@ActiveProfiles("institutional-security-test")
@TestPropertySource(properties = {
    "ldap.url=ldap://localhost:53389", "ldap.base.dn=dc=unmsm,dc=edu,dc=pe",
    "ldap.manager.dn=", "ldap.manager.password=", "ldap.user.search.base=", "ldap.user.search.filter=(uid={0})",
    "jwt.secret=7l9TsWcso/AIyGSz5K1z1YtkPzYz+4c8awtPCOWLr+k=", "jwt.expiration=3600000"
})
class CurrentUserSecurityTest {
    @Configuration @EnableWebMvc
    @Import({SecurityConfig.class, JwtFilter.class, JwtService.class, JwtAuthenticationEntryPoint.class, CurrentUserController.class})
    static class Config {
        @Bean CurrentAccountService currentAccountService() { return mock(CurrentAccountService.class); }
    }
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy filter;
    @Autowired JwtService jwt;
    @Autowired CurrentAccountService service;
    MockMvc mvc;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filter).build();
        when(service.me(any())).thenReturn(new CurrentUserResponse(1L, 2L, "real", "real@unmsm.edu.pe",
                "Nombre Real", List.of("ADMIN"), "ACTIVO", "ACTIVO", null, null, null));
    }
    @Test void meRequiresTokenDespiteAuthPrefix() throws Exception { mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized()); }
    @Test void invalidTokenRejected() throws Exception { mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized()); }
    @Test void validJwtCanReadMe() throws Exception {
        String token = jwt.generateToken(Map.of("roles", List.of("ROLE_ADMIN"), "accountId", 1), "real");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Nombre Real"));
    }
    @Test void protectedResourcesRequireToken() throws Exception { mvc.perform(get("/api/v1/constancias/docentes/22200101")).andExpect(status().isUnauthorized()); }
}
