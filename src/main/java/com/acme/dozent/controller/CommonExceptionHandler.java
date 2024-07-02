package com.acme.dozent.controller;

import com.acme.dozent.service.AccessForbiddenException;
import com.acme.dozent.service.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Handler für allgemeine Exceptions.
 *
 * @author <a href="mailto:Maja.Pfannendoerfer@h-ka.de">Maja Pfannendörfer</a>
 */
@ControllerAdvice
@Slf4j
class CommonExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    void onNotFound(final NotFoundException ex) {
        log.debug("onNotFound: {}", ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(FORBIDDEN)
    void onAccessForbidden(final AccessForbiddenException ex) {
        log.debug("onAccessForbidden: {}", ex.getMessage());
    }
}
