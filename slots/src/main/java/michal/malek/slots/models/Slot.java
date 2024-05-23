package michal.malek.slots.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Slot {
    private int slotId;
    private boolean isWinning;
}
