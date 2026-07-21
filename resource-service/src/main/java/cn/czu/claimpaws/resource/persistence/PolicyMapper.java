package cn.czu.claimpaws.resource.persistence;

import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyMapper {

    ReservationPolicy requireActiveByResourceId(@Param("resourceId") long resourceId);
}
