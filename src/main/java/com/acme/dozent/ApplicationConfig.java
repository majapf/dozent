package com.acme.dozent;

import com.acme.dozent.security.KeycloakClientConfig;
import com.acme.dozent.security.SecurityConfig;

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 */
final class ApplicationConfig implements SecurityConfig, KeycloakClientConfig {
    ApplicationConfig() {
    }
}
