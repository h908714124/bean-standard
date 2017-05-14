package net.beanstandard.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.beanstandard.compiler.LessTypes.asTypeElement;
import static net.beanstandard.compiler.LessTypes.getPackage;

final class Arity0 {

  static List<ExecutableElement> parameterlessMethods(
      TypeElement type) {
    PackageElement packageElement = getPackage(type);
    Map<String, ExecutableElement> methods = new LinkedHashMap<>();
    addFromSuperclass(type, methods, packageElement);
    return new ArrayList<>(methods.values());
  }

  private static void addFromSuperclass(
      TypeElement type,
      Map<String, ExecutableElement> methods,
      PackageElement packageElement) {
    addEnclosedMethods(type, methods, packageElement);
    TypeMirror superclass = type.getSuperclass();
    if (superclass.getKind() != TypeKind.DECLARED) {
      return;
    }
    addFromSuperclass(
        asTypeElement(superclass), methods, packageElement);
  }

  private static void addEnclosedMethods(
      TypeElement type,
      Map<String, ExecutableElement> methods,
      PackageElement packageElement) {
    methodsIn(type.getEnclosedElements())
        .stream()
        .filter(method -> method.getParameters().isEmpty())
        .filter(method -> method.getReturnType().getKind() != VOID)
        .filter(method -> !method.getModifiers().contains(ABSTRACT))
        .filter(method -> !method.getModifiers().contains(STATIC))
        .filter(method -> !method.getModifiers().contains(PRIVATE))
        .filter(method -> method.getModifiers().contains(PUBLIC) ||
            getPackage(method).equals(packageElement))
        .forEach(method ->
            methods.computeIfAbsent(method.getSimpleName().toString(),
                __ -> method));
  }
}
