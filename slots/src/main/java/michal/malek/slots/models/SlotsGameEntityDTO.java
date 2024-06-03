package michal.malek.slots.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SlotsGameEntityDTO {
    private long stake;
    private Date date;
    private boolean isDone;
    private long winAmount;
    private int multiplier;
}
