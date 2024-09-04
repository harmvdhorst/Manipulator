package nl.harmvdhorst.manipulator.utils;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static void injectDynamicTypeIntoSystemClassloader(DynamicType type){
        Map<String, byte[]> map = new HashMap<>();
        map.putIfAbsent(type.getClass().getSimpleName(), type.getBytes());
        ClassInjector.UsingReflection.ofSystemClassLoader().injectRaw(map);
    }

}
