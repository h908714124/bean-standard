package net.beanstandard.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

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
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    SetterSignature that = (SetterSignature) other;
    return (setterName != null ? setterName.equals(that.setterName) : that.setterName == null) &&
        (type != null ? type.equals(that.type) : that.type == null);
  }

  @Override
  public int hashCode() {
    int result = setterName != null ? setterName.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
