CREATE TABLE IF NOT EXISTS login (
                                     id       UUID PRIMARY KEY,
                                     username VARCHAR(20) NOT NULL UNIQUE,
                                     password VARCHAR(180) NOT NULL,
                                     rollen   VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS adresse (
                                       id   UUID PRIMARY KEY,
                                       plz  CHAR(5) NOT NULL CHECK (plz ~ '\d{5}'),
                                       ort  VARCHAR(40) NOT NULL
);
CREATE INDEX IF NOT EXISTS adresse_plz_idx ON adresse(plz);

CREATE TABLE IF NOT EXISTS dozent (
                                            id               UUID PRIMARY KEY,
                                            version          INTEGER NOT NULL DEFAULT 0,
                                            name             VARCHAR(40) NOT NULL,
                                            email            VARCHAR(40) NOT NULL UNIQUE,
                                            geburtsdatum     DATE CHECK (geburtsdatum < CURRENT_DATE),
                                            geschlecht       VARCHAR(9) CHECK (geschlecht ~ 'MAENNLICH|WEIBLICH|DIVERS'),
                                            homepage         VARCHAR(40),
                                            adresse_id       UUID NOT NULL UNIQUE REFERENCES adresse(id),
                                            username         VARCHAR(20) NOT NULL REFERENCES login(username),
                                            erzeugt          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            aktualisiert     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS dozent_name_idx ON dozent(name);

CREATE TABLE IF NOT EXISTS kurs (
                                         id               UUID PRIMARY KEY,
                                         kursName         VARCHAR(40) NOT NULL,
                                         email            VARCHAR(40) NOT NULL UNIQUE,
                                         dozent_id  UUID  REFERENCES dozent(id),
                                         idx              INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS kurs_dozent_id_idx ON kurs(dozent_id);
