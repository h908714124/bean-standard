package net.beanstandard.compiler;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.util.List;
import java.util.Optional;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.util.Objects.requireNonNull;
import static net.beanstandard.compiler.MethodScanner.GETTER_PATTERN;
import static net.beanstandard.compiler.MethodScanner.IS_PATTERN;
import static net.beanstandard.compiler.MethodScanner.SETTER_PATTERN;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class AccessorPair {

  private final ExecutableElement getter;
  private final ExecutableElement setter;

  final String propertyName;
  final TypeName propertyType;

  private AccessorPair(ExecutableElement getter, ExecutableElement setter) {
    if (!getterAssertions(getter)
        || !setterAssertions(getter, setter)) {
      throw new AssertionError();
    }
    this.getter = requireNonNull(getter);
    this.setter = setter;
    this.propertyName = propertyName(getter);
    this.propertyType = propertyType(getter);
  }

  private static boolean setterAssertions(ExecutableElement getter, ExecutableElement setter) {
    TypeName getterType = TypeName.get(getter.getReturnType());
    if (setter == null) {
      return getterType instanceof ParameterizedTypeName &&
          rawType(getterType).equals(TypeName.get(List.class));
    } else {
      return SETTER_PATTERN.matcher(setter.getSimpleName()).matches() &&
          setter.getSimpleName().toString().substring(3).equals(
              truncatedGetterName(getter)) &&
          setter.getParameters().size() == 1 &&
          setter.getReturnType().getKind() == TypeKind.VOID &&
          setter.getParameters().get(0).asType().equals(
              getter.getReturnType());
    }
  }

  static AccessorPair create(ExecutableElement getter, ExecutableElement setter) {
    return new AccessorPair(getter, setter);
  }

  static AccessorPair create(ExecutableElement getter) {
    return new AccessorPair(getter, null);
  }

  private static String lowerFirst(String s) {
    if (s.length() == 1) {
      return s.toLowerCase();
    }
    if (isLowerCase(s.charAt(1))) {
      return toLowerCase(s.charAt(0)) + s.substring(1);
    }
    return s;
  }

  private static boolean getterAssertions(ExecutableElement getter) {
    String getterName = getter.getSimpleName().toString();
    return getter.getParameters().isEmpty() &&
        getter.getReturnType().getKind() != TypeKind.VOID &&
        (GETTER_PATTERN.matcher(getterName).matches() ||
            getter.getReturnType().getKind() == TypeKind.BOOLEAN &&
                IS_PATTERN.matcher(getterName).matches());
  }

  private static String propertyName(ExecutableElement getter) {
    return lowerFirst(truncatedGetterName(getter));
  }

  private static String truncatedGetterName(ExecutableElement getter) {
    String getterName = getter.getSimpleName().toString();
    return getterName.startsWith("is") ?
        getterName.substring(2) :
        getterName.substring(3);
  }

  private static TypeName propertyType(ExecutableElement getter) {
    return TypeName.get(getter.getReturnType());
  }

  String getterName() {
    return getter.getSimpleName().toString();
  }

  // can only be absent if this is a list; see constructor
  Optional<String> setterName() {
    if (setter == null) {
      return Optional.empty();
    }
    return Optional.of(setter.getSimpleName().toString());
  }

  TypeName wrappedType() {
    if (!(propertyType instanceof ParameterizedTypeName)) {
      throw new AssertionError();
    }
    ParameterizedTypeName realType = (ParameterizedTypeName) this.propertyType;
    if (realType.typeArguments.size() != 1) {
      throw new AssertionError();
    }
    return realType.typeArguments.get(0);
  }
}
