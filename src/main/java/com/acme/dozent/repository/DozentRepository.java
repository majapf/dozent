package com.acme.dozent.repository;

import com.acme.dozent.entity.Dozent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import static com.acme.dozent.entity.Dozent.ADRESSE_KURSE_GRAPH;
import static com.acme.dozent.entity.Dozent.ADRESSE_GRAPH;


/**
 * Repository für den DB-Zugriff bei Dozent.
 */
@Repository
public interface DozentRepository extends JpaRepository<Dozent,
    UUID>, JpaSpecificationExecutor<Dozent> {
    @EntityGraph(ADRESSE_GRAPH)
    @NonNull
    @Override
    List<Dozent> findAll();

    @EntityGraph(ADRESSE_GRAPH)
    @NonNull
    @Override
    List<Dozent> findAll(@NonNull Specification<Dozent> spec);

    @EntityGraph(ADRESSE_GRAPH)
    @NonNull
    @Override
    Optional<Dozent> findById(@NonNull UUID id);

    /**
     * Dozent einschließlich Kursen anhand der ID suchen.
     *
     * @param id Dozent ID
     * @return Gefundener Dozent
     */
    @Query("""
        SELECT DISTINCT k
        FROM     #{#entityName} k
        WHERE    k.id = :id
        """)
    @EntityGraph(ADRESSE_KURSE_GRAPH)
    @NonNull
    Optional<Dozent> findByIdFetchKurse(UUID id);

    /**
     * Dozent zu gegebener Emailadresse aus der DB ermitteln.
     *
     * @param email Emailadresse für die Suche
     * @return Optional mit dem gefundenen Dozent oder leeres Optional
     */
    @Query("""
        SELECT k
        FROM   #{#entityName} k
        WHERE  lower(k.email) LIKE concat(lower(:email), '%')
        """)
    @EntityGraph(ADRESSE_GRAPH)
    Optional<Dozent> findByEmail(String email);

    /**
     * Abfrage, ob es eine Dozent mit gegebener Emailadresse gibt.
     *
     * @param email Emailadresse für die Suche
     * @return true, falls es eine solchen Dozent gibt, sonst false
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean existsByEmail(String email);

    /**
     * Dozent anhand des Namens suchen.
     *
     * @param name Der (Teil-) Name der gesuchten Dozent
     * @return Die gefundenen Dozenten oder eine leere Collection
     */
    @Query("""
        SELECT   k
        FROM     #{#entityName} k
        WHERE    lower(k.name) LIKE concat('%', lower(:name), '%')
        ORDER BY k.name
        """)
    @EntityGraph(ADRESSE_GRAPH)
    List<Dozent> findByName(CharSequence name);

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix.
     * @return Die passenden Namen oder eine leere Collection.
     */
    @Query("""
        SELECT DISTINCT k.name
        FROM     #{#entityName} k
        WHERE    lower(k.name) LIKE concat(lower(:prefix), '%')
        ORDER BY k.name
        """)
    List<String> findNamenByPrefix(String prefix);
}
