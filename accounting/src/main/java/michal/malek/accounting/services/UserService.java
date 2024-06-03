package michal.malek.accounting.services;

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
            if(cookie.getName().equals("token") || cookie.getName().equals("refresh") ){
                try {
                    return jwtService.getClaimUserId(cookie.getValue());
                }catch (Exception e){
                    return -1;
                }
            }
        }
        return -1;
    }
}
