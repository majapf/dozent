package com.acme.dozent.security;

import com.acme.dozent.KeycloakProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Konfiguration für einen Spring-HTTP-Client für Keycloak.
 */
public interface KeycloakClientConfig {
    /**
     * Logger-Objekt.
     */
    Logger LOGGER = LoggerFactory.getLogger(KeycloakClientConfig.class);

    /**
     * Bean-Methode, um ein Objekt zum Interface KeycloakRepository zu erstellen.
     *
     * @param restClientBuilder Injiziertes Objekt vom Typ RestClient.Builder
     * @param props Props
     * @return Objekt zum Interface KeycloakRepository
     */
    @Bean
    default KeycloakRepository keycloakRepository(final RestClient.Builder restClientBuilder, KeycloakProps props) {
        final var baseUrl = props.schema() + "://" + props.host() + ":" + props.port();
        LOGGER.debug("tokenRepository: baseUrl={}", baseUrl);

        final var restClient = restClientBuilder.baseUrl(baseUrl).build();
        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        return proxyFactory.createClient(KeycloakRepository.class);
    }
}
