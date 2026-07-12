package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;

import java.util.UUID;

/** Spring Data repository for {@link ClassRoom}. */
public interface ClassRoomRepository
        extends JpaRepository<ClassRoom, UUID>, JpaSpecificationExecutor<ClassRoom> {
}
