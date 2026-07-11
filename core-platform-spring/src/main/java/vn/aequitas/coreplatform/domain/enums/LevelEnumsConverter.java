package vn.aequitas.coreplatform.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link LevelEnums} as its explicit numeric value (1, 2, 3, 99) rather
 * than the JPA-default ordinal, keeping the column data identical to what EF Core
 * wrote. {@code autoApply = true} wires it to every {@code LevelEnums} field.
 */
@Converter(autoApply = true)
public class LevelEnumsConverter implements AttributeConverter<LevelEnums, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LevelEnums attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public LevelEnums convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : LevelEnums.fromValue(dbData);
    }
}
