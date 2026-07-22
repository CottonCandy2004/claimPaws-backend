package cn.czu.claimpaws.identity.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.identity.domain.Department;
import cn.czu.claimpaws.identity.domain.User;
import cn.czu.claimpaws.identity.persistence.DepartmentMapper;
import cn.czu.claimpaws.identity.persistence.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper,
                          DepartmentMapper departmentMapper,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        int offset = (page - 1) * size;
        List<User> users = userMapper.findPage(offset, size, keyword);
        long total = userMapper.count(keyword);

        Map<Long, String> deptMap = new HashMap<>();
        for (User u : users) {
            if (u.departmentId() != null && !deptMap.containsKey(u.departmentId())) {
                Department dept = departmentMapper.findById(u.departmentId());
                if (dept != null) {
                    deptMap.put(u.departmentId(), dept.name());
                }
            }
        }

        List<Map<String, Object>> records = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.id());
            m.put("username", u.username());
            m.put("displayName", u.displayName());
            m.put("email", u.email());
            m.put("phone", u.phone());
            m.put("departmentId", u.departmentId());
            m.put("departmentName", deptMap.getOrDefault(u.departmentId(), null));
            m.put("enabled", u.enabled());
            m.put("createdAt", u.createdAt());
            m.put("updatedAt", u.updatedAt());
            return m;
        }).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);

        return ResponseEntity.ok(ApiResponse.success(data, requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        User user = userMapper.findById(id);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "User not found", requestId));
        }

        String deptName = null;
        if (user.departmentId() != null) {
            Department dept = departmentMapper.findById(user.departmentId());
            if (dept != null) {
                deptName = dept.name();
            }
        }

        Map<String, Object> m = new HashMap<>();
        m.put("id", user.id());
        m.put("username", user.username());
        m.put("displayName", user.displayName());
        m.put("email", user.email());
        m.put("phone", user.phone());
        m.put("departmentId", user.departmentId());
        m.put("departmentName", deptName);
        m.put("enabled", user.enabled());
        m.put("createdAt", user.createdAt());
        m.put("updatedAt", user.updatedAt());

        return ResponseEntity.ok(ApiResponse.success(m, requestId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("INVALID_INPUT", "username and password are required", requestId));
        }

        User existing = userMapper.findByUsername(username).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(ApiResponse.failure("CONFLICT", "Username already exists", requestId));
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(
                null,
                username,
                hashedPassword,
                (String) body.get("displayName"),
                (String) body.get("email"),
                (String) body.get("phone"),
                body.get("departmentId") != null ? ((Number) body.get("departmentId")).longValue() : null,
                true,
                null,
                null,
                false
        );
        userMapper.insert(user);
        User created = userMapper.findByUsername(username).orElse(null);
        if (created == null) {
            return ResponseEntity.ok(ApiResponse.failure("INTERNAL_ERROR", "Failed to create user", requestId));
        }

        return getById(created.id(), request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(
            @PathVariable long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        User existing = userMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "User not found", requestId));
        }

        User updated = new User(
                id,
                existing.username(),
                existing.passwordHash(),
                body.containsKey("displayName") ? (String) body.get("displayName") : existing.displayName(),
                body.containsKey("email") ? (String) body.get("email") : existing.email(),
                body.containsKey("phone") ? (String) body.get("phone") : existing.phone(),
                body.containsKey("departmentId") ? ((Number) body.get("departmentId")).longValue() : existing.departmentId(),
                body.containsKey("enabled") ? (Boolean) body.get("enabled") : existing.enabled(),
                existing.createdAt(),
                null,
                existing.deleted()
        );
        userMapper.update(updated);

        return getById(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        User existing = userMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "User not found", requestId));
        }
        userMapper.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, requestId));
    }
}
