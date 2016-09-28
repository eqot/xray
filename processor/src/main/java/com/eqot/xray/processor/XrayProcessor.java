package com.eqot.xray.processor;

import com.eqot.xray.Xray;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.eqot.xray.Xray")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class XrayProcessor extends AbstractProcessor {
    private static final String POSTFIX_OF_DST_PACKAGE = ".xray";
    private static final String POSTFIX_OF_DST_CLASS = "$Xray";
    private static final String PREFIX_OF_PARAMETER = "param";

    private static final Map<String, String> CLASS_DEFAULTS = new HashMap<String, String>() {
        {
            put("boolean", "false");
            put("byte", "0");
            put("short", "0");
            put("int", "0");
            put("long", "0L");
            put("float", "0f");
            put("double", "0d");
            put("char", "'\u0000'");
            put("Integer", "0");
        }
    };

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        final List<ClassName> classNames = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Xray.class)) {
            final Xray xray = element.getAnnotation(Xray.class);
            if (xray == null) {
                continue;
            }

            ClassName className = null;
            try {
                xray.value();
            } catch (MirroredTypeException mte) {
                className = ClassName.bestGuess(mte.getTypeMirror().toString());
            }
            if (className == null) {
                continue;
            }

            if (!classNames.contains(className)) {
                classNames.add(className);
            }
        }

        for (ClassName className : classNames) {
            generateCode(className);
        }

        return true;
    }

    private void generateCode(ClassName srcClassName) {
        final ClassName dstClassName = ClassName.get(
                srcClassName.packageName() + POSTFIX_OF_DST_PACKAGE,
                srcClassName.simpleName() + POSTFIX_OF_DST_CLASS);

        final TypeSpec dstClass = buildClass(srcClassName, dstClassName);
        if (dstClass == null) {
            return;
        }

        try {
            final JavaFileObject source = processingEnv.getFiler().createSourceFile(
                    dstClassName.toString());
            final Writer writer = source.openWriter();

            JavaFile.builder(dstClassName.packageName(), dstClass)
                    .build()
                    .writeTo(writer);
//                    .writeTo(System.out);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec buildClass(ClassName srcClassName, ClassName dstClassName) {
        final Class clazz;
        try {
            clazz = Class.forName(srcClassName.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        final TypeSpec.Builder builder = TypeSpec.classBuilder(dstClassName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(clazz.getSuperclass())
                .addField(srcClassName, "mInstance", Modifier.PRIVATE, Modifier.FINAL)
                .addMethods(buildConstructors(clazz))
                .addMethods(buildSettersAndGetters(clazz))
                .addMethods(buildMethods(clazz))
                .addMethods(buildUtilityMethods(clazz));

        for (Class superinterface : clazz.getInterfaces()) {
            builder.addSuperinterface(superinterface);
        }

        return builder.build();
    }

    private List<MethodSpec> buildConstructors(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            int parameterIndex = 0;
            String combinedParameters = "";
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                final String parameterName = PREFIX_OF_PARAMETER + parameterIndex;
                builder.addParameter(parameterType, parameterName);

                if (combinedParameters.length() > 0) {
                    combinedParameters += ", ";
                }
                combinedParameters += parameterName;

                parameterIndex++;
            }

            builder.addStatement("mInstance = new $T($N)", clazz, combinedParameters);

            methods.add(builder.build());
        }

        return methods;
    }

    private List<MethodSpec> buildSettersAndGetters(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            final Class<?> fieldType = field.getType();

            final boolean isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());
            final List<Modifier> modifiers = new ArrayList<Modifier>() {{ add(Modifier.PUBLIC); }};
            if (isStatic) {
                modifiers.add(Modifier.STATIC);
            }
            final String instance = isStatic ? "null" : "mInstance";

            // Setter
            methods.add(MethodSpec.methodBuilder(field.getName())
                    .addModifiers(modifiers)
                    .addParameter(fieldType, PREFIX_OF_PARAMETER)

                    .beginControlFlow("try")
                    .addStatement("$T field = getField($S)", Field.class, field.getName())
                    .addStatement("field.set($N, $N)", instance, PREFIX_OF_PARAMETER)
                    .endControlFlow("catch (Exception e) {}")
                    .build());

            // Getter
            methods.add(MethodSpec.methodBuilder(field.getName())
                    .addModifiers(modifiers)
                    .returns(field.getType())

                    .addStatement("$T field = getField($S)", Field.class, field.getName())
                    .addStatement("return ($T) getObject(field, $N)", fieldType, instance)
                    .build());
        }

        return methods;
    }

    private List<MethodSpec> buildMethods(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            final Class<?> returnType = method.getReturnType();
            final String returnTypeDefault = getDefaultValue(returnType.getSimpleName());

            final MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(method.getReturnType());

            final boolean isStatic = java.lang.reflect.Modifier.isStatic(method.getModifiers());
            if (isStatic) {
                builder.addModifiers(Modifier.STATIC);
            }

            int parameterIndex = 0;
            String combinedParameters = "";
            String combinedParameterTypes = "";
            for (Class<?> parameterType : method.getParameterTypes()) {
                final String parameterName = "param" + parameterIndex;
                builder.addParameter(parameterType, parameterName);

                combinedParameters += ", " + parameterName;
                combinedParameterTypes += ", " + parameterType.getName() + ".class";

                parameterIndex++;
            }

            builder.addStatement("$T method = getMethod($S$N)",
                            Method.class, method.getName(), combinedParameterTypes);

            final String instance = isStatic ? "null" : "mInstance";
            final boolean hasReturn = !returnType.getSimpleName().equals("void");
            if (hasReturn) {
                builder.addStatement("$T result = $N", returnType, returnTypeDefault)
                        .beginControlFlow("try")
                        .addStatement("result = ($T) method.invoke($N$N)",
                                returnType, instance, combinedParameters)
                        .endControlFlow();
            } else {
                builder.beginControlFlow("try")
                        .addStatement("method.invoke($N$N)", instance, combinedParameters)
                        .endControlFlow();
            }

            builder.beginControlFlow("catch ($T e)", InvocationTargetException.class)
                    .addStatement("Throwable cause = e.getCause()");
            for (Class<?> exception : method.getExceptionTypes()) {
                builder.beginControlFlow("if (cause instanceof $T)", exception)
                        .addStatement("throw new $T(cause.getMessage())", exception)
                        .endControlFlow()
                        .addException(exception);
            }
            builder.endControlFlow();

            builder.beginControlFlow("catch ($T e)", IllegalAccessException.class)
                    .endControlFlow();

            if (hasReturn) {
                builder.addStatement("return result");
            }

            methods.add(builder.build());
        }

        return methods;
    }

    private List<MethodSpec> buildUtilityMethods(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        final ParameterSpec paramTypesSpec = ParameterSpec.builder(Class[].class, "paramTypes")
                .build();

        // getField()
        methods.add(MethodSpec.methodBuilder("getField")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String.class, "fieldName")
                .returns(Field.class)

                .addStatement("$T field = null", Field.class)
                .beginControlFlow("try")
                .addStatement("field = $T.class.getDeclaredField(fieldName)", clazz)
                .addStatement("field.setAccessible(true)")
                .endControlFlow("catch (Exception e) {}")
                .addStatement("return field")
                .build());

        // getObject()
        methods.add(MethodSpec.methodBuilder("getObject")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(Field.class, "field")
                .addParameter(clazz, "instance")
                .returns(Object.class)

                .addStatement("$T result = null", Object.class)
                .beginControlFlow("try")
                .addStatement("result = field.get(instance)")
                .endControlFlow("catch (Exception e) {}")
                .addStatement("return result")
                .build());

        // getMethod()
        methods.add(MethodSpec.methodBuilder("getMethod")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String.class, "methodName")
                .addParameter(paramTypesSpec).varargs(true)
                .returns(Method.class)

                .addStatement("$T method = null", Method.class)
                .beginControlFlow("try")
                .addStatement("method = $T.class.getDeclaredMethod(methodName, paramTypes)", clazz)
                .addStatement("method.setAccessible(true)")
                .endControlFlow("catch (Exception e) {}")
                .addStatement("return method")
                .build());

        return methods;
    }

    private String getDefaultValue(String className) {
        return CLASS_DEFAULTS.containsKey(className) ? CLASS_DEFAULTS.get(className) : "null";
    }

    @SuppressWarnings("unused")
    private void log(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
