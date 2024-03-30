package michal.malek.auth.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.exceptions.UserDontExistException;
import michal.malek.auth.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    @GetMapping("/login")
    public String login(){
        return "/login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(){
        return "/loginSuccess";
    }

    @PostMapping("/loginPost")
    public String login(String username, String password, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        ResponseEntity<?> authResponse = userService.login(response, username, password);
        redirectAttributes.addFlashAttribute("message", authResponse.getBody());

        if(authResponse.toString().equals("SUCCESS")){
            return "redirect:http://localhost:8888/auth/loginSuccess";
        }
        else{
            return "redirect:http://localhost:8888/auth/login";
        }
    }




    @GetMapping("/retrievePassword")
    public String passwordRecovery(){
        return "/retrievePassword";
    }

    @PostMapping("/password-recovery")
    public String passwordRecovery(@RequestParam String email, RedirectAttributes redirectAttributes){
        try{
            userService.retrievePassword(email);
            redirectAttributes.addFlashAttribute("message", "Mail Sent, Check you mailbox");
        }catch (UserDontExistException e){
            redirectAttributes.addFlashAttribute("message", "Could not send mail, User dont exist");
        }
        return "redirect:http://localhost:8888/auth/retrievePassword";
    }




}
