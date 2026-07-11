package vn.aequitas.coreplatform.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Base type shared by every persisted entity. Mirrors the .NET {@code BaseEntity}:
 * an application-assigned {@code Guid} primary key plus audit / soft-delete columns.
 *
 * <p>The id and audit columns keep their original PascalCase names so the mapping
 * stays compatible with the schema created by the EF Core migrations. GUIDs are
 * stored as {@code char(36)} (the Pomelo/MySQL representation), so the {@link UUID}
 * is bound as its 36-character string form.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "Id", columnDefinition = "char(36)", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "IsActive")
    private boolean isActive = true;

    @Column(name = "CreatedBy", columnDefinition = "longtext")
    private String createdBy = "";

    @Column(name = "UpdatedBy", columnDefinition = "longtext")
    private String updatedBy = "";

    @Column(name = "CreatedDate", columnDefinition = "datetime(6)")
    private LocalDateTime createdDate = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "UpdatedDate", columnDefinition = "datetime(6)")
    private LocalDateTime updatedDate = LocalDateTime.now(ZoneOffset.UTC);
}
