package com.hevyfit.common.error;

import java.util.Locale;
import java.util.ResourceBundle;

public final class ErrorMessages {

    private static final String BUNDLE_BASE_NAME = "com.hevyfit.common.error.Messages";

    private ErrorMessages() {
    }

    public static String resolve(ErrorCode code, String acceptLanguageHeader) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, parseLocale(acceptLanguageHeader));
        return bundle.getString(code.name());
    }

    private static Locale parseLocale(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return Locale.ROOT;
        }
        String primaryTag = acceptLanguageHeader.split(",")[0].split(";")[0].trim();
        return Locale.forLanguageTag(primaryTag);
    }
}
