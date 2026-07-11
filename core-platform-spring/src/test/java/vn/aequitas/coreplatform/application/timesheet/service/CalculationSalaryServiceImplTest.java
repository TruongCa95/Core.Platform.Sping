package vn.aequitas.coreplatform.application.timesheet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import vn.aequitas.coreplatform.domain.enums.KiEnums;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of the .NET {@code CalculationSalaryServiceTests}. {@code calculateKi} is
 * pure and never touches the unit of work, so a {@code null} dependency is safe.
 */
class CalculationSalaryServiceImplTest {

    private final CalculationSalaryServiceImpl service = new CalculationSalaryServiceImpl(null);

    @ParameterizedTest
    @CsvSource({
            "APlus, 1.4",
            "A, 1.25",
            "BPlus, 1.1",
            "B, 1.0",   // B has no explicit case -> default multiplier
            "C, 0.8",
            "D, 0.5"
    })
    void calculateKi_returnsExpectedMultiplier(KiEnums ki, String expected) {
        BigDecimal result = service.calculateKi(ki);
        assertEquals(0, new BigDecimal(expected).compareTo(result));
    }
}
