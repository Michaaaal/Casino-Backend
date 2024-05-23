package michal.malek.slots.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Entity(name = "slots_games")
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SlotsGameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int stake;
    private Date date;
    private boolean isDone;
    private long winAmount;
    private int multiplier;
    private int userId;

    @Transient
    private Map<Integer,Slot> slotMatrix;
}
