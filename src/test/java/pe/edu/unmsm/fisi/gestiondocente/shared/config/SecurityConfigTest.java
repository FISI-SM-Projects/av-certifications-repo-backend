package pe.edu.unmsm.fisi.gestiondocente.shared.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityConfigTest {

    private static final String LDAP_BASE_DN = "dc=unmsm,dc=edu,dc=pe";

    @Test
    void usesEphemeralPortForEmbeddedLdapWithoutOverride() {
        SecurityConfig securityConfig = securityConfig("");
        UnboundIdContainer container = securityConfig.ldapContainer();
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        container.setApplicationContext(applicationContext);

        assertEquals(0, container.getPort());

        try {
            container.afterPropertiesSet();
            int effectivePort = container.getPort();
            DefaultSpringSecurityContextSource contextSource = securityConfig.embeddedContextSource(container);

            assertTrue(effectivePort > 0);
            assertArrayEquals(new String[] {"ldap://localhost:" + effectivePort + "/"}, contextSource.getUrls());
            assertEquals(LDAP_BASE_DN, contextSource.getBaseLdapPathAsString());
        } finally {
            container.destroy();
            applicationContext.close();
        }
    }

    @Test
    void embeddedLdapIgnoresConfiguredExternalUrlAndUsesEphemeralPort() {
        SecurityConfig securityConfig = securityConfig("ldap://localhost:53389");
        UnboundIdContainer container = securityConfig.ldapContainer();
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        container.setApplicationContext(applicationContext);

        assertEquals(0, container.getPort());

        try {
            container.afterPropertiesSet();
            int effectivePort = container.getPort();
            DefaultSpringSecurityContextSource contextSource = securityConfig.embeddedContextSource(container);

            assertTrue(effectivePort > 0);
            assertArrayEquals(new String[] {"ldap://localhost:" + effectivePort + "/"}, contextSource.getUrls());
            assertEquals(LDAP_BASE_DN, contextSource.getBaseLdapPathAsString());
        } finally {
            container.destroy();
            applicationContext.close();
        }
    }

    @Test
    void preservesConfiguredExternalLdapUrl() {
        SecurityConfig securityConfig = securityConfig("ldap://ldap.example:389");
        DefaultSpringSecurityContextSource contextSource = securityConfig.contextSource();

        assertArrayEquals(new String[] {"ldap://ldap.example:389/"}, contextSource.getUrls());
        assertEquals(LDAP_BASE_DN, contextSource.getBaseLdapPathAsString());
    }

    private SecurityConfig securityConfig(String ldapUrl) {
        SecurityConfig securityConfig = new SecurityConfig(null, null);
        ReflectionTestUtils.setField(securityConfig, "ldapUrl", ldapUrl);
        ReflectionTestUtils.setField(securityConfig, "ldapBaseDn", LDAP_BASE_DN);
        ReflectionTestUtils.setField(securityConfig, "ldapManagerDn", "");
        ReflectionTestUtils.setField(securityConfig, "ldapManagerPassword", "");
        return securityConfig;
    }
}
