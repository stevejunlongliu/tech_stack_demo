package tech.demo.mysqldemo.rest;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tech.demo.mysqldemo.mapper.DeptMapper;
import tech.demo.mysqldemo.model.Dept;

import java.util.List;

@RestController  //  此注解指明该控制器直接返回数据，而不进行页面跳转
@RequestMapping("/dept")  //  定义路由信息
public class DeptController {

    @Autowired
    DeptMapper deptMapper;

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)  //  则次路由信息应该是/dept/findAll
    public List<Dept> findAll() {
        return deptMapper.findAll();
        // return deptService.findAll();
    }
}
