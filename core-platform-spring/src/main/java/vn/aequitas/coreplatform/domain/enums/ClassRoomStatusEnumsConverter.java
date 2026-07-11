package vn.aequitas.coreplatform.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link ClassRoomStatusEnums} as its numeric value (0, 1, 2), matching
 * the int column written by EF Core.
 */
@Converter(autoApply = true)
public class ClassRoomStatusEnumsConverter implements AttributeConverter<ClassRoomStatusEnums, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ClassRoomStatusEnums attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ClassRoomStatusEnums convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : ClassRoomStatusEnums.fromValue(dbData);
    }
}
