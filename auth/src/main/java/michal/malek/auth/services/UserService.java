package michal.malek.auth.services;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.models.AuthResponse;
import michal.malek.auth.models.Role;
import michal.malek.auth.models.User;
import michal.malek.auth.models.UserRegisterDTO;
import michal.malek.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.exp}")
    private int jwtExpiration;
    @Value("${jwt.refresh.exp}")
    private int jwtRefreshExpiration;

    public String generateToken(String username, int exp){
        return jwtService.generateToken(username, exp);
    }

    public void validateToken(HttpServletRequest request, HttpServletResponse response){
        String token = null;
        String refresh = null;

        for(Cookie value: Arrays.stream(request.getCookies()).toList()){
            if(value.getName().equals("token")){
                token = value.getValue();
            }else if(value.getName().equals("refresh")){
                refresh = value.getValue();
            }
        }

        try{
            jwtService.validateToken(token);
        }catch (IllegalArgumentException | ExpiredJwtException e){
            jwtService.validateToken(refresh);
            Cookie refreshCookie = cookieService.generateCookie("refresh", jwtService.refreshToken(refresh,jwtRefreshExpiration),jwtRefreshExpiration);
            Cookie cookie = cookieService.generateCookie("token", jwtService.refreshToken(refresh,jwtExpiration),jwtExpiration);
            response.addCookie(cookie);
            response.addCookie(refreshCookie);
        }
    }

    public void register(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        user.setLogin(userRegisterDTO.getLogin());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        if(userRegisterDTO.getRole()!=null){
            user.setRole(userRegisterDTO.getRole());
        }else{
            user.setRole(Role.USER);
        }
        saveUser(user);
    }

    public AuthResponse login(HttpServletResponse response, String login, String password){
        User authRequest = User.builder().password(password).login(login).build();
        User user = userRepository.findUserByLogin(authRequest.getUsername()).orElse(null);

        if(user!=null){
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
                if(authentication.isAuthenticated()){
                    Cookie cookie = cookieService.generateCookie("token",generateToken(authRequest.getUsername(),jwtExpiration),jwtExpiration);
                    Cookie refresh = cookieService.generateCookie("refresh",generateToken(authRequest.getUsername(),jwtRefreshExpiration),jwtRefreshExpiration);
                    response.addCookie(cookie);
                    response.addCookie(refresh);
                    return AuthResponse.SUCCESS;
                }else {
                    return AuthResponse.FAIL;
                }
            } catch (AuthenticationException e) {
                return AuthResponse.FAIL;
            }
        }

        return AuthResponse.FAIL;
    }


    private void saveUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);
    }
}
