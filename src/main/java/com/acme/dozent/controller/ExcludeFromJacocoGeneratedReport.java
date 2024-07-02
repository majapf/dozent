package com.acme.dozent.controller;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;

// https://gist.github.com/dariahervieux/49a644fb4a12c94558f87219169ed9f7

/**
 * Annotation to put on Mapstruct mappers for generated classes to keep the annotation.
 * <i>https://github.com/mapstruct/mapstruct/issues/1528</i>
 * <i>https://github.com/mapstruct/mapstruct/issues/1574</i>
 */
@Retention(CLASS)
public @interface ExcludeFromJacocoGeneratedReport {
}
