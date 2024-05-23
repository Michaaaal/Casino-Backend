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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final GoogleOAuth2Service googleOAuth2Service;

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
            if(user.isGoogle()){
                return ResponseEntity.status(401).body(new AuthResponse("SOMETHING WENT WRONG"));
            }
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
                if(authentication.isAuthenticated()){
                    Cookie cookie = cookieService.generateCookie("token",generateToken(authRequest.getUsername(),jwtExpiration),jwtExpiration);
                    Cookie refresh = cookieService.generateCookie("refresh",generateToken(authRequest.getUsername(),jwtRefreshExpiration),jwtRefreshExpiration);
                    response.addCookie(cookie);
                    response.addCookie(refresh);
                    return ResponseEntity.ok(
                            UserRegisterDTO
                                    .builder()
                                    .login(user.getUsername())
                                    .email(user.getEmail())
                                    .role(user.getRole())
                                    .id(user.getId())
                                    .build());

                }else {
                    return ResponseEntity.status(401).body(new AuthResponse("WRONG PASSWORD"));
                }
            } catch (AuthenticationException e) {
                return ResponseEntity.status(401).body(new AuthResponse("SOMETHING WENT WRONG"));
            }
        }

        return ResponseEntity.status(401).body(new AuthResponse("NO SUCH ACCOUNT OR NOT VERIFIED ACC"));
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
                                .id(user.getId())
                                .build());
            }
            return ResponseEntity.status(401).body(new AuthResponse("wrong or expired token"));
        }
        catch (ExpiredJwtException | IllegalArgumentException e){
            return ResponseEntity.status(401).body(new AuthResponse("wrong or no token"));
        }
    }

    private void saveUser(User user){
        if(!user.getPassword().isEmpty())
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

    public void retrievePassword(String email) throws UserDontExistException, IOException {
        Optional<User> userByEmail = userRepository.findUserByEmail(email);

        if(userByEmail.isPresent() && !userByEmail.get().isGoogle()) {
            ResetOperations resetOperations = resetOperationService.initResetOperation(userByEmail.get());
            emailService.sendPasswordRecovery(userByEmail.get(), resetOperations.getUid());
        }
        else
            throw new UserDontExistException("User dont exist");
    }

    @Transactional
    public void changePassword( UserChangePasswordDTO userChangePasswordDTO){
        ResetOperations resetOperations = resetOperationsRepository.findByUid(userChangePasswordDTO.getUid()).orElse(null);
        if(resetOperations !=null){
            Optional<User> userByUid = userRepository.findUserByUid(resetOperations.getUser().getUid());

            if(userByUid.isPresent()){
                userByUid.get().setPassword(passwordEncoder.encode(userChangePasswordDTO.getPassword()));
                userRepository.save(userByUid.get());
                resetOperationService.endOperation(resetOperations.getUid());
            }else
                throw new UserDontExistException();
        }
    }

    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = cookieService.removeCookie(request.getCookies(),"token");
        if (cookie != null){
            response.addCookie(cookie);
        }
        cookie = cookieService.removeCookie(request.getCookies(),"refresh");
        if (cookie != null){
            response.addCookie(cookie);
        }
        return ResponseEntity.ok().body(new AuthResponse("Success"));
    }

    public ResponseEntity<?> loginWithGoogle(String code, HttpServletResponse response) {

        OAuth2AccessToken accessToken = googleOAuth2Service.codeToAccessToken(code);
        OAuth2AuthenticationToken authentication = googleOAuth2Service.accessTokenToAuthToken(accessToken);

        OAuth2User user = authentication.getPrincipal();

        try {
            if(authentication.isAuthenticated()){
                String email = user.getAttribute("email");
                User userByEmail = userRepository.findUserByEmail(email).orElse(null);

                if(userByEmail==null){
                    User newUser = new User();
                    newUser.setEmail(user.getAttribute("email"));
                    newUser.setLogin(user.getAttribute("name"));
                    newUser.setUid(user.getAttribute("sub"));
                    newUser.setPassword("");
                    newUser.setLock(false);
                    newUser.setEnabled(true);
                    newUser.setGoogle(true);
                    newUser.setRole(Role.USER);

                    this.saveUser(newUser);
                    userByEmail = newUser;
                }

                if(!userByEmail.getUid().equals(user.getAttribute("sub")) || !userByEmail.isGoogle()){
                    return ResponseEntity.status(401).body(new AuthResponse("Cant Authorize, probably gmail is already used"));
                }

                String tokenValue = jwtService.generateToken(user.getAttribute("name"), jwtExpiration);
                String refreshValue = jwtService.generateToken(user.getAttribute("name"), jwtRefreshExpiration);
                Cookie token = cookieService.generateCookie("token", tokenValue, jwtExpiration);
                Cookie refresh = cookieService.generateCookie("refresh", refreshValue, jwtRefreshExpiration);
                response.addCookie(token);
                response.addCookie(refresh);

                return ResponseEntity.ok(
                        UserRegisterDTO
                                .builder()
                                .login(userByEmail.getUsername())
                                .email(userByEmail.getEmail())
                                .role(userByEmail.getRole())
                                .id(userByEmail.getId())
                                .build());
            }else {
                return ResponseEntity.status(401).body(new AuthResponse("AUTHORIZATION WENT WRONG 2"));
            }
        }catch( AuthenticationException authenticationException){
            return ResponseEntity.status(500).body(new AuthResponse("AUTHORIZATION WENT WRONG 3"));
        }
    }
}
