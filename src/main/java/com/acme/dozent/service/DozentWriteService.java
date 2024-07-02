package com.acme.dozent.service;

import com.acme.dozent.entity.Dozent;
import com.acme.dozent.mail.Mailer;
import com.acme.dozent.repository.DozentRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anwendungslogik für Dozenten auch mit Bean Validation.
 * <img src="../../../../../asciidoc/DozentWriteService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DozentWriteService {
    private final DozentRepository repo;
    // private final CustomUserDetailsService userService; // NOSONAR
    private final Mailer mailer;

    /**
     * Einen neuen Dozenten anlegen.
     *
     * @param dozent Das Objekt des neu anzulegenden Dozenten.
     * @return Der neu angelegte Dozenten mit generierter ID
     * @throws EmailExistsException Es gibt bereits einen Dozenten mit der Emailadresse.
     */
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
    @Transactional
    @SuppressWarnings("TrailingComment")
    public Dozent create(final Dozent dozent) {
        log.debug("create: dozent={}", dozent);
        log.debug("create: adresse={}", dozent.getAdresse());
        log.debug("create: kurse={}", dozent.getKurse());

        if (repo.existsByEmail(dozent.getEmail())) {
            throw new EmailExistsException(dozent.getEmail());
        }

        // final var login = userService.save(user); // NOSONAR
        dozent.setUsername("user");

        final var dozentDB = repo.save(dozent);

        log.trace("create: Thread-ID={}", Thread.currentThread().threadId());
        mailer.send(dozentDB);

        log.debug("create: dozentDB={}", dozentDB);
        return dozentDB;
    }

    /**
     * Einen vorhandenen Dozenten aktualisieren.
     *
     * @param dozent Das Objekt mit den neuen Daten (ohne ID)
     * @param id           ID des zu aktualisierenden Dozenten
     * @param version Die erforderliche Version
     * @return Aktualisierter Dozent mit erhöhter Versionsnummer
     * @throws NotFoundException        Kein Dozent zur ID vorhanden.
     * @throws VersionOutdatedException Die Versionsnummer ist veraltet und nicht aktuell.
     * @throws EmailExistsException     Es gibt bereits einen Dozenten mit der Emailadresse.
     */
    @Transactional
    public Dozent update(final Dozent dozent, final UUID id,  final int version) {
        log.debug("update: dozent={}", dozent);
        log.debug("update: id={}, version={}", id, version);

        var dozentDb = repo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(id));
        log.trace("update: version={}, dozentDb={}", version, dozentDb);
        if (version != dozentDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        final var email = dozent.getEmail();
        // Ist die neue E-Mail bei einem *ANDEREN* Dozenten vorhanden?
        if (!Objects.equals(email, dozentDb.getEmail()) && repo.existsByEmail(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }
        log.trace("update: Kein Konflikt mit der Emailadresse");

        // Zu ueberschreibende Werte uebernehmen
        dozentDb.set(dozent);
        dozentDb = repo.save(dozentDb);

        log.debug("update: {}", dozentDb);
        return dozentDb;
    }
}
