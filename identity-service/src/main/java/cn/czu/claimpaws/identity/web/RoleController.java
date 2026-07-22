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
    public ResponseEntity<ApiResponse<List<Role>>> list(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        List<Role> roles = roleMapper.findAll();
        return ResponseEntity.ok(ApiResponse.success(roles, requestId));
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
        Role role = new Role(null, name, (String) body.get("description"), null, null, false);
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
