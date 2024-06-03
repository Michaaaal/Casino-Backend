package michal.malek.accounting.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDTO {
    private String phone;
    private String email;
    private String bankAccountNumber;
    private String firstName;
    private String lastName;
}
