-- Copyright (C) 2022 - present Marcel Gediga, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

--  docker compose exec postgres bash
--  psql --dbname=dozent --username=dozent [--file=/sql/V1.1__Insert.sql]

-- COPY mit CSV-Dateien erfordert den Pfad src/main/resources/...
-- Dieser Pfad existiert aber nicht im Docker-Image
-- https://www.postgresql.org/docs/current/sql-copy.html
INSERT INTO login (id, username, password, rollen)
VALUES
    ('30000000-0000-0000-0000-000000000000','admin','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','ADMIN,USER,ACTUATOR'),
    ('30000000-0000-0000-0000-000000000001','user','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g', 'USER');

-- Insert addresses
INSERT INTO adresse (id, plz, ort)
VALUES
    ('20000000-0000-0000-0000-000000000000', '12345', 'Karlsruhe'),
    ('20000000-0000-0000-0000-000000000001', '23456', 'Berlin'),
    ('20000000-0000-0000-0000-000000000002', '34567', 'Stuttgart');

-- Insert dozenten
INSERT INTO dozent (id, version, name, email, geburtsdatum, geschlecht, homepage, adresse_id, username, erzeugt, aktualisiert)
VALUES
    ('00000000-0000-0000-0000-000000000000', 0, 'Michael Maier', 'm.maier@uni_example.com', '1950-10-01', 'MAENNLICH', 'https://www.uni-mm.example.com', '20000000-0000-0000-0000-000000000000', 'admin', '2024-05-27 00:00:00', '2024-05-27 00:00:00'),
    ('00000000-0000-0000-0000-000000000001', 0, 'Simon Schmidt', 's.schmidt@uni_example.com', '1960-01-05', 'MAENNLICH', 'https://www.uni-ss.example.com', '20000000-0000-0000-0000-000000000001', 'user', '2024-05-27 00:00:00', '2024-05-27 00:00:00'),
    ('00000000-0000-0000-0000-000000000002', 0, 'Birgit Bach', 'b.bach@uni_example.com', '1970-07-12', 'WEIBLICH', 'https://www.uni-bb.example.com', '20000000-0000-0000-0000-000000000002', 'user', '2024-05-27 00:00:00', '2024-05-27 00:00:00');

-- Insert courses
INSERT INTO kurs (id, kursName, email, dozent_id)
VALUES
    ('10000000-0000-0000-0000-000000000000', 'Programmieren', 'programmieren@uni_example.com', '00000000-0000-0000-0000-000000000000'),
    ('10000000-0000-0000-0000-000000000001', 'Mathe', 'mathe@uni_example.com', '00000000-0000-0000-0000-000000000000'),
    ('10000000-0000-0000-0000-000000000002', 'Finanzwirtschaft', 'fiwi@uni_example.com', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000003', 'BWL', 'bwl@uni_example.com', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000004', 'Softwarearchitekturen', 'swa@uni_example.com', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000005', 'Recht', 'recht@uni_example.com', '00000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000006', 'Marketing', 'marketing@uni_example.com', '00000000-0000-0000-0000-000000000002');
