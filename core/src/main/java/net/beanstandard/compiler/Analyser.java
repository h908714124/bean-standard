package net.beanstandard.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class Analyser {

  private final Model model;

  private Analyser(Model model) {
    this.model = model;
  }

  static Analyser create(Model model) {
    return new Analyser(model);
  }

  TypeSpec analyse() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(rawType(model.generatedClass));
    builder.addMethod(builderMethod());
    builder.addMethod(builderMethodWithParam());
    builder.addMethod(buildMethod());
    for (AccessorPair parameter : model.accessorPairs) {
      FieldSpec.Builder fieldBuilder = FieldSpec.builder(parameter.propertyType,
          parameter.propertyName)
          .addModifiers(PRIVATE);
      FieldSpec f = fieldBuilder.build();
      ParameterSpec p = ParameterSpec.builder(parameter.propertyType,
          parameter.propertyName).build();
      builder.addField(f);
      builder.addMethod(setterMethod(parameter, f, p));
    }
    return builder.addModifiers(PUBLIC, FINAL)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE).build())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BeanStandardProcessor.class.getCanonicalName())
            .build())
        .build();
  }

  private MethodSpec setterMethod(AccessorPair parameter, FieldSpec f, ParameterSpec p) {
    return MethodSpec.methodBuilder(
        parameter.propertyName)
        .addStatement("this.$N = $N", f, p)
        .addStatement("return this")
        .addParameter(p)
        .addModifiers(PUBLIC)
        .returns(model.generatedClass)
        .build();
  }

  private MethodSpec builderMethod() {
    return MethodSpec.methodBuilder("builder")
        .addModifiers(PUBLIC, STATIC)
        .addStatement("return new $T()", model.generatedClass)
        .returns(model.generatedClass)
        .addJavadoc("Creates a new builder.\n" +
            "\n" +
            "@return a builder\n")
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
    block.addStatement("$T $N = new $T()", builder.type, builder, model.generatedClass);
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

  private MethodSpec buildMethod() {
    CodeBlock.Builder block = CodeBlock.builder();
    for (int i = 0; i < model.accessorPairs.size(); i++) {
      AccessorPair parameter = model.accessorPairs.get(i);
      FieldSpec f = FieldSpec.builder(parameter.propertyType,
          parameter.propertyName)
          .addModifiers(PRIVATE)
          .build();
      if (i > 0) {
        block.add(",");
      }
      block.add("\n    $N", f);
    }
    return MethodSpec.methodBuilder("build")
        .addCode("return new $T(", model.sourceClass())
        .addCode(block.build())
        .addCode(");\n")
        .returns(model.sourceClass())
        .addModifiers(PUBLIC)
        .build();
  }
}
