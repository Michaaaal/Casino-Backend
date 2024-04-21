package michal.malek.auth.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@Getter
@Setter
public class UserChangePasswordDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Length(min=8, max=75, message = "password should have at least 8 characters")
    private String password;

    private String uid;
}
