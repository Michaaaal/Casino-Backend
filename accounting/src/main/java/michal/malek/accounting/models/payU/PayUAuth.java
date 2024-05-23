package michal.malek.accounting.models.payU;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PayUAuth {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String grant_type;
}
