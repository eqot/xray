package com.eqot.xray.processor;

import com.eqot.xray.Xray;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
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
    private static final String POSTFIX_OF_GENERATED_PACKAGE = ".xray";
    private static final String POSTFIX_OF_GENERATED_CLASS = "$Xray";

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
        for (Element element : roundEnv.getElementsAnnotatedWith(Xray.class)) {
            final Xray xray = element.getAnnotation(Xray.class);
            if (xray == null) {
                continue;
            }

            String className = "";
            try {
                xray.value();
            } catch (MirroredTypeException mte) {
                className = mte.getTypeMirror().toString();
            }
            if (className.equals("") || className.equals("java.lang.Object")) {
                continue;
            }

            generateCode(className);
        }

        return true;
    }

    private void generateCode(String target) {
        final ClassDef classDef = new ClassDef(target);
        final ClassName generatedClassName = ClassName.get(
                classDef.packageName + POSTFIX_OF_GENERATED_PACKAGE,
                classDef.className + POSTFIX_OF_GENERATED_CLASS);

        final TypeSpec generatedClass = buildClass(target);

        try {
            final JavaFileObject source = processingEnv.getFiler().createSourceFile(
                    generatedClassName.toString());
            final Writer writer = source.openWriter();

            JavaFile.builder(generatedClassName.packageName(), generatedClass)
                    .build()
                    .writeTo(writer);
//                    .writeTo(System.out);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec buildClass(String target) {
        final ClassDef classDef = new ClassDef(target);

        final ClassName targetClass = ClassName.get(classDef.packageName, classDef.className);
        final ClassName generatedClass = ClassName.get(
                classDef.packageName, classDef.className + "$Xray");

        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClass.simpleName())
                .addModifiers(Modifier.PUBLIC);

        classBuilder
                .addField(targetClass, "mInstance", Modifier.PRIVATE, Modifier.FINAL);

        for (ClassDef.MethodDef constructor : classDef.constructors) {
            final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            String combinedParameters = "";
            for (ClassDef.ParameterDef parameterDef : constructor.parameters) {
                constructorBuilder
                        .addParameter(parameterDef.type, parameterDef.name);

                if (!combinedParameters.equals("")) {
                    combinedParameters += ", ";
                }
                combinedParameters += parameterDef.name;
            }
            constructorBuilder
                    .addStatement("mInstance = new $T($N)", targetClass, combinedParameters)
                    .addStatement("initialize()");

            classBuilder.addMethod(constructorBuilder.build());
        }

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
                                combinedMethodName, targetClass, methodDef.name, argTypes)
                        .addStatement("$N.setAccessible(true)", combinedMethodName)
                        .endControlFlow("catch (Exception e) {}");
            } else {
                initializerBuilder
                        .beginControlFlow("try")
                        .addStatement("$N = $T.class.getDeclaredMethod($S$N)",
                                combinedMethodName, targetClass, methodDef.name, argTypes)
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

    private void log(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
