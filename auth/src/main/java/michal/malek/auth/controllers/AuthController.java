package michal.malek.auth.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.models.AuthResponse;
import michal.malek.auth.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        AuthResponse authResponse = userService.login(response, username, password);
        redirectAttributes.addFlashAttribute("message", authResponse.toString());

        System.out.println(authResponse.isOk());
        if(authResponse.isOk()){
            return "redirect:http://localhost:8888/auth/loginSuccess";
        }
        else{
            return "redirect:http://localhost:8888/auth/login";
        }
    }


}
