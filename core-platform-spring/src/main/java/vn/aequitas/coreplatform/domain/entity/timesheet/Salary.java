package vn.aequitas.coreplatform.domain.entity.timesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.common.BaseEntity;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.math.BigDecimal;

/**
 * Base-salary table row. Maps to the existing {@code SalaryRooms} table
 * (the table name is a historical quirk carried over from the .NET model).
 */
@Getter
@Setter
@Entity
@Table(name = "SalaryRooms")
public class Salary extends BaseEntity {

    @Column(name = "Money", columnDefinition = "decimal(18,4)", nullable = false)
    private BigDecimal money;

    @Column(name = "Level")
    private LevelEnums level;

    @Column(name = "NumberOfStudent")
    private int numberOfStudent;
}
