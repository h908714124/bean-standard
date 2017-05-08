package net.beanstandard.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import java.util.Arrays;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
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
        .addModifiers(PRIVATE, FINAL)
        .initializer("new $T()", model.sourceClass())
        .build();
    this.initMethod = initMethod(model, beanField);
    this.refTrackingBuilder = RefTrackingBuilder.create(model);
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
    RefTrackingBuilder refTrackingBuilder = RefTrackingBuilder.create(model);
    builder.addType(refTrackingBuilder.define());
    builder.addType(PerThreadFactory.create(model, initMethod, refTrackingBuilder).define());
    builder.addField(beanField);
    for (AccessorPair parameter : model.accessorPairs) {
      ParameterSpec p = ParameterSpec.builder(parameter.propertyType,
          parameter.propertyName).build();
      builder.addMethod(setterMethod(parameter, p));
    }
    return builder.addModifiers(PUBLIC, ABSTRACT)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE).build())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BeanStandardProcessor.class.getCanonicalName())
            .build())
        .build();
  }

  private MethodSpec setterMethod(AccessorPair accessorPair, ParameterSpec p) {
    return MethodSpec.methodBuilder(accessorPair.propertyName)
        .addStatement("this.$N.$L($N)", beanField, accessorPair.setterName(), p)
        .addStatement("return this")
        .addParameter(p)
        .addModifiers(PUBLIC)
        .returns(model.generatedClass)
        .build();
  }

  private MethodSpec builderMethod() {
    return MethodSpec.methodBuilder("builder")
        .addModifiers(PUBLIC, STATIC)
        .addStatement("return new $T()", model.simpleBuilderClass)
        .returns(model.generatedClass)
        .build();
  }

  private MethodSpec builderMethodWithParam() {
    ParameterSpec builder = ParameterSpec.builder(model.generatedClass, "builder").build();
    ParameterSpec input = ParameterSpec.builder(TypeName.get(model.sourceClassElement.asType()), "input").build();
    CodeBlock.Builder block = CodeBlock.builder();
    block.beginControlFlow("if ($N == null)", input)
        .addStatement("throw new $T($S)",
            NullPointerException.class, "Null " + input.name)
        .endControlFlow();
    block.addStatement("$T $N = new $T()", builder.type, builder, model.simpleBuilderClass);
    for (AccessorPair parameter : model.accessorPairs) {
      block.addStatement("$N.$N = $N.$L()", builder, parameter.propertyName, input,
          parameter.getterName());
    }
    block.addStatement("return $N", builder);
    return MethodSpec.methodBuilder("builder")
        .addCode(block.build())
        .addParameter(input)
        .addModifiers(PUBLIC, STATIC)
        .returns(model.generatedClass)
        .build();
  }

  private static MethodSpec initMethod(Model model, FieldSpec beanField) {
    ParameterSpec builder = ParameterSpec.builder(model.generatedClass, "builder").build();
    ParameterSpec input = ParameterSpec.builder(model.sourceClass(), "input").build();
    CodeBlock.Builder block = CodeBlock.builder();
    block.beginControlFlow("if ($N == null)", input)
        .addStatement("throw new $T($S)",
            NullPointerException.class, "Null " + input.name)
        .endControlFlow();
    for (AccessorPair accessorPair : model.accessorPairs) {
      block.addStatement("$N.$N.$L($N.$L())", builder, beanField, accessorPair.setterName(), input,
          accessorPair.getterName());
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
