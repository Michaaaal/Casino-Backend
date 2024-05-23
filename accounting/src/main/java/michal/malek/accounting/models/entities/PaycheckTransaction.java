package michal.malek.accounting.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity(name = "paycheck_transactions")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaycheckTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long amount;
    private Date date;
    private int userAccountId;
    private boolean isDone;

}
