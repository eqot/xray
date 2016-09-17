package com.eqot.xray.processor;

import com.eqot.xray.Xray;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private static final ClassName CLASS_NAME_METHOD = ClassName.bestGuess("java.lang.reflect.Method");
    private static final ClassName CLASS_NAME_FIELD = ClassName.bestGuess("java.lang.reflect.Field");

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
            if (className == null || className.toString().equals("java.lang.Object")) {
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
        final Class clazz = getClass(srcClassName.toString());

        return TypeSpec.classBuilder(dstClassName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addField(srcClassName, "mInstance", Modifier.PRIVATE, Modifier.FINAL)
                .addMethods(buildConstructors(clazz))
                .addMethods(buildSettersAndGetters(clazz))
                .addMethods(buildMethods(clazz))
                .build();
    }

    private Class getClass(String classNameString) {
        Class clazz = null;
        try {
            clazz = Class.forName(classNameString);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }

    private List<MethodSpec> buildConstructors(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            int parameterIndex = 0;
            String combinedParameters = "";
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                final String parameterName = "param" + parameterIndex;
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
            final String fieldName = field.getName() + POSTFIX_OF_DST_CLASS;
            final Class<?> fieldType = field.getType();
            final String fieldTypeDefault = getDefaultValue(field.getType().getSimpleName());

            // Setter
            methods.add(MethodSpec.methodBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fieldType, field.getName())
                    .beginControlFlow("try")
                    .addStatement("$T field = mInstance.getClass().getDeclaredField($S)",
                            CLASS_NAME_FIELD, field.getName())
                    .addStatement("field.setAccessible(true)")
                    .addStatement("field.set(mInstance, $N)", field.getName())
                    .endControlFlow("catch (Exception e) {}")
                    .build());

            // Getter
            methods.add(MethodSpec.methodBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(field.getType())
                    .addStatement("$T result = $N", fieldType, fieldTypeDefault)
                    .beginControlFlow("try")
                    .addStatement("$T field = mInstance.getClass().getDeclaredField($S)",
                            CLASS_NAME_FIELD, field.getName())
                    .addStatement("field.setAccessible(true)")
                    .addStatement("result = ($T) field.get(mInstance)", fieldType)
                    .endControlFlow("catch (Exception e) {}")
                    .addStatement("return result")
                    .build());
        }

        return methods;
    }

    private List<MethodSpec> buildMethods(Class clazz) {
        final List<MethodSpec> methods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            final Class<?> returnType = method.getReturnType();
            final String returnTypeDefault = getDefaultValue(returnType.getSimpleName());
            final boolean hasReturn = !returnType.getSimpleName().equals("void");

            final MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(method.getReturnType());

            final boolean isStatic = (method.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0;
            if (isStatic) {
                builder.addModifiers(Modifier.STATIC);
            }

            if (hasReturn) {
                builder.addStatement("$T result = $N", returnType, returnTypeDefault);
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

            builder
                    .beginControlFlow("try")
                    .addStatement("$T method = $T.class.getDeclaredMethod($S$N)",
                            CLASS_NAME_METHOD, clazz, method.getName(), combinedParameterTypes)
                    .addStatement("method.setAccessible(true)");

            final String instance = isStatic ? "null" : "mInstance";

            if (hasReturn) {
                builder.addStatement("result = ($T) method.invoke($N$N)",
                        returnType, instance, combinedParameters);
            } else {
                builder.addStatement("method.invoke(mInstance$N)", combinedParameters);
            }

            builder.endControlFlow("catch (Exception e) {}");

            if (hasReturn) {
                builder.addStatement("return result");
            }

            methods.add(builder.build());
        }

        return methods;
    }

    private String getDefaultValue(String className) {
        return CLASS_DEFAULTS.containsKey(className) ? CLASS_DEFAULTS.get(className) : "null";
    }

    private void log(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
