package net.beanstandard.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

final class SetterSignature {

  private final String setterName;
  private final TypeMirror type;

  private SetterSignature(String setterName, TypeMirror type) {
    this.setterName = setterName;
    this.type = type;
  }

  static SetterSignature ofSetter(ExecutableElement setter) {
    return new SetterSignature(setter.getSimpleName().toString(),
        setter.getParameters().get(0).asType());
  }

  static SetterSignature ofSetter(String setterName, TypeMirror type) {
    return new SetterSignature(setterName, type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SetterSignature that = (SetterSignature) o;
    return Objects.equals(setterName, that.setterName) &&
        Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(setterName, type);
  }
}
