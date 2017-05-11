package net.beanstandard.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class RefTrackingBuilder {

  private final Model model;
  final FieldSpec inUse;
  final FieldSpec beanField;
  final ClassName refTrackingBuilderClass;
  final ClassName perThreadFactoryClass;

  private RefTrackingBuilder(Model model,
                             FieldSpec beanField,
                             ClassName refTrackingBuilderClass,
                             ClassName perThreadFactoryClass) {
    this.model = model;
    this.beanField = beanField;
    this.refTrackingBuilderClass = refTrackingBuilderClass;
    this.perThreadFactoryClass = perThreadFactoryClass;
    this.inUse = FieldSpec.builder(TypeName.BOOLEAN, "inUse", PRIVATE).build();
  }

  private static ClassName perThreadFactoryClass(Model model) {
    return rawType(model.generatedClass)
        .nestedClass("PerThreadFactory");
  }

  static RefTrackingBuilder create(Model model, FieldSpec beanField) {
    ClassName perThreadFactoryClass = perThreadFactoryClass(model);
    return new RefTrackingBuilder(model,
        beanField, model.refTrackingBuilderClass, perThreadFactoryClass);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(refTrackingBuilderClass)
        .addField(inUse)
        .superclass(model.generatedClass)
        .addMethod(buildMethod())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }

  private MethodSpec buildMethod() {
    ParameterSpec result = ParameterSpec.builder(model.sourceClass(), "result").build();
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = super.build()", model.sourceClass(), result);
    builder.addStatement("super.$N = new $T()", beanField, model.sourceClass());
    builder.addStatement("this.$N = $L", inUse, false)
        .addStatement("return $N", result);
    return MethodSpec.methodBuilder("build")
        .addAnnotation(Override.class)
        .addCode(builder.build())
        .returns(model.sourceClass())
        .addModifiers(model.maybePublic())
        .build();
  }
}
