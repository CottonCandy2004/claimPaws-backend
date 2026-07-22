package cn.czu.claimpaws.identity.persistence;

import cn.czu.claimpaws.identity.domain.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    List<Department> findAll();

    Department findById(@Param("id") long id);

    int insert(Department department);

    int update(Department department);

    int deleteById(@Param("id") long id);
}
