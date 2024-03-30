package michal.malek.auth.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import michal.malek.auth.annotations.FieldsValueMatch;
import org.hibernate.validator.constraints.Length;

@FieldsValueMatch.List({
        @FieldsValueMatch(
                field = "password",
                fieldMatch = "repeatPassword",
                message = "Passwords do not match!"
        )
})
@AllArgsConstructor
@Getter
@Setter
public class UserChangePasswordDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Length(min=8, max=75, message = "password should have at least 8 characters")
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String repeatPassword;

}
