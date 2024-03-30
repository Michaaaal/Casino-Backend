package michal.malek.auth.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.exceptions.UserDontExistException;
import michal.malek.auth.exceptions.UserExistingWithLogin;
import michal.malek.auth.exceptions.UserExistingWithMail;
import michal.malek.auth.models.UserChangePasswordDTO;
import michal.malek.auth.models.UserRegisterDTO;
import michal.malek.auth.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRegisterDTO user){
        try{
            userService.register(user);
            return ResponseEntity.ok("Success");
        }catch (UserExistingWithMail | UserExistingWithLogin e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (/*RuntimeException |*/ IOException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Couldn't send verification email !");
        }
    }
    @GetMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String uid){
        try{
            userService.activateUser(uid);
            return ResponseEntity.ok("Success");
        }catch (UserDontExistException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User dont Exist");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> passwordChange(@RequestParam String uid, @RequestBody UserChangePasswordDTO userChangePasswordDTO){
        try{
            userService.changePassword(uid,userChangePasswordDTO);
            return ResponseEntity.ok("Success");
        }catch (UserDontExistException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User dont Exist");
        }
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

    @GetMapping("/logged-in")
    public ResponseEntity<?> loggedIn(HttpServletRequest request, HttpServletResponse response){
        try {
            userService.validateToken(request, response);
            return ResponseEntity.ok().body("YES");
        }catch (IllegalArgumentException | ExpiredJwtException e){
            return ResponseEntity.ok().body("NO");
        }
    }

    @GetMapping("/auto-login")
    public ResponseEntity<?> autoLogin(HttpServletRequest request, HttpServletResponse response){
        return userService.loginByToken(request,response);
    }

    @GetMapping("/check")
    public String check(){
        return "CHECK";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException e){
        return e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    }
}
