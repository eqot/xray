package com.eqot.xray.processor;

import com.squareup.javapoet.ClassName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ClassDef {
    final ClassName className;
    final List<MethodDef> constructors = new ArrayList<>();
    final List<MethodDef> methods = new ArrayList<>();
    final List<FieldDef> fields = new ArrayList<>();

    ClassDef(String target) {
        className = ClassName.bestGuess(target);

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

        for (Field field : clazz.getDeclaredFields()) {
            FieldDef fieldDef = new FieldDef(field);
            fields.add(fieldDef);
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

            addParameters(method.getParameters());
        }

        MethodDef(Constructor constructor) {
            name = constructor.getName();
            returnType = null;
            isStatic = false;

            addParameters(constructor.getParameters());
        }

        private void addParameters(Parameter[] parameters) {
            for (Parameter parameter : parameters) {
                ParameterDef parameterDef = new ParameterDef(parameter);
                this.parameters.add(parameterDef);
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

    public class FieldDef {
        final Class<?> type;
        final String name;

        FieldDef(Field field) {
            type = field.getType();
            name = field.getName();
        }
    }
}
