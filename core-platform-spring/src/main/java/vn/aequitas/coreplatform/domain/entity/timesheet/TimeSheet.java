package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * Timesheet aggregate root. Maps to the existing {@code TimeSheets} table.
 */
@Getter
@Setter
@Entity
@Table(name = "TimeSheets")
public class TimeSheet extends BaseEntity {

    @Column(name = "Name", columnDefinition = "longtext")
    private String name = "";

    @Column(name = "Description", columnDefinition = "longtext")
    private String description = "";

    @Column(name = "Date", columnDefinition = "datetime(6)")
    private LocalDateTime date;
}
