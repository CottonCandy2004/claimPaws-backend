package cn.czu.claimpaws.resource.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.resource.domain.Resource;
import cn.czu.claimpaws.resource.persistence.ResourceMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourcePublicController {

    private final ResourceMapper resourceMapper;

    public ResourcePublicController(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    // ---- campus ----
    @GetMapping("/campus")
    public ApiResponse<Map<String, Object>> listCampus(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("CAMPUS", null, page, size, keyword, requestId);
    }

    @GetMapping("/campus/all")
    public ApiResponse<List<Resource>> allCampuses(@RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findAllByType("CAMPUS"), requestId);
    }

    @PostMapping("/campus")
    public ApiResponse<Resource> createCampus(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("CAMPUS", body, requestId);
    }

    @PutMapping("/campus/{id}")
    public ApiResponse<Resource> updateCampus(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/campus/{id}")
    public ApiResponse<Void> deleteCampus(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- buildings ----
    @GetMapping("/buildings")
    public ApiResponse<Map<String, Object>> listBuildings(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long campusId, @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("BUILDING", campusId, page, size, keyword, requestId);
    }

    @GetMapping("/buildings/by-campus/{campusId}")
    public ApiResponse<List<Resource>> buildingsByCampus(@PathVariable long campusId, @RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findByParentId(campusId, "BUILDING"), requestId);
    }

    @PostMapping("/buildings")
    public ApiResponse<Resource> createBuilding(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("BUILDING", body, requestId);
    }

    @PutMapping("/buildings/{id}")
    public ApiResponse<Resource> updateBuilding(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/buildings/{id}")
    public ApiResponse<Void> deleteBuilding(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- floors ----
    @GetMapping("/floors")
    public ApiResponse<Map<String, Object>> listFloors(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long buildingId, @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("FLOOR", buildingId, page, size, keyword, requestId);
    }

    @GetMapping("/floors/by-building/{buildingId}")
    public ApiResponse<List<Resource>> floorsByBuilding(@PathVariable long buildingId, @RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findByParentId(buildingId, "FLOOR"), requestId);
    }

    @PostMapping("/floors")
    public ApiResponse<Resource> createFloor(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("FLOOR", body, requestId);
    }

    @PutMapping("/floors/{id}")
    public ApiResponse<Resource> updateFloor(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/floors/{id}")
    public ApiResponse<Void> deleteFloor(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- rooms ----
    @GetMapping("/rooms")
    public ApiResponse<Map<String, Object>> listRooms(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long floorId, @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("ROOM", floorId, page, size, keyword, requestId);
    }

    @PostMapping("/rooms")
    public ApiResponse<Resource> createRoom(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("ROOM", body, requestId);
    }

    @PutMapping("/rooms/{id}")
    public ApiResponse<Resource> updateRoom(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/rooms/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- workstations ----
    @GetMapping("/workstations")
    public ApiResponse<Map<String, Object>> listWorkstations(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long floorId, @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("WORKSTATION", floorId, page, size, keyword, requestId);
    }

    @PostMapping("/workstations")
    public ApiResponse<Resource> createWorkstation(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("WORKSTATION", body, requestId);
    }

    @PutMapping("/workstations/{id}")
    public ApiResponse<Resource> updateWorkstation(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/workstations/{id}")
    public ApiResponse<Void> deleteWorkstation(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- facilities ----
    @GetMapping("/facilities")
    public ApiResponse<Map<String, Object>> listFacilities(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        return listByType("FACILITY", null, page, size, keyword, requestId);
    }

    @PostMapping("/facilities")
    public ApiResponse<Resource> createFacility(@RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return createResource("FACILITY", body, requestId);
    }

    @PutMapping("/facilities/{id}")
    public ApiResponse<Resource> updateFacility(@PathVariable long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Request-Id") String requestId) {
        return updateResource(id, body, requestId);
    }

    @DeleteMapping("/facilities/{id}")
    public ApiResponse<Void> deleteFacility(@PathVariable long id, @RequestHeader("X-Request-Id") String requestId) {
        return deleteResource(id, requestId);
    }

    // ---- generic CRUD ----
    private ApiResponse<Map<String, Object>> listByType(String type, Long parentId, int page, int size, String keyword, String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage(type, parentId, offset, size, keyword);
        long total = resourceMapper.count(type, parentId, keyword);
        List<Map<String, Object>> mapped = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.id());
            m.put("name", r.name());
            m.put("type", r.type());
            m.put("floor", r.floor());
            m.put("building", r.building());
            m.put("capacity", r.capacity());
            m.put("description", r.description());
            m.put("active", r.active());
            m.put("createdAt", r.createdAt() != null ? r.createdAt().toString() : null);
            m.put("updatedAt", r.updatedAt() != null ? r.updatedAt().toString() : null);
            if ("CAMPUS".equals(type)) m.put("address", r.description());
            if ("BUILDING".equals(type)) {
                m.put("campusName", r.building());
                m.put("campusId", resourceMapper.findIdByNameAndType(r.building(), "CAMPUS"));
                m.put("floorCount", resourceMapper.countByBuildingName(r.name()));
            }
            if ("FLOOR".equals(type) || "ROOM".equals(type) || "WORKSTATION".equals(type)) {
                m.put("buildingName", r.building());
                m.put("buildingId", resourceMapper.findIdByNameAndType(r.building(), "BUILDING"));
            }
            if ("ROOM".equals(type) || "WORKSTATION".equals(type)) {
                m.put("floorName", r.floor());
                m.put("floorId", resourceMapper.findIdByNameAndType(r.floor(), "FLOOR"));
            }
            if ("FLOOR".equals(type)) {
                String s = r.description();
                m.put("sort", s != null && !s.isEmpty() ? Integer.parseInt(s) : 0);
            }
            return m;
        }).toList();
        Map<String, Object> data = new HashMap<>();
        data.put("records", mapped);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return ApiResponse.success(data, requestId);
    }

    private ApiResponse<Resource> createResource(String type, Map<String, Object> body, String requestId) {
        String name = (String) body.getOrDefault("name", "");
        String building = resolveParentName(type, body);
        String floor = "FLOOR".equals(type) ? resolveParentName(type, body) : "";
        String desc = "CAMPUS".equals(type) ? (String) body.getOrDefault("address", "")
                : "FLOOR".equals(type) ? String.valueOf(body.getOrDefault("sort", 0))
                : (String) body.getOrDefault("description", "");
        Integer capacity = body.get("capacity") != null ? ((Number) body.get("capacity")).intValue() : null;
        Resource resource = new Resource(null, name, type,
                "FLOOR".equals(type) || "ROOM".equals(type) || "WORKSTATION".equals(type) ? floor : "",
                !"CAMPUS".equals(type) ? building : "",
                capacity, desc, true, null, null, false);
        resourceMapper.insert(resource);
        return ApiResponse.success(resource, requestId);
    }

    private String resolveParentName(String type, Map<String, Object> body) {
        String parentKey = switch (type) {
            case "BUILDING" -> "campusId";
            case "FLOOR" -> "buildingId";
            case "ROOM", "WORKSTATION" -> "floorId";
            default -> null;
        };
        if (parentKey != null && body.get(parentKey) != null) {
            Long parentId = Long.valueOf(body.get(parentKey).toString());
            Resource parent = resourceMapper.findById(parentId);
            return parent != null ? parent.name() : "";
        }
        return (String) body.getOrDefault("building", "");
    }

    private ApiResponse<Resource> updateResource(long id, Map<String, Object> body, String requestId) {
        Resource existing = resourceMapper.findById(id);
        if (existing == null) return ApiResponse.failure("NOT_FOUND", "Resource not found", requestId);
        String building = resolveParentName(existing.type(), body);
        if ("CAMPUS".equals(existing.type())) building = "";
        String desc = "CAMPUS".equals(existing.type()) && body.containsKey("address")
                ? (String) body.getOrDefault("address", existing.description())
                : "FLOOR".equals(existing.type()) && body.containsKey("sort")
                ? String.valueOf(body.getOrDefault("sort", 0))
                : (String) body.getOrDefault("description", existing.description());
        Resource toUpdate = new Resource(id,
                (String) body.getOrDefault("name", existing.name()),
                existing.type(),
                (String) body.getOrDefault("floor", existing.floor()),
                !building.isEmpty() ? building : existing.building(),
                body.get("capacity") != null ? ((Number) body.get("capacity")).intValue()
                        : (existing.capacity() != null ? existing.capacity() : 0),
                desc,
                existing.active(), null, null, existing.deleted());
        resourceMapper.update(toUpdate);
        return ApiResponse.success(resourceMapper.findById(id), requestId);
    }

    private ApiResponse<Void> deleteResource(long id, String requestId) {
        if (resourceMapper.findById(id) == null) return ApiResponse.failure("NOT_FOUND", "Resource not found", requestId);
        resourceMapper.deleteById(id);
        return ApiResponse.success(null, requestId);
    }
}
