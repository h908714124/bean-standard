package net.beanstandard.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class OptionalInfo {

  private static final ClassName OPTIONAL_CLASS =
      ClassName.get(Optional.class);

  private static final List<OptionalInfo> OPTIONAL_PRIMITIVES =
      Arrays.asList(
          new OptionalInfo(ClassName.get(OptionalInt.class), TypeName.INT),
          new OptionalInfo(ClassName.get(OptionalDouble.class), TypeName.DOUBLE),
          new OptionalInfo(ClassName.get(OptionalLong.class), TypeName.LONG));

  final ClassName wrapper;
  final TypeName wrapped;

  private OptionalInfo(ClassName wrapper, TypeName wrapped) {
    this.wrapper = wrapper;
    this.wrapped = wrapped;
  }

  static OptionalInfo create(TypeName typeName) {
    if (typeName instanceof ClassName) {
      for (OptionalInfo optionalPrimitive : OPTIONAL_PRIMITIVES) {
        if (optionalPrimitive.wrapper.equals(typeName)) {
          return optionalPrimitive;
        }
      }
      return null;
    }
    if (!(typeName instanceof ParameterizedTypeName)) {
      return null;
    }
    ParameterizedTypeName type = (ParameterizedTypeName) typeName;
    if (!type.rawType.equals(OPTIONAL_CLASS)) {
      return null;
    }
    return new OptionalInfo(OPTIONAL_CLASS,
        type.typeArguments.get(0));
  }

  boolean isOptional() {
    return wrapper.equals(OPTIONAL_CLASS);
  }

  private boolean isIrregular() {
    return wrapped instanceof TypeVariableName ||
        isOptional() &&
            rawType(wrapped).equals(OPTIONAL_CLASS);
  }

  boolean isRegular() {
    return !isIrregular();
  }
}
