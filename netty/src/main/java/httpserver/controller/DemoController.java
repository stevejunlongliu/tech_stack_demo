package httpserver.controller;

import httpserver.annotation.RequestMapping;
import httpserver.annotation.RequestMethod;
import httpserver.core.Request;
import httpserver.core.Result;
import httpserver.model.entry.TestEntry;

@RequestMapping(value = "/demo", method = {RequestMethod.GET})
//@ResponseDataType(ResponseDataTypeElement.JSON)
public class DemoController {

    /**
     * 用于获取外部群组的信息
     */
    @RequestMapping(value = "t1")
    public Result groupInfo(Request<TestEntry> entry) throws Exception {
        //sRequest<TestEntry> request
        Object x = entry.getData();
        Result result = new Result();
        result.build("haah");
        System.out.println("demo t1 working ");
        return result;
    }


    /**
     * 测试
     */
    @RequestMapping(value = "t2")
    public void t2() throws Exception {
        //sRequest<TestEntry> request

        System.out.println("demo t2 working ");

    }

//    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        TestController a = new TestController();
//        TestController.class.getMethod("groupInfo").invoke(a, null);
//    }
}
