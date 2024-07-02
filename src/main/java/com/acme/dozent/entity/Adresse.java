package com.acme.dozent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Repräsentiert eine Adresse.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Entity
@Table(name = "adresse")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Adresse {
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Die Postleitzahl für die Adresse.
     *
     * @param plz Die Postleitzahl als String
     * @return Die Postleitzahl als String
     */
    private String plz;

    /**
     * Der Ort für die Adresse.
     *
     * @param ort Der Ort als String
     * @return Der Ort als String
     */
    private String ort;
}
