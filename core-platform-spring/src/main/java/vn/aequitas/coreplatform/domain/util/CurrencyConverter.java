package vn.aequitas.coreplatform.domain.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats an amount as a localized currency string. Port of the .NET
 * {@code CurrencyConverter}; defaults to Vietnamese (vi-VN).
 */
public final class CurrencyConverter {

    private CurrencyConverter() {
    }

    public static String convertToCurrency(BigDecimal amount) {
        return convertToCurrency(amount, Locale.of("vi", "VN"));
    }

    public static String convertToCurrency(BigDecimal amount, Locale locale) {
        return NumberFormat.getCurrencyInstance(locale).format(amount);
    }
}
