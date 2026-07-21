package cn.czu.claimpaws.identity.persistence;

import cn.czu.claimpaws.identity.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    User requireActive(@Param("id") long id);

    Optional<User> findByUsername(@Param("username") String username);

    void insert(User user);
}
