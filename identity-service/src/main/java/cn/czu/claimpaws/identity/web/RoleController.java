package cn.czu.claimpaws.identity.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.identity.domain.Role;
import cn.czu.claimpaws.identity.persistence.RoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleMapper roleMapper;

    public RoleController(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        List<Role> roles = roleMapper.findAll();
        int start = (page - 1) * size;
        int end = Math.min(start + size, roles.size());
        var body = Map.<String, Object>of("records", roles.subList(Math.min(start, roles.size()), end), "total", roles.size(), "page", page, "size", size);
        return ResponseEntity.ok(ApiResponse.success(body, requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getById(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Role role = roleMapper.findById(id);
        if (role == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Role not found", requestId));
        }
        return ResponseEntity.ok(ApiResponse.success(role, requestId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> create(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("INVALID_INPUT", "name is required", requestId));
        }
        Role role = new Role(null, name, (String) body.get("code"), (String) body.get("description"), null, null, false);
        roleMapper.insert(role);
        return ResponseEntity.ok(ApiResponse.success(role, requestId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> update(
            @PathVariable long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Role existing = roleMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Role not found", requestId));
        }
        Role updated = new Role(
                id,
                body.containsKey("name") ? (String) body.get("name") : existing.name(),
                body.containsKey("code") ? (String) body.get("code") : existing.code(),
                body.containsKey("description") ? (String) body.get("description") : existing.description(),
                existing.createdAt(),
                null,
                existing.deleted()
        );
        roleMapper.update(updated);
        Role result = roleMapper.findById(id);
        return ResponseEntity.ok(ApiResponse.success(result, requestId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Role existing = roleMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Role not found", requestId));
        }
        roleMapper.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, requestId));
    }
}
