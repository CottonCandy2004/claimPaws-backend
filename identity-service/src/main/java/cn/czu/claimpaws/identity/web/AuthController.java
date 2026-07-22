package cn.czu.claimpaws.identity.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.identity.application.AuthenticationService;
import cn.czu.claimpaws.identity.domain.User;
import cn.czu.claimpaws.identity.persistence.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authService;
    private final UserMapper userMapper;

    public AuthController(AuthenticationService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        try {
            String token = authService.login(body.get("username"), body.get("password"));
            return ResponseEntity.ok(ApiResponse.success(Map.of("accessToken", token), requestId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.failure("AUTH_FAILED", e.getMessage(), requestId));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> currentUser(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof String username)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.failure("UNAUTHORIZED", "Not authenticated", requestId));
        }
        return userMapper.findByUsername(username)
                .map(user -> {
                    var body = Map.<String, Object>of(
                            "id", user.id(),
                            "username", user.username(),
                            "displayName", user.displayName() != null ? user.displayName() : user.username(),
                            "roles", List.of("admin")
                    );
                    return ResponseEntity.ok(ApiResponse.success(body, requestId));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(ApiResponse.failure("UNAUTHORIZED", "User not found", requestId)));
    }
}
