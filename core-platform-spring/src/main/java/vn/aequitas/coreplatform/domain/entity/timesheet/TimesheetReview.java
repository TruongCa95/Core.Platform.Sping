package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.aequitas.coreplatform.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-student review attached to a timesheet. Maps to the existing
 * {@code TimesheetReviews} table. {@code Progress} is nullable, like the .NET model.
 */
@Getter
@Setter
@Entity
@Table(name = "TimesheetReviews")
public class TimesheetReview extends BaseEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "StudentId", columnDefinition = "char(36)")
    private UUID studentId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "TimesheetId", columnDefinition = "char(36)")
    private UUID timesheetId;

    @Column(name = "Review", columnDefinition = "longtext")
    private String review = "";

    @Column(name = "Progress", columnDefinition = "decimal(18,2)")
    private BigDecimal progress;
}
