package net.beanstandard.compiler;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

final class PerThreadFactory {

  private final Model model;
  private final MethodSpec initMethod;
  private final FieldSpec builder;
  private final FieldSpec beanField;

  private PerThreadFactory(
      Model model, MethodSpec initMethod, FieldSpec beanField) {
    this.model = model;
    this.initMethod = initMethod;
    this.builder = FieldSpec.builder(model.generatedClass, "builder", PRIVATE)
        .build();
    this.beanField = beanField;
  }

  static PerThreadFactory create(
      Model model, MethodSpec initMethod, FieldSpec beanField) {
    return new PerThreadFactory(model, initMethod, beanField);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(model.perThreadFactoryClass())
        .addField(builder)
        .addMethod(builderMethod())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(STATIC, FINAL)
        .build();
  }

  private MethodSpec builderMethod() {
    ParameterSpec input = ParameterSpec.builder(model.sourceClass(), "input").build();
    CodeBlock.Builder block = CodeBlock.builder()
        .beginControlFlow("if (this.$N == null || this.$N.$N != null)",
            builder, builder, beanField)
        .addStatement("this.$N = new $T()", builder, model.generatedClass)
        .endControlFlow()
        .addStatement("$T.$N(this.$N, $N)", model.generatedClass, initMethod, builder, input)
        .addStatement("return this.$N", builder);
    return MethodSpec.methodBuilder("builder")
        .addParameter(input)
        .addCode(block.build())
        .returns(model.generatedClass)
        .build();
  }
}
