package com.hevyfit.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorMessagesTest {

    @Test
    void resolvesExactLanguageMatch() {
        assertEquals("Recurso no encontrado", ErrorMessages.resolve(ErrorCode.NOT_FOUND, "es"));
    }

    @Test
    void fallsBackToSpanishWhenLanguageHasNoMatch() {
        assertEquals("Recurso no encontrado", ErrorMessages.resolve(ErrorCode.NOT_FOUND, "fr-FR,fr;q=0.9"));
    }

    @Test
    void fallsBackToSpanishWhenHeaderIsAbsent() {
        assertEquals("Recurso no encontrado", ErrorMessages.resolve(ErrorCode.NOT_FOUND, null));
    }
}
