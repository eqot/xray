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
import java.lang.reflect.Parameter;
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
        Class clazz = null;
        try {
            clazz = Class.forName(srcClassName.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            return null;
        }

        final ClassDef classDef = new ClassDef(srcClassName.toString());

        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(dstClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);

        classBuilder
                .addField(srcClassName, "mInstance", Modifier.PRIVATE, Modifier.FINAL)
                .addMethods(buildConstructors(clazz))
                .addMethods(buildSetterAndGetter(clazz));

        final MethodSpec.Builder initializerBuilder = MethodSpec.methodBuilder("initialize")
                .addModifiers(Modifier.PRIVATE);
        final MethodSpec.Builder initializerStaticBuilder = MethodSpec.methodBuilder("initializeStatic")
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.STATIC);

        for (ClassDef.MethodDef methodDef : classDef.methods) {
            String combinedMethodName = "_" + methodDef.name;
            String argTypes = "";
            String argNames = "";
            for (ClassDef.ParameterDef parameterDef : methodDef.parameters) {
                combinedMethodName += "_" + parameterDef.type.getSimpleName();
                argTypes += ", " + parameterDef.type.getName() + ".class";
                argNames += ", " + parameterDef.name;
            }

            if (methodDef.isStatic) {
                initializerStaticBuilder
                        .beginControlFlow("try")
                        .addStatement("$N = $T.class.getDeclaredMethod($S$N)",
                                combinedMethodName, srcClassName, methodDef.name, argTypes)
                        .addStatement("$N.setAccessible(true)", combinedMethodName)
                        .endControlFlow("catch (Exception e) {}");
            } else {
                initializerBuilder
                        .beginControlFlow("try")
                        .addStatement("$N = $T.class.getDeclaredMethod($S$N)",
                                combinedMethodName, srcClassName, methodDef.name, argTypes)
                        .addStatement("$N.setAccessible(true)", combinedMethodName)
                        .endControlFlow("catch (Exception e) {}");
            }

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodDef.name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodDef.returnType);

            if (methodDef.isStatic) {
                methodSpecBuilder
                        .addModifiers(Modifier.STATIC);
            }

            for (ClassDef.ParameterDef parameterDef : methodDef.parameters) {
                methodSpecBuilder
                        .addParameter(parameterDef.type, parameterDef.name);
            }

            final String returnType = methodDef.returnType.getSimpleName();
            final String returnTypeDefault = CLASS_DEFAULTS.containsKey(returnType) ?
                    CLASS_DEFAULTS.get(returnType) : "null";

            final String instance = methodDef.isStatic ? "null" : "mInstance";

            if (methodDef.isStatic) {
                methodSpecBuilder
                        .addStatement("initializeStatic()");
            }

            if (!returnType.equals("void")) {
                methodSpecBuilder
                        .addStatement("$N result = $N", returnType, returnTypeDefault)
                        .beginControlFlow("try")
                        .addStatement("result = ($N) $N.invoke($N$N)",
                                methodDef.returnType.getName(), combinedMethodName, instance, argNames)
                        .endControlFlow("catch (Exception e) {}")
                        .addStatement("return result");
            } else {
                methodSpecBuilder
                        .beginControlFlow("try")
                        .addStatement("$N.invoke($N$N)",
                                combinedMethodName, instance, argNames)
                        .endControlFlow("catch (Exception e) {}");
            }

            if (methodDef.isStatic) {
                classBuilder
                        .addField(Method.class, combinedMethodName, Modifier.PRIVATE, Modifier.STATIC);
            } else {
                classBuilder
                        .addField(Method.class, combinedMethodName, Modifier.PRIVATE);
            }

            classBuilder
                    .addMethod(methodSpecBuilder.build());
        }

        classBuilder
                .addMethod(initializerBuilder.build())
                .addMethod(initializerStaticBuilder.build());

        return classBuilder.build();
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

            builder.addStatement("mInstance = new $T($N)", clazz, combinedParameters)
                    .addStatement("initialize()");

            methods.add(builder.build());
        }

        return methods;
    }

    private List<MethodSpec> buildSetterAndGetter(Class clazz) {
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

    private String getDefaultValue(String className) {
        return CLASS_DEFAULTS.containsKey(className) ? CLASS_DEFAULTS.get(className) : "null";
    }

    private void log(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
