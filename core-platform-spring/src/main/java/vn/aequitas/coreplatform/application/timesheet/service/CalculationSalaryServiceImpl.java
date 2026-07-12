package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.stereotype.Service;
import vn.aequitas.coreplatform.application.timesheet.dto.CalculationSalaryRequest;
import vn.aequitas.coreplatform.application.timesheet.dto.CalculationSalaryResponse;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.repository.SalaryRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Port of the .NET {@code CalculationSalaryService}.
 *
 * <p>For each request the base pay is looked up from the {@code SalaryRooms} table
 * by level and student count (using the highest tier once the count reaches the
 * {@code max} threshold of 99), then scaled by the Ki multiplier.</p>
 */
@Service
public class CalculationSalaryServiceImpl implements CalculationSalaryService {

    private static final int MAX = 99;

    private final SalaryRepository salaries;

    public CalculationSalaryServiceImpl(SalaryRepository salaries) {
        this.salaries = salaries;
    }

    @Override
    public List<CalculationSalaryResponse> calculationSalary(List<CalculationSalaryRequest> requests) {
        List<CalculationSalaryResponse> result = new ArrayList<>();
        if (requests == null || requests.isEmpty()) {
            return result;
        }

        List<Salary> salaryRows = salaries.findAll();

        for (CalculationSalaryRequest request : requests) {
            List<Salary> salaryList = salaryRows.stream()
                    .filter(s -> s.getLevel() == request.getLevel())
                    .toList();
            if (salaryList.isEmpty()) {
                continue;
            }

            BigDecimal salaryAmount;
            if (request.getNumberOfStudent() >= MAX) {
                salaryAmount = salaryList.stream()
                        .max(Comparator.comparingInt(Salary::getNumberOfStudent))
                        .map(Salary::getMoney)
                        .orElse(BigDecimal.ZERO);
            } else {
                salaryAmount = salaryList.stream()
                        .filter(s -> s.getNumberOfStudent() == request.getNumberOfStudent())
                        .map(Salary::getMoney)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
            }

            result.add(CalculationSalaryResponse.builder()
                    .classroomId(request.getClassroomId())
                    .salary(salaryAmount.multiply(calculateKi(request.getKi())))
                    .build());
        }

        return result;
    }

    /**
     * Ki (teaching-quality) multiplier. B and any unmapped grade fall back to 1.0,
     * exactly like the .NET switch expression.
     */
    public BigDecimal calculateKi(vn.aequitas.coreplatform.domain.enums.KiEnums ki) {
        if (ki == null) {
            return BigDecimal.ONE;
        }
        return switch (ki) {
            case APlus -> new BigDecimal("1.4");
            case A -> new BigDecimal("1.25");
            case BPlus -> new BigDecimal("1.1");
            case C -> new BigDecimal("0.8");
            case D -> new BigDecimal("0.5");
            default -> BigDecimal.ONE;
        };
    }
}
