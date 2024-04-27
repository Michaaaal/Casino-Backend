package michal.malek.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openEndpoints = List.of(
            "/auth/reset-password",
            "/auth/activate",
            "/auth/password-recovery",
            "/auth/change-password",
            "/auth/login",
            "/auth/google-login",
            "/auth/exchange-code",
            "/auth/auto-login",
            "/auth/logout",
            "/auth/logged-in",
            "/auth/register",
            "/auth/validate");

    public Predicate<ServerHttpRequest> isSecure =
            serverHttpRequest -> openEndpoints
                    .stream()
                    .noneMatch(uri->serverHttpRequest.getURI()
                            .getPath()
                            .contains(uri));
}
