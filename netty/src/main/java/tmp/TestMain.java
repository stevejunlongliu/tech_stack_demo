package tmp;

import httpserver.core.Result;

import java.lang.reflect.InvocationTargetException;

public class TestMain {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TestClass a = new TestClass();
        TestClass.class.getMethod("test1", Result.class).invoke(a, null);
    }
}
