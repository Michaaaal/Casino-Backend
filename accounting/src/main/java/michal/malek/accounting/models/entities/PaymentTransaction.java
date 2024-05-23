package michal.malek.accounting.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "payment_transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long amount;
    private boolean isDone;
    private Date date;
    private Long userAccountId;
    private String payuId;
}
