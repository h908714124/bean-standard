package net.beanstandard.compiler;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;
import static net.beanstandard.compiler.Arity0.parameterlessMethods;
import static net.beanstandard.compiler.Arity1.singleParameterMethodsReturningVoid;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class MethodScanner {

  static final Pattern GETTER_PATTERN =
      Pattern.compile("^get[A-Z].*$");
  static final Pattern IS_PATTERN =
      Pattern.compile("^is[A-Z].*$");
  static final Pattern SETTER_PATTERN =
      Pattern.compile("^set[A-Z].*$");

  private final TypeElement sourceClassElement;

  private MethodScanner(TypeElement sourceClassElement) {
    this.sourceClassElement = sourceClassElement;
  }

  static MethodScanner create(TypeElement sourceClassElement) {
    checkSourceElement(sourceClassElement);
    return new MethodScanner(sourceClassElement);
  }

  List<AccessorPair> scan() {
    Map<SetterSignature, ExecutableElement> setters = setters();
    List<AccessorPair> result = new ArrayList<>();
    parameterlessMethods(sourceClassElement).stream()
        .filter(m -> m.getParameters().isEmpty())
        .filter(m -> m.getReturnType().getKind() != TypeKind.VOID)
        .filter(m -> GETTER_PATTERN.matcher(m.getSimpleName().toString()).matches() ||
            TypeKind.BOOLEAN == m.getReturnType().getKind() &&
                IS_PATTERN.matcher(m.getSimpleName().toString()).matches())
        .collect(groupingBy(m -> m.getSimpleName().toString()))
        .forEach((name, executableElements) -> {
          ExecutableElement getter = executableElements.get(0);
          ExecutableElement matchingSetter = matchingSetter(getter, setters);
          if (matchingSetter != null) {
            result.add(AccessorPair.create(getter, matchingSetter));
          } else {
            TypeName type = TypeName.get(getter.getReturnType());
            if (type instanceof ParameterizedTypeName &&
                rawType(type).equals(TypeName.get(List.class))) {
              result.add(AccessorPair.create(getter));
            }
          }
        });
    return result;
  }

  private static ExecutableElement matchingSetter(
      ExecutableElement getter,
      Map<SetterSignature, ExecutableElement> setters) {
    if (!getter.getParameters().isEmpty() ||
        getter.getReturnType().getKind() == TypeKind.VOID) {
      throw new AssertionError();
    }
    String truncatedGetterName = truncatedGetterName(getter);
    return setters.get(SetterSignature.ofSetter("set" +
        truncatedGetterName, getter.getReturnType()));
  }

  private static String truncatedGetterName(ExecutableElement getter) {
    String getterName = getter.getSimpleName().toString();
    String truncatedGetterName;
    if (GETTER_PATTERN.matcher(getterName).matches()) {
      truncatedGetterName = getterName.substring(3);
    } else if (getter.getReturnType().getKind() == TypeKind.BOOLEAN &&
        IS_PATTERN.matcher(getterName).matches()) {
      truncatedGetterName = getterName.substring(2);
    } else {
      throw new AssertionError();
    }
    return truncatedGetterName;
  }

  private Map<SetterSignature, ExecutableElement> setters() {
    Map<SetterSignature, ExecutableElement> result = new HashMap<>();
    singleParameterMethodsReturningVoid(sourceClassElement).stream()
        .filter(m -> m.getParameters().size() == 1)
        .filter(m -> m.getReturnType().getKind() == TypeKind.VOID)
        .filter(m -> SETTER_PATTERN.matcher(m.getSimpleName().toString()).matches())
        .collect(groupingBy(m -> m.getSimpleName().toString()))
        .forEach((name, executableElements) -> executableElements.forEach(executableElement -> {
          result.put(SetterSignature.ofSetter(executableElement), executableElement);
        }));
    return result;
  }

  private static void checkSourceElement(TypeElement sourceClassElement) {
    if (!sourceClassElement.getTypeParameters().isEmpty()) {
      throw new ValidationException("Type parameters not allowed here", sourceClassElement);
    }
    if (sourceClassElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException("The class may not be private", sourceClassElement);
    }
    if (sourceClassElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ValidationException("The class may not be abstract", sourceClassElement);
    }
    if (sourceClassElement.getEnclosingElement() != null &&
        sourceClassElement.getEnclosingElement().getKind() == ElementKind.CLASS &&
        !sourceClassElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException("The inner class must be static " +
          sourceClassElement.getEnclosingElement(), sourceClassElement);
    }
  }
}
