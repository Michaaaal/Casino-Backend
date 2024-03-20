package michal.malek.auth.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserRegisterDTO {

    @Length(min=8, max=50, message = "login should have at least 8 characters")
    private String login;
    @Email
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Length(min=8, max=75, message = "password should have at least 8 characters")
    private String password;
    private Role role;
}
