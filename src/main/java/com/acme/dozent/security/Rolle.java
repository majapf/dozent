package com.acme.dozent.security;

import java.util.stream.Stream;

/**
 * Singleton für verfügbare Rollen als Strings für das Spring-Interface GrantedAuthority.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
public enum Rolle {
    /**
     * Die Rolle ADMIN.
     */
    ADMIN,

    /**
     * Die Rolle USER.
     */
    USER;

    /**
     * Zu einem String die Rolle als Enum ermitteln.
     *
     * @param str String einer Rolle
     * @return Rolle als Enum oder null
     */
    public static Rolle of(final String str) {
        return Stream.of(values())
            .filter(rolle -> rolle.name().equalsIgnoreCase(str))
            .findFirst()
            .orElse(null);
    }
}
