package cn.czu.claimpaws.identity.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.identity.application.AuthenticationService;
import cn.czu.claimpaws.identity.domain.Role;
import cn.czu.claimpaws.identity.persistence.RoleMapper;
import cn.czu.claimpaws.identity.persistence.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
        "campus:read", "campus:write",
        "building:read", "building:write",
        "floor:read", "floor:write",
        "room:read", "room:write",
        "workstation:read", "workstation:write",
        "facility:read", "facility:write",
        "policy:read", "policy:write",
        "reservation:read", "reservation:write",
        "reservation:approve",
        "user:read", "user:write",
        "role:read", "role:write",
        "department:read", "department:write",
        "webhook:read", "webhook:write"
    );

    private final AuthenticationService authService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AuthController(AuthenticationService authService, UserMapper userMapper, RoleMapper roleMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
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
            return ResponseEntity.ok(ApiResponse.failure("AUTH_FAILED", e.getMessage(), requestId));
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
                    List<Role> roles = roleMapper.findByUserId(user.id());
                    List<String> roleNames = roles.stream().map(Role::name).toList();
                    List<Long> roleIds = roles.stream().map(Role::id).toList();
                    List<String> permissions = roleNames.contains("admin")
                            ? new ArrayList<>(ADMIN_PERMISSIONS)
                            : List.of("reservation:read", "reservation:write");
                    var body = new HashMap<String, Object>();
                    body.put("id", user.id());
                    body.put("username", user.username());
                    body.put("displayName", user.displayName() != null ? user.displayName() : user.username());
                    body.put("roles", roleNames);
                    body.put("roleIds", roleIds);
                    body.put("permissions", permissions);
                    return ResponseEntity.ok(ApiResponse.success(body, requestId));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(ApiResponse.failure("UNAUTHORIZED", "User not found", requestId)));
    }
}
