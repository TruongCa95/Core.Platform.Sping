package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;

import java.util.UUID;

/** Spring Data repository for the {@link ClassRoomTimeSheet} join rows. */
public interface ClassRoomTimeSheetRepository
        extends JpaRepository<ClassRoomTimeSheet, UUID>, JpaSpecificationExecutor<ClassRoomTimeSheet> {
}
