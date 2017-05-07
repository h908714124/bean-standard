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
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class AccessorPair {

  private final ExecutableElement getter;
  private final ExecutableElement setter;

  final String propertyName;
  final TypeName propertyType;

  private AccessorPair(ExecutableElement getter, ExecutableElement setter) {
    getterAssertions(getter);
    this.getter = requireNonNull(getter);
    this.setter = setter;
    this.propertyName = propertyName(getter);
    this.propertyType = propertyType(getter);
  }

  static AccessorPair create(ExecutableElement getter, ExecutableElement setter) {
    if (!AccessorHelper.SETTER_PATTERN.matcher(setter.getSimpleName()).matches()) {
      throw new AssertionError();
    }
    if (!setter.getSimpleName().toString().substring(3)
        .equals(truncatedGetterName(getter))) {
      throw new AssertionError();
    }
    if (setter.getParameters().size() == 1) {
      throw new AssertionError();
    }
    if (setter.getReturnType().getKind() != TypeKind.VOID) {
      throw new AssertionError();
    }
    if (!setter.getParameters().get(0).asType().equals(getter.getReturnType())) {
      throw new AssertionError();
    }
    return new AccessorPair(getter, setter);
  }

  static AccessorPair create(ExecutableElement getter) {
    TypeName type = TypeName.get(getter.getReturnType());
    if (!(type instanceof ParameterizedTypeName)) {
      throw new AssertionError();
    }
    if (!rawType(type).equals(TypeName.get(List.class))) {
      throw new AssertionError();
    }
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

  private static void getterAssertions(ExecutableElement getter) {
    String getterName = getter.getSimpleName().toString();
    if (!getter.getParameters().isEmpty()) {
      throw new AssertionError();
    }
    if (getter.getReturnType().getKind() == TypeKind.VOID) {
      throw new AssertionError();
    }
    if (!(AccessorHelper.GETTER_PATTERN.matcher(getterName).matches() ||
        getter.getReturnType().getKind() == TypeKind.BOOLEAN &&
            AccessorHelper.IS_PATTERN.matcher(getterName).matches())) {
      throw new AssertionError();
    }
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

  Optional<String> setterName() {
    if (setter == null) {
      return Optional.empty();
    }
    return Optional.of(setter.getSimpleName().toString());
  }
}
