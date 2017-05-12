package net.beanstandard.compiler;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.beanstandard.compiler.BeanStandardProcessor.rawType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

final class Model {

  private static final String SUFFIX = "_Builder";
  private static final Modifier[] PUBLIC_MODIFIER = {PUBLIC};
  private static final Modifier[] NO_MODIFIERS = new Modifier[0];

  private final TypeElement sourceClassElement;

  final TypeName generatedClass;
  final List<AccessorPair> accessorPairs;

  private Model(TypeName generatedClass,
                TypeElement sourceClassElement) {
    this.generatedClass = generatedClass;
    this.sourceClassElement = sourceClassElement;
    this.accessorPairs = MethodScanner.scan(sourceClassElement);
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
    ClassName generatedClass = peer(TypeName.get(sourceClassElement.asType()));
    return new Model(generatedClass,
        sourceClassElement);
  }

  private static ClassName peer(TypeName type) {
    String name = String.join("_", rawType(type).simpleNames()) + SUFFIX;
    return rawType(type).topLevelClassName().peerClass(name);
  }

  private boolean isPublic() {
    return sourceClassElement.getModifiers().contains(PUBLIC);
  }

  Modifier[] maybePublic() {
    if (isPublic()) {
      return PUBLIC_MODIFIER;
    }
    return NO_MODIFIERS;
  }

  ClassName sourceClass() {
    return rawType(TypeName.get(sourceClassElement.asType()));
  }

  ClassName perThreadFactoryClass() {
    return rawType(generatedClass).nestedClass("PerThreadFactory");
  }
}
