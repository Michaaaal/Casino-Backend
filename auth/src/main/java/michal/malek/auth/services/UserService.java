package michal.malek.auth.services;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.exceptions.UserDontExistException;
import michal.malek.auth.exceptions.UserExistingWithLogin;
import michal.malek.auth.exceptions.UserExistingWithMail;
import michal.malek.auth.models.*;
import michal.malek.auth.repositories.ResetOperationsRepository;
import michal.malek.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final ResetOperationService resetOperationService;
    private final ResetOperationsRepository resetOperationsRepository;

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

        if(request.getCookies() !=null){
            for(Cookie value: Arrays.stream(request.getCookies()).toList()){
                if(value.getName().equals("token")){
                    token = value.getValue();
                }else if(value.getName().equals("refresh")){
                    refresh = value.getValue();
                }
            }
        }else {
            throw new IllegalArgumentException("Token can't be null");
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

    public void register(UserRegisterDTO userRegisterDTO) throws UserExistingWithLogin, UserExistingWithMail, IOException {

        System.out.println(userRegisterDTO.getEmail());

        Optional<User> userByEmail = userRepository.findUserByEmail(userRegisterDTO.getEmail());
        if(userByEmail.isPresent()){
            throw new UserExistingWithMail("User with this mail already exist. ");
        }

        Optional<User> userByLogin = userRepository.findUserByLogin(userRegisterDTO.getLogin());
        if(userByLogin.isPresent()){
            throw new UserExistingWithLogin("User with this login already exist.");
        }

        User user = new User();
        user.setLock(true);
        user.setEnabled(true);
        user.setLogin(userRegisterDTO.getLogin());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        if(userRegisterDTO.getRole()!=null){
            user.setRole(userRegisterDTO.getRole());
        }else{
            user.setRole(Role.USER);
        }
        saveUser(user);
        emailService.sendActivation(user);
    }

    public ResponseEntity<?> login(HttpServletResponse response, String login, String password){
        User authRequest = User.builder().password(password).login(login).build();
        User user = userRepository.findUserByLoginAndIsEnabledAndIsLock(authRequest.getUsername()).orElse(null);

        if(user!=null){
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
                if(authentication.isAuthenticated()){
                    Cookie cookie = cookieService.generateCookie("token",generateToken(authRequest.getUsername(),jwtExpiration),jwtExpiration);
                    Cookie refresh = cookieService.generateCookie("refresh",generateToken(authRequest.getUsername(),jwtRefreshExpiration),jwtRefreshExpiration);
                    response.addCookie(cookie);
                    response.addCookie(refresh);
                    return ResponseEntity.ok("SUCCESS");
                }else {
                    return ResponseEntity.ok("WRONG PASSWORD");
                }
            } catch (AuthenticationException e) {
                return ResponseEntity.ok("SOMETHING WENT WRONG");
            }
        }

        return ResponseEntity.ok("NO SUCH ACCOUNT OR NOT VERIFIED ACC");
    }

    public ResponseEntity<?> loginByToken(HttpServletRequest request, HttpServletResponse response) {
        try{
            validateToken(request,response);
            String refresh=null;
            for (Cookie value : Arrays.stream(request.getCookies()).toList()){
                if(value.getName().equals("refresh")){
                    refresh = value.getValue();
                }
            }
            String login = jwtService.getSubject(refresh);
            User user = userRepository.findUserByLoginAndIsEnabledAndIsLock(login).orElse(null);

            if(user!=null){
                return ResponseEntity.ok(
                        UserRegisterDTO.builder()
                                .email(user.getEmail())
                                .login(user.getLogin())
                                .role(user.getRole())
                                .build());
            }
            return ResponseEntity.status(401).body("wrong or expired token");
        }
        catch (ExpiredJwtException | IllegalArgumentException e){
            return ResponseEntity.status(401).body("wrong or no token");
        }
    }

    private void saveUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);
    }

    public void activateUser(String uid) throws UserDontExistException {
        Optional<User> userByUid = userRepository.findUserByUid(uid);
        if(userByUid.isPresent()){
            userByUid.get().setLock(false);
            userRepository.save(userByUid.get());
        }else
            throw new UserDontExistException("User does not exist");
    }

    public void retrievePassword(String email) throws UserDontExistException{
        Optional<User> userByEmail = userRepository.findUserByEmail(email);

        if(userByEmail.isPresent()) {
            ResetOperations resetOperations = resetOperationService.initResetOperation(userByEmail.get());
            emailService.sendPasswordRecovery(userByEmail.get(), resetOperations.getUid());
        }
        else
            throw new UserDontExistException("User dont exist");
    }

    @Transactional
    public void changePassword(String uid, UserChangePasswordDTO userChangePasswordDTO){
        ResetOperations resetOperations = resetOperationsRepository.findByUid(uid).orElse(null);
        if(resetOperations !=null){
            Optional<User> userByUid = userRepository.findUserByUid(resetOperations.getUser().getUid());

            if(userByUid.isPresent()){
                userByUid.get().setPassword(userChangePasswordDTO.getPassword());
                userRepository.save(userByUid.get());
                resetOperationService.endOperation(resetOperations.getUid());
            }else
                throw new UserDontExistException();
        }
    }
}
