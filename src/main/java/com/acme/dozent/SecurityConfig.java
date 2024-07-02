package com.acme.dozent;

import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.ResourceServerExpressionInterceptUrlRegistryPostProcessor;
import java.util.Map;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import static com.acme.dozent.controller.DozentGetController.NAME_PATH;
import static com.acme.dozent.controller.DozentGetController.REST_PATH;
import static com.acme.dozent.security.AuthController.AUTH_PATH;
import static com.acme.dozent.security.Rolle.ADMIN;
import static com.acme.dozent.security.Rolle.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

// https://github.com/spring-projects/spring-security/tree/master/samples
/**
 * Security-Konfiguration.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@SuppressWarnings("TrailingComment")
public interface SecurityConfig {
    // https://foojay.io/today/how-to-do-password-hashing-in-java-applications-the-right-way
    /**
     * Salt für Argon2.
     */
    int SALT_LENGTH = 32; // default: 16
    /**
     * Hash-Länge für Argon2.
     */
    int HASH_LENGTH = 64; // default: 32
    /**
     * Parallelität für Argon2.
     */
    int PARALLELISM = 1; // default: 1 (Bouncy Castle kann keine Parallelitaet)
    /**
     * Anzahl Bits für "Memory Consumption" bei Argon2.
     */
    int NUMBER_OF_BITS = 14;
    /**
     * "Memory Consumption" in KBytes bei Argon2.
     */
    int MEMORY_CONSUMPTION_KBYTES = 1 << NUMBER_OF_BITS; // default: 2^14 KByte = 16 MiB  ("Memory Cost Parameter")
    /**
     * Anzahl Iterationen bei Argon2.
     */
    int ITERATIONS = 3; // default: 3

    /**
     * Bean-Methode zur Integration von Spring Security mit Keycloak.
     *
     * @return Post-Prozessor für Spring Security zur Integration mit Keycloak
     */
    @Bean
    default ResourceServerExpressionInterceptUrlRegistryPostProcessor authorizePostProcessor() {
        return registry -> registry
            .requestMatchers(OPTIONS, "/rest/**").permitAll()
            .requestMatchers("/rest/**").authenticated()
            .anyRequest().authenticated();
    }

    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu konfigurieren,
     * d.h. vor Anwendung von @PreAuthorize.
     *
     * @param http Injiziertes Objekt von HttpSecurity als Ausgangspunkt für die Konfiguration.
     * @param authenticationConverter Injiziertes Objekt von Converter für die Anpassung an Keycloak
     * @return Objekt von SecurityFilterChain
     * @throws Exception Wegen HttpSecurity.authorizeHttpRequests()
     */
    // https://github.com/spring-projects/spring-security-samples/blob/main/servlet/java-configuration/...
    // ...authentication/preauth/src/main/java/example/SecurityConfiguration.java
    @Bean
    @SuppressWarnings("LambdaBodyLength")
    default SecurityFilterChain securityFilterChain(
        final HttpSecurity http,
        final Converter<Jwt, AbstractAuthenticationToken> authenticationConverter
    ) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> {
                final var restPathDozentId = REST_PATH + "/*";
                authorize
                    .requestMatchers(OPTIONS, REST_PATH + "/**").permitAll()
                    .requestMatchers(GET, AUTH_PATH + "/me").hasAnyRole(ADMIN.name(), USER.name())

                    // https://spring.io/blog/2020/06/30/url-matching-with-pathpattern-in-spring-mvc
                    // https://docs.spring.io/spring-security/reference/current/servlet/integrations/mvc.html
                    .requestMatchers(GET, REST_PATH).hasRole(ADMIN.name())
                    .requestMatchers(
                        GET,
                        REST_PATH + NAME_PATH + "/*",
                        "/swagger-ui.html"
                    ).hasRole(ADMIN.name())
                    .requestMatchers(GET, restPathDozentId).hasAnyRole(ADMIN.name(), USER.name())
                    .requestMatchers(PUT, restPathDozentId).hasRole(ADMIN.name())
                    .requestMatchers(PATCH, restPathDozentId).hasRole(ADMIN.name())
                    .requestMatchers(DELETE, restPathDozentId).hasRole(ADMIN.name())

                    .requestMatchers(POST, "/dev/db_populate").hasRole(ADMIN.name())

                    .requestMatchers(POST, REST_PATH, "/graphql", AUTH_PATH + "/login").permitAll()

                    .requestMatchers(
                        // Actuator: Health mit Liveness und Readiness fuer Kubernetes
                        EndpointRequest.to(HealthEndpoint.class),
                        // Actuator: Prometheus fuer Monitoring
                        EndpointRequest.to(PrometheusScrapeEndpoint.class)
                    ).permitAll()
                    // OpenAPI bzw. Swagger UI und GraphiQL
                    .requestMatchers(GET, "/v3/api-docs.yaml", "/v3/api-docs", "/graphiql").permitAll()
                    .requestMatchers("/error", "/error/**").permitAll()

                    // https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html
                    // https://marco.dev/spring-boot-h2-error
                    // .requestMatchers(toH2Console()).permitAll()

                    .anyRequest().authenticated();
            })

            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter))
            )

            // Spring Security erzeugt keine HttpSession und verwendet keine fuer SecurityContext
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable) // NOSONAR
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
            .build();
    }

    /**
     * Bean-Methode für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist.
     *
     * @return "Checker-Objekt" für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist
     */
    @Bean
    default CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    /**
     * Bean-Definition, um den Verschlüsselungsalgorithmus für Passwörter bereitzustellen.
     * Es wird Argon2id statt bcrypt (Default-Algorithmus von Spring Security) verwendet.
     *
     * @return Objekt für die Verschlüsselung von Passwörtern.
     */
    @Bean
    default PasswordEncoder passwordEncoder() {
        // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
        // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Password_Storage_Cheat_Sheet.md
        // https://www.rfc-editor.org/rfc/rfc9106.html
        final var idForEncode = "argon2id";
        final Map<String, PasswordEncoder> encoders = Map.of(
            idForEncode,
            new Argon2PasswordEncoder(
                SALT_LENGTH,
                HASH_LENGTH,
                PARALLELISM,
                MEMORY_CONSUMPTION_KBYTES,
                ITERATIONS
            )
        );
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }
}
