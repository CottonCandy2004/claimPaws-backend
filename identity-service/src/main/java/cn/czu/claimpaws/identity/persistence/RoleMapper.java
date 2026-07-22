package cn.czu.claimpaws.identity.persistence;

import cn.czu.claimpaws.identity.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<Role> findByUserId(@Param("userId") long userId);

    List<Role> findAll();

    Role findById(@Param("id") long id);

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("id") long id);
}
