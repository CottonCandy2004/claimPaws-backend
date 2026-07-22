package cn.czu.claimpaws.resource.persistence;

import cn.czu.claimpaws.resource.domain.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceMapper {

    Resource requireActive(@Param("id") long id);

    List<Resource> findPage(@Param("type") String type, @Param("parentId") Long parentId, @Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);

    long count(@Param("type") String type, @Param("parentId") Long parentId, @Param("keyword") String keyword);
    long countByBuildingName(@Param("buildingName") String buildingName);

    List<Resource> findAllByType(@Param("type") String type);

    List<Resource> findByParentId(@Param("parentId") long parentId, @Param("type") String type);

    Resource findById(@Param("id") long id);

    int insert(Resource resource);

    int update(Resource resource);

    int deleteById(@Param("id") long id);
}
