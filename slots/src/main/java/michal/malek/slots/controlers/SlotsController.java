package michal.malek.slots.controlers;

import lombok.RequiredArgsConstructor;
import michal.malek.slots.models.BalanceUpdateDTO;
import michal.malek.slots.models.GameIdDTO;
import michal.malek.slots.models.SlotResponse;
import michal.malek.slots.models.SlotsGameEntity;
import michal.malek.slots.services.SlotsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/slots")
public class SlotsController {
    private final SlotsService slotsService;

    @PostMapping("/spin")
    public ResponseEntity<?> spin(@RequestBody BalanceUpdateDTO balanceUpdateDTO){
        try{
            int stake = (int)balanceUpdateDTO.getAmount();
            int userId = (int)balanceUpdateDTO.getUserId();
            SlotsGameEntity spin = slotsService.startSlotGame(stake, userId);
            return ResponseEntity.ok(spin);
        }catch (Exception e){
            return ResponseEntity.status(400).body(new SlotResponse("Something went wrong: ") + e.getMessage());
        }
    }

    @PostMapping("/make-game-done")
    public ResponseEntity<?> spin(@RequestBody GameIdDTO gameIdDTO){
        System.out.println("make game done");
        try{
            slotsService.makeSlotGameDone((long)gameIdDTO.getGameId());
            return ResponseEntity.ok(new SlotResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.status(401).body(new SlotResponse("Something went wrong: ") + e.getMessage());
        }
    }

}
