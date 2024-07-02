package com.acme.dozent.controller;

import com.acme.dozent.controller.DozentDTO.OnCreate;
import com.acme.dozent.service.EmailExistsException;
import com.acme.dozent.service.DozentWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import static com.acme.dozent.controller.DozentGetController.ID_PATTERN;
import static com.acme.dozent.controller.DozentGetController.REST_PATH;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

/**
 * Eine Controller-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../asciidoc/DozentWriteController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Controller
@RequestMapping(REST_PATH)
@Validated
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "java:S1075", "preview", "java:S6856"})
class DozentWriteController {
    /**
     * Basispfad für "type" innerhalb von ProblemDetail.
     */
    @SuppressWarnings("TrailingComment")
    static final String PROBLEM_PATH = "/problem/";

    private static final String VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt";

    private final DozentWriteService service;
    private final DozentMapper mapper;
    private final UriHelper uriHelper;

    /**
     * Einen neuen Dozent-Datensatz anlegen.
     *
     * @param dozentDTO Das Dozentobjekt aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     *      sind oder die Emailadresse bereits existiert oder Statuscode 400 falls syntaktische Fehler im Request-Body
     *      vorliegen.
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einen neuen Dozenten anlegen", tags = "Neuanlegen")
    @ApiResponse(responseCode = "201", description = "Dozent neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<Void> post(
        @RequestBody @Validated({Default.class, OnCreate.class}) final DozentDTO dozentDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        log.debug("post: dozentDTO{}", dozentDTO);

        if (dozentDTO.username() == null || dozentDTO.password() == null) {
            return badRequest().build();
        }

        final var dozentInput = mapper.toDozent(dozentDTO);
        final var dozent = service.create(dozentInput);
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var location = URI.create(baseUri + '/' + dozent.getId());
        return created(location).build();
    }

    /**
     * Einen vorhandenen Dozent-Datensatz überschreiben.
     *
     * @param id ID des zu aktualisierenden Dozentenn.
     * @param dozentDTO Das Dozentsobjekt aus dem eingegangenen Request-Body.
     * @param version Versionsnummer aus dem Header If-Match
     * @param request Das Request-Objekt, um ggf. die URL für ProblemDetail zu ermitteln
     * @return Response mit Statuscode 204 oder Statuscode 400, falls der JSON-Datensatz syntaktisch nicht korrekt ist
     *      oder 422 falls Constraints verletzt sind oder die Emailadresse bereits existiert
     *      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Eine Dozent mit neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "Dozent nicht vorhanden")
    @ApiResponse(responseCode = "412", description = "Versionsnummer falsch")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    @ApiResponse(responseCode = "428", description = VERSIONSNUMMER_FEHLT)
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody @Valid final DozentDTO dozentDTO,
        @RequestHeader("If-Match") final Optional<String> version,
        final HttpServletRequest request
    ) {
        log.debug("put: id={}, dozentDTO={}", id, dozentDTO);
        final int versionInt = getVersion(version, request);
        final var dozentInput = mapper.toDozent(dozentDTO);
        final var dozent = service.update(dozentInput, id, versionInt);
        log.debug("put: {}", dozent);
        return noContent().eTag("\"" + dozent.getVersion() + '"').build();
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(final Optional<String> versionOpt, final HttpServletRequest request) {
        log.trace("getVersion: {}", versionOpt);
        final var versionStr = versionOpt.orElseThrow(() -> new VersionInvalidException(
            PRECONDITION_REQUIRED,
            VERSIONSNUMMER_FEHLT,
            URI.create(request.getRequestURL().toString()))
        );
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungueltiges ETag " + versionStr,
                URI.create(request.getRequestURL().toString())
            );
        }

        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungueltiges ETag " + versionStr,
                URI.create(request.getRequestURL().toString()),
                ex
            );
        }

        log.trace("getVersion: version={}", version);
        return version;
    }

    @ExceptionHandler
    ProblemDetail onConstraintViolations(
        final ConstraintViolationException ex,
        final HttpServletRequest request
    ) {
        log.debug("onConstraintViolations: {}", ex.getMessage());

        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            // Methodenname und Argumentname entfernen: siehe @Valid in der Service-Klasse
            ex.getMessage().replace("create.dozent.", "")
                .replace("update.dozent.", "")
        );
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}/\{ProblemType.CONSTRAINTS.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));

        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onEmailExists(final EmailExistsException ex, final HttpServletRequest request) {
        log.debug("onEmailExists: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}/\{ProblemType.CONSTRAINTS.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onMessageNotReadable(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.debug("onMessageNotReadable: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}/\{ProblemType.BAD_REQUEST.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }
}
