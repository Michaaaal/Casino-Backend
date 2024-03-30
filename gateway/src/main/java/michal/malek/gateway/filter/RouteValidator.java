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
            "/auth/retrievePassword",
            "/auth/login",
            "/auth/loginPost",
            "/auth/register",
            "/auth/validate");

    public Predicate<ServerHttpRequest> isSecure =
            serverHttpRequest -> openEndpoints
                    .stream()
                    .noneMatch(uri->serverHttpRequest.getURI()
                            .getPath()
                            .contains(uri));
}
