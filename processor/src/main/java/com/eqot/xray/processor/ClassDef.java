package com.eqot.xray.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ClassDef {
    final String packageName;
    final String className;
    final List<MethodDef> constructors = new ArrayList<>();
    final List<MethodDef> methods = new ArrayList<>();

    ClassDef(String target) {
        final String[] words = target.split("\\.");
        String tmpPackageName = "";
        String tmpClassName = "";
        for (int i = 0, l = words.length; i < l; i++) {
            if (i < l - 1) {
                if (i != 0) {
                    tmpPackageName += ".";
                }
                tmpPackageName += words[i];
            } else {
                tmpClassName = words[i];
            }
        }
        packageName = tmpPackageName;
        className = tmpClassName;

        Class clazz = null;
        try {
            clazz = Class.forName(target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            return;
        }

        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            MethodDef methodDef = new MethodDef(constructor);
            constructors.add(methodDef);
        }

        for (Method method : clazz.getDeclaredMethods()) {
            MethodDef methodDef = new MethodDef(method);
            methods.add(methodDef);
        }
    }

    public class MethodDef {
        final String name;
        final boolean isStatic;
        final Class<?> returnType;
        final List<ParameterDef> parameters = new ArrayList<>();

        MethodDef(Method method) {
            name = method.getName();
            returnType = method.getReturnType();

            isStatic = (method.getModifiers() & Modifier.STATIC) != 0;

            for (Parameter parameter : method.getParameters()) {
                ParameterDef parameterDef = new ParameterDef(parameter);
                parameters.add(parameterDef);
            }
        }

        MethodDef(Constructor constructor) {
            name = constructor.getName();
            returnType = null;

            isStatic = false;

            for (Parameter parameter : constructor.getParameters()) {
                ParameterDef parameterDef = new ParameterDef(parameter);
                parameters.add(parameterDef);
            }
        }
    }

    public class ParameterDef {
        final Class<?> type;
        final String name;

        ParameterDef(Parameter parameter) {
            type = parameter.getType();
            name = parameter.getName();
        }
    }
}
