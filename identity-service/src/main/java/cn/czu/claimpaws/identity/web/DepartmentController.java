package cn.czu.claimpaws.identity.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.identity.domain.Department;
import cn.czu.claimpaws.identity.persistence.DepartmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    public DepartmentController(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Department>>> tree(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        List<Department> departments = departmentMapper.findAll();
        return ResponseEntity.ok(ApiResponse.success(departments, requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getById(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Department dept = departmentMapper.findById(id);
        if (dept == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Department not found", requestId));
        }
        return ResponseEntity.ok(ApiResponse.success(dept, requestId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> create(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("INVALID_INPUT", "name is required", requestId));
        }
        Long parentId = body.get("parentId") != null
                ? ((Number) body.get("parentId")).longValue()
                : null;
        Department dept = new Department(null, name, parentId,
                body.get("sort") != null ? ((Number) body.get("sort")).intValue() : 0,
                null, null, false);
        departmentMapper.insert(dept);
        return ResponseEntity.ok(ApiResponse.success(dept, requestId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> update(
            @PathVariable long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Department existing = departmentMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Department not found", requestId));
        }
        Department updated = new Department(
                id,
                body.containsKey("name") ? (String) body.get("name") : existing.name(),
                body.containsKey("parentId") ? ((Number) body.get("parentId")).longValue() : existing.parentId(),
                body.get("sort") != null ? ((Number) body.get("sort")).intValue() : (existing.sort() != null ? existing.sort() : 0),
                existing.createdAt(),
                null,
                existing.deleted()
        );
        departmentMapper.update(updated);
        Department result = departmentMapper.findById(id);
        return ResponseEntity.ok(ApiResponse.success(result, requestId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable long id,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        Department existing = departmentMapper.findById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Department not found", requestId));
        }
        departmentMapper.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, requestId));
    }
}
