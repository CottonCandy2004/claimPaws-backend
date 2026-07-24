package cn.czu.claimpaws.identity.persistence;

import cn.czu.claimpaws.identity.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    User requireActive(@Param("id") long id);

    Optional<User> findByUsername(@Param("username") String username);

    void insert(User user);
    int insertUserRole(@Param("userId") long userId, @Param("roleId") long roleId);
    int deleteUserRoles(@Param("userId") long userId);

    List<User> findPage(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);

    long count(@Param("keyword") String keyword);

    User findById(@Param("id") long id);

    int update(User user);

    int deleteById(@Param("id") long id);
}
