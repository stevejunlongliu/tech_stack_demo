package tech.demo.mysqldemo.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import tech.demo.mysqldemo.model.Dept;

import java.util.List;

@Mapper
@Repository
public interface DeptMapper {
    List<Dept> findAll();

    void addDept(Dept dept);
}
