package michal.malek.auth.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {
    private String field;
    private String fieldMatch;

    @Override
    public void initialize(final FieldsValueMatch constraintAnnotation) {
        field = constraintAnnotation.field();
        fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field field = value.getClass().getDeclaredField(this.field);
            field.setAccessible(true);
            final Object fieldValue = field.get(value);

            Field fieldMatch = value.getClass().getDeclaredField(this.fieldMatch);
            fieldMatch.setAccessible(true);
            final Object fieldMatchValue = fieldMatch.get(value);

            if (fieldValue != null) {
                return fieldValue.equals(fieldMatchValue);
            } else {
                return fieldMatchValue == null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
