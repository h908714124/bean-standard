package net.beanstandard.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import java.util.Arrays;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class Analyser {

  private final Model model;
  private final MethodSpec initMethod;
  private final RefTrackingBuilder refTrackingBuilder;
  private final FieldSpec beanField;

  private Analyser(Model model) {
    this.model = model;
    this.beanField = FieldSpec.builder(model.sourceClass(), "bean")
        .addModifiers(PRIVATE)
        .build();
    this.initMethod = initMethod(model);
    this.refTrackingBuilder = RefTrackingBuilder.create(model, beanField);
  }

  static Analyser create(Model model) {
    return new Analyser(model);
  }

  TypeSpec analyse() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(rawType(model.generatedClass));
    builder.addMethod(builderMethod());
    builder.addMethod(builderMethodWithParam());
    builder.addMethod(initMethod);
    builder.addMethod(buildMethod());
    builder.addMethod(perThreadFactoryMethod(refTrackingBuilder));
    builder.addType(SimpleBuilder.create(model).define());
    builder.addType(refTrackingBuilder.define());
    builder.addType(PerThreadFactory.create(model, initMethod, refTrackingBuilder)
        .define());
    builder.addField(beanField);
    for (AccessorPair parameter : model.accessorPairs) {
      ParameterSpec p = ParameterSpec.builder(parameter.propertyType,
          parameter.propertyName).build();
      builder.addMethod(setterMethod(parameter, p));
    }
    return builder.addModifiers(model.maybePublic())
        .addModifiers(ABSTRACT)
        .addMethod(MethodSpec.constructorBuilder()
            .addStatement("this.$N = new $T()", beanField, model.sourceClass())
            .build())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BeanStandardProcessor.class.getCanonicalName())
            .build())
        .build();
  }

  private MethodSpec setterMethod(AccessorPair accessorPair, ParameterSpec p) {
    CodeBlock.Builder block = CodeBlock.builder();
    String setterName = accessorPair.setterName().orElse(null);
    if (setterName != null) {
      block.addStatement("this.$N.$L($N)", beanField, setterName, p);
    } else {
      block.addStatement("this.$N.$L().clear()", beanField,
          accessorPair.getterName())
          .addStatement("this.$N.$L().addAll($N)", beanField,
              accessorPair.getterName(), p);
    }
    return MethodSpec.methodBuilder(accessorPair.propertyName)
        .addCode(block.build())
        .addStatement("return this")
        .addParameter(p)
        .addModifiers(model.maybePublic())
        .addModifiers(FINAL)
        .returns(model.generatedClass)
        .build();
  }

  private MethodSpec builderMethod() {
    return MethodSpec.methodBuilder("builder")
        .addModifiers(STATIC)
        .addStatement("return new $T()", model.simpleBuilderClass)
        .returns(model.generatedClass)
        .build();
  }

  private MethodSpec builderMethodWithParam() {
    ParameterSpec builder = ParameterSpec.builder(model.generatedClass, "builder").build();
    ParameterSpec input = ParameterSpec.builder(model.sourceClass(), "input").build();
    CodeBlock.Builder block = CodeBlock.builder()
        .addStatement("$T $N = new $T()", builder.type, builder, model.simpleBuilderClass)
        .addStatement("$N($N, $N)", initMethod, builder, input)
        .addStatement("return $N", builder);
    return MethodSpec.methodBuilder("builder")
        .addCode(block.build())
        .addParameter(input)
        .addModifiers(STATIC)
        .returns(model.generatedClass)
        .build();
  }

  private static MethodSpec initMethod(Model model) {
    ParameterSpec builder = ParameterSpec.builder(model.generatedClass, "builder").build();
    ParameterSpec input = ParameterSpec.builder(model.sourceClass(), "input").build();
    CodeBlock.Builder block = CodeBlock.builder();
    block.beginControlFlow("if ($N == null)", input)
        .addStatement("throw new $T($S)",
            NullPointerException.class, "Null " + input.name)
        .endControlFlow();
    for (AccessorPair accessorPair : model.accessorPairs) {
      block.addStatement("$N.$N($N.$L())", builder, accessorPair.propertyName,
          input, accessorPair.getterName());
    }
    return MethodSpec.methodBuilder("init")
        .addCode(block.build())
        .addParameters(Arrays.asList(builder, input))
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private MethodSpec buildMethod() {
    return MethodSpec.methodBuilder("build")
        .returns(model.sourceClass())
        .addStatement("return this.$N", beanField)
        .addModifiers(model.maybePublic())
        .build();
  }

  private MethodSpec perThreadFactoryMethod(RefTrackingBuilder refTrackingBuilder) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("perThreadFactory")
        .returns(refTrackingBuilder.perThreadFactoryClass)
        .addModifiers(STATIC);
    return builder.addStatement("return new $T()",
        refTrackingBuilder.perThreadFactoryClass)
        .build();
  }
}
