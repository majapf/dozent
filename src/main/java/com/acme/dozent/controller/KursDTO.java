package com.acme.dozent.controller;

import jakarta.validation.constraints.NotNull;

/**
 * ValueObject für das Neuanlegen und Ändern eines neuen Dozent.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 * @param kursName Name des Dozenten.
 * @param id ID des Dozenten.
 * @param email email des Dozenten.
 */
record
KursDTO(

    @NotNull(message = "Der Name fehlt")
    String kursName,

    @NotNull(message = "Der Dozent muss eine id haben")
    String id,

    @NotNull(message = "Der Dozent muss eine email haben")
    String email
) {
}
