package michal.malek.slots.controlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import michal.malek.slots.models.GameIdDTO;
import michal.malek.slots.models.SlotResponse;
import michal.malek.slots.models.SlotsGameEntity;
import michal.malek.slots.services.SlotsService;
import michal.malek.slots.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/slots")
public class SlotsController {
    private final SlotsService slotsService;
    private final UserService userService;

    @GetMapping("/spin")
    public ResponseEntity<?> spin(HttpServletRequest request, @RequestParam long stake){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try{
            SlotsGameEntity spin = slotsService.startSlotGame(stake, userId);
            return ResponseEntity.ok(spin);
        }catch (Exception e){
            return ResponseEntity.status(400).body(new SlotResponse("Something went wrong: ") + e.getMessage());
        }
    }

    @PatchMapping("/make-game-done")
    public ResponseEntity<?> spin(@RequestBody GameIdDTO gameIdDTO){
        try{
            slotsService.makeSlotGameDone(gameIdDTO.getGameId());
            return ResponseEntity.ok(new SlotResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.status(400).body(new SlotResponse("Something went wrong: ") + e.getMessage());
        }
    }

    @GetMapping("/get-winning-games")
    public ResponseEntity<?> getWinningGames(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }
        return ResponseEntity.ok(slotsService.getWinningSlotsGames(userId));
    }

}
