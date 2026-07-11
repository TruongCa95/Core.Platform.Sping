package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.aequitas.coreplatform.domain.common.BaseEntity;

import java.util.UUID;

/**
 * Join row linking a {@link ClassRoom} to a {@link TimeSheet}, carrying the number
 * of students recorded for that classroom on that timesheet. Maps to the existing
 * {@code ClassRoomTimeSheets} table. The foreign keys are nullable, exactly like
 * the .NET model.
 */
@Getter
@Setter
@Entity
@Table(name = "ClassRoomTimeSheets")
public class ClassRoomTimeSheet extends BaseEntity {

    @Column(name = "NumberOfStudent")
    private int numberOfStudent;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ClassRoomId", columnDefinition = "char(36)")
    private UUID classRoomId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "TimeSheetId", columnDefinition = "char(36)")
    private UUID timeSheetId;
}
