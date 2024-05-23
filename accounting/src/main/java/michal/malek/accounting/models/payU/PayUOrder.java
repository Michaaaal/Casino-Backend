package michal.malek.accounting.models.payU;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PayUOrder {
    private String notifyUrl;
    private String customerIp;
    private String merchantPosId;
    private String description;
    private String currencyCode;
    private long totalAmount;
    private String extOrderId;
    private PayUBuyer buyer;
    private List<PayUProduct> products;

}
