/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.dozent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import static com.acme.dozent.entity.Dozent.ADRESSE_GRAPH;
import static com.acme.dozent.entity.Dozent.ADRESSE_KURSE_GRAPH;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static java.util.Collections.emptyList;

/**
 * Daten eines Dozenten. In DDD ist Dozent ist ein Aggregate Root.
 * <img src="../../../../../asciidoc/Dozent.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
// https://thorben-janssen.com/java-records-hibernate-jpa
@Entity
@Table(name = "dozent")
@NamedEntityGraph(name = ADRESSE_GRAPH, attributeNodes = @NamedAttributeNode("adresse"))
@NamedEntityGraph(name = ADRESSE_KURSE_GRAPH, attributeNodes = {
    @NamedAttributeNode("adresse"), @NamedAttributeNode("kurse")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString
@Builder
@SuppressWarnings({
    "ClassFanOutComplexity",
    "RequireEmptyLineBeforeBlockTagGroup",
    "DeclarationOrder",
    "JavadocDeclaration",
    "MissingSummary",
    "RedundantSuppression", "com.intellij.jpb.LombokEqualsAndHashCodeInspection"})
public class Dozent {
    /**
     * NamedEntityGraph für das Attribut "adresse".
     */
    public static final String ADRESSE_GRAPH = "Dozent.adresse";

    /**
     * NamedEntityGraph für die Attribute "adresse" und "kurse".
     */
    public static final String ADRESSE_KURSE_GRAPH = "Dozent.adresseKurse";

    // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
    // https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate
    // https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate
    /**
     * Die ID des Dozenten.
     *
     * @param id Die ID.
     * @return Die ID.
     */
    @Id
    // https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html...
    // ...#identifiers-generators-uuid
    // https://in.relation.to/2022/05/12/orm-uuid-mapping
    @GeneratedValue
    // Oracle: https://in.relation.to/2022/05/12/orm-uuid-mapping
    // @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @EqualsAndHashCode.Include
    private UUID id;

    // https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html...
    // ...#locking-optimistic-mapping
    /**
     * Versionsnummer für optimistische Synchronisation.
     */
    @Version
    private int version;

    /**
     * Der Name des Dozenten.
     *
     * @param name Der Name.
     * @return Der Name.
     */
    private String name;

    /**
     * Die Emailadresse des Dozenten.
     *
     * @param email Die Emailadresse.
     * @return Die Emailadresse.
     */
    private String email;

    /**
     * Das Geburtsdatum des Dozenten.
     *
     * @param geburtsdatum Das Geburtsdatum.
     * @return Das Geburtsdatum.
     */
    private LocalDate geburtsdatum;

    /**
     * Die URL zur Homepage des Dozenten. Bei URI statt URL muss nur die Syntax stimmen und die Ressource muss nicht
     * existieren.
     *
     * @param homepage Die URL zur Homepage.
     * @return Die URL zur Homepage.
     */
    private URL homepage;

    /**
     * Das Geschlecht des Dozenten.
     *
     * @param geschlecht Das Geschlecht.
     * @return Das Geschlecht.
     */
    @Enumerated(STRING)
    private GeschlechtType geschlecht;

    /**
     * Die Adresse des Dozenten.
     *
     * @param adresse Die Adresse.
     * @return Die Adresse.
     */
    @OneToOne(optional = false, cascade = {PERSIST, REMOVE}, fetch = LAZY, orphanRemoval = true)
    @ToString.Exclude
    private Adresse adresse;

    /**
     * Die Kurse des Dozenten.
     *
     * @param kurse Die Kurse.
     * @return Die Kurse.
     */
    // Default: fetch=LAZY
    @OneToMany(cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "dozent_id")
    @OrderColumn(name = "idx", nullable = false)
    @ToString.Exclude
    private List<Kurs> kurse;

    private String username;

    // https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html...
    // ...#mapping-generated-CreationTimestamp
    @CreationTimestamp
    private LocalDateTime erzeugt;

    // https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html...
    // ...#mapping-generated-UpdateTimestamp
    @UpdateTimestamp
    private LocalDateTime aktualisiert;

    /**
     * Dozentendaten überschreiben.
     *
     * @param dozent Neue Dozentendaten.
     */
    public void set(final Dozent dozent) {
        name = dozent.name;
        email = dozent.email;
        geburtsdatum = dozent.geburtsdatum;
        homepage = dozent.homepage;
        geschlecht = dozent.geschlecht;
        kurse = dozent.kurse;
    }
}
