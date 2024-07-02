package com.acme.dozent.service;

import com.acme.dozent.entity.Dozent;
import com.acme.dozent.repository.SpecificationBuilder;
import com.acme.dozent.repository.DozentRepository;
import com.acme.dozent.security.Rolle;
import io.micrometer.observation.annotation.Observed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.acme.dozent.security.Rolle.ADMIN;

/**
 * Anwendungslogik für Dozent.
 * <img src="../../../../../asciidoc/DozentReadService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DozentReadService {
    private final DozentRepository repo;
    private final SpecificationBuilder specificationBuilder;

    /**
     * Eine Dozent anhand ihrer ID suchen.
     *
     * @param id Die Id der gesuchten Dozent
     * @param username Benutzername aus einem JWT
     * @param rollen Rollen als Liste von Enums
     * @param fetchKurse true, falls die Kurse mitgeladen werden sollen
     * @return Die gefundene Dozent
     * @throws NotFoundException Falls keine Dozent gefunden wurde
     * @throws AccessForbiddenException Falls die erforderlichen Rollen nicht gegeben sind
     */
    @Observed(name = "find-by-id")
    public @NonNull Dozent findById(
        final UUID id,
        final String username,
        final List<Rolle> rollen,
        final boolean fetchKurse
    ) {
        log.debug("findById: id={}, username={}, rollen={}", id, username, rollen);

        final var dozentOptional = fetchKurse ? repo.findByIdFetchKurse(id) : repo.findById(id);
        final var dozent = dozentOptional.orElse(null);
        log.trace("findById: dozent={}", dozent);

        // beide find()-Methoden liefern ein Optional
        if (dozent != null && dozent.getUsername().contentEquals(username)) {
            // eigene Dozentsdaten
            return dozent;
        }

        if (!rollen.contains(ADMIN)) {
            // nicht admin, aber keine eigenen (oder keine) Dozentsdaten
            throw new AccessForbiddenException(rollen);
        }

        // admin: Dozentsdaten evtl. nicht gefunden
        if (dozent == null) {
            throw new NotFoundException(id);
        }
        log.debug("findById: dozent={}, kurs={}",
            dozent, fetchKurse ? dozent.getKurse() : "N/A");
        return dozent;
    }


    /**
     * Dozenten anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Dozenten oder eine leere Liste
     * @throws NotFoundException Falls keine Dozenten gefunden wurden
     */
    @SuppressWarnings("ReturnCount")
    public @NonNull Collection<Dozent> find(@NonNull final Map<String, List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var namen = suchkriterien.get("name");
            if (namen != null && namen.size() == 1) {
                return findByName(namen.getFirst(), suchkriterien);
            }

            final var emails = suchkriterien.get("email");
            if (emails != null && emails.size() == 1) {
                return findByEmail(emails.getFirst(), suchkriterien);
            }
        }

        final var specification = specificationBuilder
            .build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var dozenten = repo.findAll(specification);
        if (dozenten.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", dozenten);
        return dozenten;
    }

    private List<Dozent> findByName(final String name, final Map<String, List<String>> suchkriterien) {
        log.trace("findByName: {}", name);
        final var dozenten = repo.findByName(name);
        if (dozenten.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("findByName: {}", dozenten);
        return dozenten;
    }

    private Collection<Dozent> findByEmail(final String email, final Map<String, List<String>> suchkriterien) {
        log.trace("findByEmail: {}", email);
        final var dozent = repo
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var dozenten = List.of(dozent);
        log.debug("findByEmail: {}", dozenten);
        return dozenten;
    }

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix.
     * @return Die passenden Namen in alphabetischer Reihenfolge.
     * @throws NotFoundException Falls keine Namen gefunden wurden.
     */
    public @NonNull List<String> findNamenByPrefix(final String prefix) {
        log.debug("findNamenByPrefix: {}", prefix);
        final var namen = repo.findNamenByPrefix(prefix);
        if (namen.isEmpty()) {
            //noinspection NewExceptionWithoutArguments
            throw new NotFoundException();
        }
        log.debug("findNamenByPrefix: {}", namen);
        return namen;
    }
}
