package com.acme.dozent.controller;

import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import static com.acme.dozent.controller.ProblemType.PRECONDITION;
import static com.acme.dozent.controller.DozentWriteController.PROBLEM_PATH;

/**
 * Exception, falls die Versionsnummer im ETag fehlt oder syntaktisch ungültig ist.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
class VersionInvalidException extends ErrorResponseException {
    VersionInvalidException(final HttpStatusCode status, final String message, final URI uri) {
        this(status, message, uri, null);
    }

    VersionInvalidException(
        final HttpStatusCode status,
        final String message,
        final URI uri,
        final Throwable cause
    ) {
        super(status, asProblemDetail(status, message, uri), cause);
    }

    private static ProblemDetail asProblemDetail(final HttpStatusCode status, final String detail, final URI uri) {
        final var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + PRECONDITION.getValue()));
        problemDetail.setInstance(uri);
        return problemDetail;
    }
}
