package com.acme.dozent.controller;

import com.acme.dozent.entity.Adresse;
import com.acme.dozent.entity.Dozent;
import com.acme.dozent.entity.Kurs;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import static org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT;

/**
 * Mapper zwischen Entity-Klassen.
 * Siehe build\generated\sources\annotationProcessor\java\main\...\DozentMapperImpl.java.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Mapper(nullValueIterableMappingStrategy = RETURN_DEFAULT, componentModel = "spring")
@AnnotateWith(ExcludeFromJacocoGeneratedReport.class)
interface DozentMapper {
    /**
     * Ein DTO-Objekt von DozentDTO in ein Objekt für Dozent konvertieren.
     *
     * @param dto DTO-Objekt für DozentDTO ohne ID
     * @return Konvertiertes Dozent-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "erzeugt", ignore = true)
    @Mapping(target = "geschlecht", ignore = true)
    @Mapping(target = "aktualisiert", ignore = true)
    Dozent toDozent(DozentDTO dto);

    /**
     * Ein DTO-Objekt von AdresseDTO in ein Objekt für Adresse konvertieren.
     *
     * @param dto DTO-Objekt für AdresseDTO ohne dozent
     * @return Konvertiertes Adresse-Objekt
     */
    @Mapping(target = "id", ignore = true)
    Adresse toAdresse(AdresseDTO dto);

    /**
     * Ein DTO-Objekt von KurseDTO in ein Objekt für Kurse konvertieren.
     *
     * @param dto DTO-Objekt für KurseDTO ohne ID und kunde
     * @return Konvertiertes Kurs-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    Kurs toKurs(KursDTO dto);
}
