package dev.rosemarylab.dataquerying.internal.utils;

import dev.rosemarylab.dataquerying.api.exceptions.SpecificationBuilderException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static <T> List<String> getAttributeNames(Class<T> targetType) throws SpecificationBuilderException {
        if (targetType.isInterface()) {
            if (Arrays.stream(targetType.getDeclaredMethods()).noneMatch(method -> (isNonBooleanGetter(method) || isBooleanGetter(method)))) {
                throw new SpecificationBuilderException("Target interface doesn't contain any getter.");
            }
            return Arrays.stream(targetType.getDeclaredMethods())
                    .filter(method -> (isNonBooleanGetter(method) || isBooleanGetter(method)))
                    .map(ReflectionUtils::getFieldNameFromGetter)
                    .toList();
        }

        if (targetType.getDeclaredFields().length == 0)
            throw new SpecificationBuilderException("Target class doesn't contain any attribute.");
        return Arrays.stream(targetType.getDeclaredFields())
                .map(Field::getName)
                .toList();
    }

    private static String getFieldNameFromGetter(Method method) {
        String name;
        if (isBooleanGetter(method))
            name = method.getName().substring(2);
        else
            name = method.getName().substring(3);
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static boolean isNonBooleanGetter(Method method) {
        return method.getName().startsWith("get") && method.getParameterCount() == 0;
    }

    private static boolean isBooleanGetter(Method method) {
        return method.getName().startsWith("is")
                && (method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class)
                && method.getParameterCount() == 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceType, Map<String, Object> data) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                new DynamicInvocationHandler(data)
        );
    }

    private record DynamicInvocationHandler(Map<String, Object> data) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String fieldName = getFieldNameFromMethod(method);
            return data.get(fieldName);
        }

        private String getFieldNameFromMethod(Method method) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
            return methodName;
        }
    }
}
