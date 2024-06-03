package michal.malek.slots.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserService {
    private final JwtService jwtService;

    public long getUserId(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie: cookies){
            System.out.println(cookie.getName());
            if(cookie.getName().equals("token") || cookie.getName().equals("refresh") ){
                    System.out.println(jwtService.getClaimUserId(cookie.getValue()));
                try {
                    return jwtService.getClaimUserId(cookie.getValue());
                }catch (Exception e){
                    System.out.println(e.getStackTrace());
                    return -1;
                }
            }
        }
        return -1;
    }
}
