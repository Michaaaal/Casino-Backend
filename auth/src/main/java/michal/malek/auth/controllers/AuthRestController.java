package michal.malek.auth.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.models.UserRegisterDTO;
import michal.malek.auth.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody UserRegisterDTO user){
        userService.register(user);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request, HttpServletResponse response){
        //System.out.println("VALIDATION");

        try {
            userService.validateToken(request, response);
            return ResponseEntity.ok().body("Success");
        }catch (IllegalArgumentException | ExpiredJwtException e){
            return ResponseEntity.status(401).body("wrong or expired token");
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok("CHECK");
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException e){
        return e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    }
}
