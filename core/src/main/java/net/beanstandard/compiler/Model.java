package net.beanstandard.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

final class Model {

  private static final String SUFFIX = "_Builder";

  final TypeName generatedClass;
  final TypeElement sourceClassElement;
  final List<AccessorPair> accessorPairs;

  private Model(TypeName generatedClass,
                TypeElement sourceClassElement) {
    this.generatedClass = generatedClass;
    this.sourceClassElement = sourceClassElement;
    this.accessorPairs = AccessorHelper.scan(sourceClassElement);
  }

  static Model create(TypeElement sourceClassElement) {
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(
        sourceClassElement.getEnclosedElements());
    if (constructors.stream()
        .noneMatch(c -> !c.getModifiers().contains(PRIVATE) &&
            c.getParameters().isEmpty())) {
      throw new ValidationException(
          "Default constructor not found", sourceClassElement);
    }
    return new Model(peer(TypeName.get(sourceClassElement.asType())), sourceClassElement);
  }

  private static TypeName peer(TypeName type) {
    String name = String.join("_", rawType(type).simpleNames()) + SUFFIX;
    return rawType(type).topLevelClassName().peerClass(name);
  }

  ClassName sourceClass() {
    return rawType(TypeName.get(sourceClassElement.asType()));
  }
}
