package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.common.BaseEntity;

/**
 * Student entity. The class keeps the original (plural) .NET name so it stays a
 * 1:1 mapping with the {@code Students} table and the navigation names used in
 * the join entities.
 */
@Getter
@Setter
@Entity
@Table(name = "Students")
public class Students extends BaseEntity {

    @Column(name = "Name", columnDefinition = "longtext")
    private String name = "";

    @Column(name = "Grade", columnDefinition = "longtext")
    private String grade = "";

    @Column(name = "Review", columnDefinition = "longtext")
    private String review = "";
}
