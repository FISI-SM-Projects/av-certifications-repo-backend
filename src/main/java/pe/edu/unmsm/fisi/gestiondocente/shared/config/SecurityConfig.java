package pe.edu.unmsm.fisi.gestiondocente.shared.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtAuthenticationEntryPoint;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    @Value("${ldap.manager.dn}")
    private String ldapManagerDn;

    @Value("${ldap.manager.password}")
    private String ldapManagerPassword;

    @Value("${ldap.user.search.base}")
    private String ldapUserSearchBase;

    @Value("${ldap.user.search.filter}")
    private String ldapUserSearchFilter;

    public SecurityConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    @Profile("dev")
    public UnboundIdContainer ldapContainer() {
        UnboundIdContainer container = new UnboundIdContainer(ldapBaseDn, "classpath:users.ldif");
        container.setPort(53389);
        return container;
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        String fullUrl = ldapUrl.endsWith("/") ? ldapUrl +
                ldapBaseDn : ldapUrl + "/" + ldapBaseDn;
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(fullUrl);
        if (ldapManagerDn != null && !ldapManagerDn.isBlank() && ldapManagerPassword != null && !ldapManagerPassword.isBlank()) {
            contextSource.setUserDn(ldapManagerDn);
            contextSource.setPassword(ldapManagerPassword);
        }

        return contextSource;
    }

    @Bean
    public AuthenticationManager authenticationManager(DefaultSpringSecurityContextSource contextSource) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserSearchBase(ldapUserSearchBase);
        factory.setUserSearchFilter(ldapUserSearchFilter);

        return factory.createAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(sessionManager -> sessionManager
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger/**","/swagger-ui/**", "/v3/api-docs/**","/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
