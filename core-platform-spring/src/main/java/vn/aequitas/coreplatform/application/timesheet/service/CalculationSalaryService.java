package vn.aequitas.coreplatform.application.timesheet.service;

import vn.aequitas.coreplatform.application.timesheet.dto.CalculationSalaryRequest;
import vn.aequitas.coreplatform.application.timesheet.dto.CalculationSalaryResponse;

import java.util.List;

/**
 * Computes teaching salary per classroom. Port of the .NET
 * {@code ICalculationSalaryService}.
 */
public interface CalculationSalaryService {

    List<CalculationSalaryResponse> calculationSalary(List<CalculationSalaryRequest> requests);
}
