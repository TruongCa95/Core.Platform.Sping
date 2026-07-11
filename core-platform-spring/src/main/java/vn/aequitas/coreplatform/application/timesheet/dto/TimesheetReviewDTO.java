package vn.aequitas.coreplatform.application.timesheet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-student review carried in timesheet payloads / results.
 * Port of the .NET {@code TimesheetReviewDTO}.
 *
 * <p>Note: the .NET DTO decorated {@code Name} with Newtonsoft's {@code [JsonIgnore]},
 * but the API serializes with System.Text.Json which ignores that attribute - so
 * {@code name} is in fact emitted by the .NET API. This port keeps the same
 * observable behaviour and serializes {@code name}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class TimesheetReviewDTO {

    private UUID studentId;

    private String name;

    private String review = "";

    private BigDecimal progress;
}
