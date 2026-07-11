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
 * Enrolment join row linking a {@link Students} to a {@link ClassRoom}.
 * Maps to the existing {@code StudentClasses} table.
 */
@Getter
@Setter
@Entity
@Table(name = "StudentClasses")
public class StudentClasses extends BaseEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "StudentId", columnDefinition = "char(36)")
    private UUID studentId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ClassId", columnDefinition = "char(36)")
    private UUID classId;
}
