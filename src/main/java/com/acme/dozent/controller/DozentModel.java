package com.acme.dozent.controller;

import com.acme.dozent.entity.Adresse;
import com.acme.dozent.entity.Dozent;
import com.acme.dozent.entity.GeschlechtType;
import com.acme.dozent.entity.Kurs;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;



/**
 * Model-Klasse für Spring HATEOAS. @lombok.Data fasst die Annotationsn @ToString, @EqualsAndHashCode, @Getter, @Setter
 * und @RequiredArgsConstructor zusammen.
 * <img src="../../../../../asciidoc/DozentModel.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@JsonPropertyOrder({
    "name", "email", "geburtsdatum", "homepage", "geschlecht",
    "adresse","kurse", "faktultaeten"
})
@Relation(collectionRelation = "dozenten", itemRelation = "dozent")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@ToString(callSuper = true)
class DozentModel extends RepresentationModel<DozentModel> {
    private final String name;

    @EqualsAndHashCode.Include
    private final String email;

    private final LocalDate geburtsdatum;
    private final URL homepage;
    private final GeschlechtType geschlecht;
    private final Adresse adresse;
    private final List<Kurs> kurse;

    DozentModel(final Dozent dozent) {
        name = dozent.getName();
        email = dozent.getEmail();
        geburtsdatum = dozent.getGeburtsdatum();
        homepage = dozent.getHomepage();
        geschlecht = dozent.getGeschlecht();
        adresse = dozent.getAdresse();
        kurse = dozent.getKurse();
    }
}
