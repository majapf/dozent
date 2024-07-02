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
package com.acme.dozent.mail;

import com.acme.dozent.MailProps;
import com.acme.dozent.entity.Dozent;
import jakarta.mail.internet.InternetAddress;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import static jakarta.mail.Message.RecipientType.TO;

/**
 * Mail-Client.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class Mailer {
    @SuppressWarnings("BooleanVariableAlwaysNegated")
    private static final boolean SMTP_ACTIVATED = Objects.equals(System.getenv("SMTP_ACTIVATED"), "true") ||
        Objects.equals(System.getProperty("smtp-activated"), "true");

    private final JavaMailSender mailSender;
    private final MailProps props;

    @Value("${spring.mail.host}")
    private String mailhost;

    /**
     * Email senden, dass es einen neuen Dozenten gibt.
     *
     * @param neuerDozent Das Objekt des neuen Dozenten.
     */
    @Async
    @SuppressWarnings({"CatchParameterName", "IllegalIdentifierName", "LocalFinalVariableName"})
    public void send(final Dozent neuerDozent) {
        if (!SMTP_ACTIVATED) {
            log.warn("SMTP ist deaktiviert.");
        }
        final MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setFrom(new InternetAddress(props.from()));
            mimeMessage.setRecipient(TO, new InternetAddress(props.sales()));
            mimeMessage.setSubject("Neuer Dozent " + neuerDozent.getId());
            final var body = "<strong>Neuer Dozent:</strong> <em>" + neuerDozent.getName() + "</em>";
            log.trace("send: Mailserver={}, Thread-ID={}, body={}", mailhost, Thread.currentThread().threadId(), body);
            mimeMessage.setText(body);
            mimeMessage.setHeader("Content-Type", "text/html");
        };

        try {
            mailSender.send(preparator);
        } catch (final MailSendException | MailAuthenticationException _) {
            // TODO Wiederholung, um die Email zu senden
            log.warn("Email nicht gesendet: Ist der Mailserver {} erreichbar?", mailhost);
        }
    }
}
