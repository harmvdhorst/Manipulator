package nl.harmvdhorst.manipulator;

import nl.harmvdhorst.manipulator.annotations.Inject;
import nl.harmvdhorst.manipulator.annotations.Overwrite;
import nl.harmvdhorst.manipulator.annotations.Patch;
import nl.harmvdhorst.manipulator.annotations.Redirect;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class Manipulator {

    private final Instrumentation instrumentation;
    private final Set<Class<?>> patches;

    public static final Map<Inject.At, Map<String, Map<String, Method>>> METHODS_MAP = new HashMap<>();

    public Manipulator(Instrumentation instrumentation, Set<Class<?>> patches){
        this.instrumentation = instrumentation;
        this.patches = patches;
        this.patch();
    }

    public Manipulator(Instrumentation instrumentation, Class<?>... patches){
        this.instrumentation = instrumentation;
        this.patches = new HashSet<>(Arrays.asList(patches));
        this.patch();
    }

    public Manipulator(Instrumentation instrumentation, List<Class<?>> patches){
        this.instrumentation = instrumentation;
        this.patches = new HashSet<>(patches);
        this.patch();
    }


    private void patch(){

        try {
            Advice advice = Advice.to(Handler.class);

            patches.forEach(patchClass -> {

                if(patchClass.isAnnotationPresent(Patch.class)){
                    Class<?> patchType = patchClass.getAnnotation(Patch.class).type();
                    String patchName = patchClass.getAnnotation(Patch.class).name();

                    if(patchName.isEmpty()){
                        patchName = patchType.getSimpleName();
                    }

                    AgentBuilder.Identified.Extendable builder = new AgentBuilder.Default()
                            .disableClassFormatChanges()
                            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                            .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                            .type(named(patchName))
                            .transform((tempBuilder, typeDescription, classLoader, javaModule, protectionDomain) -> tempBuilder);

                    for (Method method : patchClass.getMethods()) {
                        method.setAccessible(true);

                        Class<?>[] args = method.getParameterTypes();

                        if(method.isAnnotationPresent(Overwrite.class)){
                            builder = builder.transform((overwriteBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                                    overwriteBuilder
                                            .method(named(method.getName()).and(takesArguments(args)))
                                            .intercept(MethodDelegation.to(patchClass))
                            );
                        }
                        if(method.isAnnotationPresent(Inject.class)){
                            Inject.At at = method.getAnnotation(Inject.class).at();
                            METHODS_MAP.putIfAbsent(at, new HashMap<>());
                            METHODS_MAP.get(at).putIfAbsent(patchName, new HashMap<>());
                            METHODS_MAP.get(at).get(patchName).putIfAbsent(method.getAnnotation(Inject.class).method(), method);
                            builder = builder.transform((injectBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                                    injectBuilder
                                            .visit(advice.on(named(method.getAnnotation(Inject.class).method()).and(takesArguments(args))))
                            );
                        }
                        if(method.isAnnotationPresent(Redirect.class)){
                            builder = builder.transform((overwriteBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                                    {
                                        try {
                                            return overwriteBuilder
                                                    .method(named(method.getName()).and(takesArguments(args)))
                                                    .intercept(FixedValue.value(method.invoke(null)));
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );
                        }
                    }

                    builder.installOn(instrumentation);

                }

            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
