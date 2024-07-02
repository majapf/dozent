package com.acme.dozent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Der Name, die ID und die Email Adresse eines Kurses.
 */
@Entity
@Table(name = "kurs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Kurs {

    /**
     * Die ID eines Kurses.
     */
    @Id
    @NotNull
    private  String id;

    /**
     * Der Name eines Kurses.
     */
    @NotNull
    private String kursName;

    /**
     * Die Email Adresse eines Kurses.
     */
    @Email
    @NotNull
    private  String email;
}
