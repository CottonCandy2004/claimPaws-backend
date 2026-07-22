package cn.czu.claimpaws.resource.persistence;

import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PolicyMapper {

    ReservationPolicy requireActiveByResourceId(@Param("resourceId") long resourceId);

    List<ReservationPolicy> findPage(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);

    long count(@Param("keyword") String keyword);

    ReservationPolicy findById(@Param("id") long id);

    ReservationPolicy findByResourceId(@Param("resourceId") long resourceId);

    int insert(ReservationPolicy policy);

    int update(ReservationPolicy policy);

    int deleteById(@Param("id") long id);
}
