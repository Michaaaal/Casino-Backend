package michal.malek.auth.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.exceptions.UserDontExistException;
import michal.malek.auth.exceptions.UserExistingWithLogin;
import michal.malek.auth.exceptions.UserExistingWithMail;
import michal.malek.auth.models.*;
import michal.malek.auth.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            return ResponseEntity.ok(new AuthResponse("Success"));
        }catch (UserExistingWithMail | UserExistingWithLogin e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage()));
        } catch (RuntimeException | IOException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse("Couldn't sent email"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> passwordRecovery(@RequestBody EmailDTO email, RedirectAttributes redirectAttributes){
        try{
            userService.retrievePassword(email.getEmail());
            return ResponseEntity.ok(new AuthResponse("Success"));
        }catch (UserDontExistException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage()));
        } catch ( RuntimeException | IOException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse("Couldn't sent email"));
        }

    }
    @PostMapping("/change-password")
    public ResponseEntity<?> passwordChange(@RequestBody UserChangePasswordDTO userChangePasswordDTO){
        try{
            userService.changePassword(userChangePasswordDTO);
            return ResponseEntity.ok(new AuthResponse("Success"));
        }catch (UserDontExistException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginData, HttpServletResponse response){
        return userService.login(response, userLoginData.getLogin(), userLoginData.getPassword());
    }
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,HttpServletResponse response){
        return userService.logout(request, response);
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String uid){
        try{
            userService.activateUser(uid);
            return ResponseEntity.ok(new AuthResponse("Success"));
        }catch (UserDontExistException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new AuthResponse(e.getMessage()));
        }
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request, HttpServletResponse response){
        //System.out.println("VALIDATION");
        try {
            userService.validateToken(request, response);
            return ResponseEntity.ok().body(new AuthResponse("Success"));
        }catch (IllegalArgumentException | ExpiredJwtException e){
            return ResponseEntity.status(401).body(new AuthResponse("wrong or expired token"));
        }
    }

    @GetMapping("/logged-in")
    public ResponseEntity<?> loggedIn(HttpServletRequest request, HttpServletResponse response){
        try {
            userService.validateToken(request, response);
            return ResponseEntity.ok().body(new LoginResponse(true));
        }catch (IllegalArgumentException | ExpiredJwtException e){
            return ResponseEntity.ok().body(new LoginResponse(false));
        }
    }

    @GetMapping("/auto-login")
    public ResponseEntity<?> autoLogin(HttpServletRequest request, HttpServletResponse response){
        return userService.loginByToken(request,response);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException e){
        return e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    }
}
