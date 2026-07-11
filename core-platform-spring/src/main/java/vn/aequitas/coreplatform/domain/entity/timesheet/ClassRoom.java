package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.common.BaseEntity;
import vn.aequitas.coreplatform.domain.enums.ClassRoomStatusEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

/**
 * Classroom aggregate root. Maps to the existing {@code ClassRooms} table.
 *
 * <p>The many-to-many navigations to {@code TimeSheet}/{@code Students} that exist
 * on the .NET entity are intentionally omitted: every handler works through the
 * explicit join entities ({@link ClassRoomTimeSheet}, {@link StudentClasses})
 * directly, so the collections were never actually read or written.</p>
 */
@Getter
@Setter
@Entity
@Table(name = "ClassRooms")
public class ClassRoom extends BaseEntity {

    @Column(name = "ClassCode", columnDefinition = "longtext")
    private String classCode = "";

    @Column(name = "Location", columnDefinition = "longtext")
    private String location = "";

    @Column(name = "ClassName", columnDefinition = "longtext")
    private String className = "";

    @Column(name = "NumberOfStudent")
    private int numberOfStudent = 1;

    @Column(name = "Level")
    private LevelEnums level;

    @Column(name = "Status")
    private ClassRoomStatusEnums status = ClassRoomStatusEnums.Active;
}
