package cn.czu.claimpaws.resource.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.resource.domain.Resource;
import cn.czu.claimpaws.resource.persistence.ResourceMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/campus")
    public ApiResponse<Map<String, Object>> listCampus(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("CAMPUS", null, offset, size, keyword);
        long total = resourceMapper.count("CAMPUS", null, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/campus/all")
    public ApiResponse<List<Resource>> allCampuses(
            @RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findAllByType("CAMPUS"), requestId);
    }

    @GetMapping("/buildings")
    public ApiResponse<Map<String, Object>> listBuildings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long campusId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("BUILDING", campusId, offset, size, keyword);
        long total = resourceMapper.count("BUILDING", campusId, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/buildings/by-campus/{campusId}")
    public ApiResponse<List<Resource>> buildingsByCampus(
            @PathVariable long campusId,
            @RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findByParentId(campusId, "BUILDING"), requestId);
    }

    @GetMapping("/floors")
    public ApiResponse<Map<String, Object>> listFloors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("FLOOR", buildingId, offset, size, keyword);
        long total = resourceMapper.count("FLOOR", buildingId, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/floors/by-building/{buildingId}")
    public ApiResponse<List<Resource>> floorsByBuilding(
            @PathVariable long buildingId,
            @RequestHeader("X-Request-Id") String requestId) {
        return ApiResponse.success(resourceMapper.findByParentId(buildingId, "FLOOR"), requestId);
    }

    @GetMapping("/rooms")
    public ApiResponse<Map<String, Object>> listRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long floorId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("ROOM", floorId, offset, size, keyword);
        long total = resourceMapper.count("ROOM", floorId, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/workstations")
    public ApiResponse<Map<String, Object>> listWorkstations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long floorId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("WORKSTATION", floorId, offset, size, keyword);
        long total = resourceMapper.count("WORKSTATION", floorId, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/facilities")
    public ApiResponse<Map<String, Object>> listFacilities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Resource> records = resourceMapper.findPage("FACILITY", null, offset, size, keyword);
        long total = resourceMapper.count("FACILITY", null, keyword);
        return buildPagedResponse(records, total, page, size, requestId);
    }

    @GetMapping("/{id}")
    public ApiResponse<Resource> getById(
            @PathVariable long id,
            @RequestHeader("X-Request-Id") String requestId) {
        Resource resource = resourceMapper.findById(id);
        if (resource == null) {
            return ApiResponse.failure("NOT_FOUND", "Resource not found", requestId);
        }
        return ApiResponse.success(resource, requestId);
    }

    @PostMapping
    public ApiResponse<Resource> create(
            @RequestBody Resource resource,
            @RequestHeader("X-Request-Id") String requestId) {
        resourceMapper.insert(resource);
        return ApiResponse.success(resource, requestId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Resource> update(
            @PathVariable long id,
            @RequestBody Resource resource,
            @RequestHeader("X-Request-Id") String requestId) {
        Resource existing = resourceMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("NOT_FOUND", "Resource not found", requestId);
        }
        Resource toUpdate = new Resource(
                id,
                resource.name(),
                resource.type(),
                resource.floor(),
                resource.building(),
                resource.capacity(),
                resource.description(),
                resource.active(),
                null,
                null,
                existing.deleted()
        );
        resourceMapper.update(toUpdate);
        Resource updated = resourceMapper.findById(id);
        return ApiResponse.success(updated, requestId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable long id,
            @RequestHeader("X-Request-Id") String requestId) {
        Resource existing = resourceMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("NOT_FOUND", "Resource not found", requestId);
        }
        resourceMapper.deleteById(id);
        return ApiResponse.success(null, requestId);
    }

    private ApiResponse<Map<String, Object>> buildPagedResponse(
            List<Resource> records, long total, int page, int size, String requestId) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return ApiResponse.success(data, requestId);
    }
}
