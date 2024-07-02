package com.acme.dozent.controller;

import com.acme.dozent.entity.Dozent;
import com.acme.dozent.security.JwtService;
import com.acme.dozent.service.DozentReadService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.dozent.controller.DozentGetController.REST_PATH;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/**
 * Controller für die Abfrage von Dozentdaten.
 * <img src="../../../../../asciidoc/DozentGetController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@RestController
@RequestMapping(REST_PATH)
@OpenAPIDefinition(info = @Info(title = "Dozent API", version = "v1"))
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"java:S1075", "java:S6856"})
public class DozentGetController {
    /**
     * REST-Pfad für die Dozent-Ressourcen.
     */
    public static final String REST_PATH = "/rest";

    /**
     * Pfad, um Namen abzufragen.
     */
    public static final String NAME_PATH = "/name";

    /**
     * Muster für die Dozent-ID.
     */
    public static final String ID_PATTERN = "[\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12}";

    private final DozentReadService service;
    private final JwtService jwtService;
    private final UriHelper uriHelper;

    /**
     * Suche anhand der Dozent-ID als Pfad-Parameter.
     *
     * @param id ID der zu suchenden Dozent
     * @param version Versionsnummer aus dem Header If-None-Match
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @param jwt JWT für Security
     * @return Ein Response mit dem Statuscode 200 und die gefundene Dozent mit Atom-Links oder Statuscode 404.
     */
    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = HAL_JSON_VALUE)
    // "Distributed Tracing" durch https://micrometer.io bei Aufruf eines anderen Microservice
    @Observed(name = "get-by-id")
    @Operation(summary = "Suche mit der Dozent-ID", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "Dozent gefunden")
    @ApiResponse(responseCode = "404", description = "Dozent nicht gefunden")
    @SuppressWarnings("ReturnCount")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    ResponseEntity<DozentModel> getById(
        @PathVariable final UUID id,
        @RequestHeader("If-None-Match") final Optional<String> version,
        final HttpServletRequest request,
        @AuthenticationPrincipal final Jwt jwt
    ) {
        final var username = jwtService.getUsername(jwt);
        log.debug("getById: id={}, version={}, username={}", id, version, username);
        // KEIN Optional https://github.com/spring-projects/spring-security/issues/3208
        if (username == null) {
            log.error("Trotz Spring Security wurde getById() ohne Benutzername im JWT aufgerufen");
            return status(UNAUTHORIZED).build();
        }
        final var rollen = jwtService.getRollen(jwt);
        log.trace("getById: rollen={}", rollen);

        final var dozent = service.findById(id, username, rollen, false);
        log.trace("getById: {}", dozent);

        final var currentVersion = "\"" + dozent.getVersion() + '"';
        if (Objects.equals(version.orElse(null), currentVersion)) {
            return status(NOT_MODIFIED).build();
        }

        final var model = dozentToModel(dozent, request);
        log.debug("getById: model={}", model);
        return ok().eTag(currentVersion).body(model);
    }

    private DozentModel dozentToModel(final Dozent dozent, final HttpServletRequest request) {
        final var model = new DozentModel(dozent);
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var idUri = baseUri + '/' + dozent.getId();

        final var selfLink = Link.of(idUri);
        final var listLink = Link.of(baseUri, LinkRelation.of("list"));
        final var addLink = Link.of(baseUri, LinkRelation.of("add"));
        final var updateLink = Link.of(idUri, LinkRelation.of("update"));
        final var removeLink = Link.of(idUri, LinkRelation.of("remove"));
        model.add(selfLink, listLink, addLink, updateLink, removeLink);
        return model;
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter.
     *
     * @param suchkriterien Query-Parameter als Map.
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200, die gefundene dozent als CollectionModel oder Statuscode 404.
     */
    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Suchkriterien", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "CollectionModel mid den Dozenten")
    @ApiResponse(responseCode = "404", description = "Keine Dozenten gefunden")
    CollectionModel<DozentModel> get(
        @RequestParam @NonNull final MultiValueMap<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        log.debug("get: suchkriterien={}", suchkriterien);

        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var models = service.find(suchkriterien)
            .stream()
            .map(dozent -> {
                final var model = new DozentModel(dozent);
                model.add(Link.of(baseUri + '/' + dozent.getId()));
                return model;
            })
            .toList();
        log.debug("get: {}", models);
        return CollectionModel.of(models);
    }

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix als Pfadvariable.
     * @return Die passenden Namen oder Statuscode 404, falls es keine gibt.
     */
    @GetMapping(path = NAME_PATH + "/{prefix}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Suche Namen mit Praefix", tags = "Suchen")
    String getNamenByPrefix(@PathVariable final String prefix) {
        log.debug("getNamenByPrefix: {}", prefix);
        final var namen = service.findNamenByPrefix(prefix);
        log.debug("getNamenByPrefix: {}", namen);
        return namen.stream()
            .map(name -> "\"" + name + '"')
            .toList()
            .toString();
    }
}
