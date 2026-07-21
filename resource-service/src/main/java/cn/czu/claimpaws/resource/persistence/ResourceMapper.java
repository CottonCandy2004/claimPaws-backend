package cn.czu.claimpaws.resource.persistence;

import cn.czu.claimpaws.resource.domain.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ResourceMapper {

    Resource requireActive(@Param("id") long id);
}
