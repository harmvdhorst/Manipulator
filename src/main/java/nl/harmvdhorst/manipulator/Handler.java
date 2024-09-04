package nl.harmvdhorst.manipulator;

import nl.harmvdhorst.manipulator.annotations.Inject;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Handler {

    @Advice.OnMethodEnter
    public static void enter(@Advice.This Object thisObject, @Advice.Origin String origin, @Advice.AllArguments Object[] args){
        Method method = getMethod(Inject.At.HEAD, thisObject, origin);
        if(method != null){
            executeMethod(method, thisObject, origin, args);
        }
    }

    @Advice.OnMethodExit
    public static void exit(@Advice.This Object thisObject, @Advice.Origin String origin, @Advice.AllArguments Object[] args){
        Method method = getMethod(Inject.At.TAIL, thisObject, origin);
        if(method != null){
            executeMethod(method, thisObject, origin, args);
        }
    }

    public static void executeMethod(Method method, Object thisObject, String origin, Object[] args){
        try {
            if(method.getParameterCount() > 0 && method.getParameters()[0].getType().equals(MethodData.class)){
                MethodData data = new MethodData(thisObject, origin, args);
                method.invoke(null, data);
            } else {
                method.invoke(null);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Inject.At at, Object thisObject, String method){
        String name = thisObject.getClass().getSimpleName();

        String[] methodString = method.split("\\.");
        String newMethod = methodString[methodString.length - 1].split("\\(")[0];

        if(Manipulator.METHODS_MAP.containsKey(at)){
            if(Manipulator.METHODS_MAP.get(at).containsKey(name)){
                if(Manipulator.METHODS_MAP.get(at).get(name).containsKey(newMethod)){
                    Method methodObject = Manipulator.METHODS_MAP.get(at).get(name).get(newMethod);
                    methodObject.setAccessible(true);
                    return methodObject;
                }
            }
        }
        return null;
    }

}
