package michal.malek.accounting.models.payU;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PayUProduct {
    private String name;
    private int unitPrice;
    private int quantity;

}