package net.beanstandard.compiler;

import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;

public class BeanStandardProcessorTest {

  @Test
  public void privateConstructor() throws Exception {
    List<String> animal = asList(
        "package animal;",
        "import net.beanstandard.BeanStandard;",
        "",
        "@BeanStandard",
        "class Animal {",
        "  private String name;",
        "  private Animal() {",
        "  }",
        "  String getName() {",
        "    return name;",
        "  }",
        "  void setName(String name) {",
        "    this.name = name;",
        "  }",
        "}");
    JavaFileObject animalFile = forSourceLines("animal.Animal", animal);
    assertAbout(javaSources()).that(asList(animalFile))
        .processedWith(new BeanStandardProcessor())
        .failsToCompile()
        .withErrorContaining("Default constructor not found");
  }

  @Test
  public void inheritedAccessorsArePackagePrivate() throws Exception {
    List<String> animal = asList(
        "package animal;",
        "",
        "public class Animal {",
        "  private String name;",
        "  protected Animal() {",
        "  }",
        "  String getName() {",
        "    return name;",
        "  }",
        "  void setName(String name) {",
        "    this.name = name;",
        "  }",
        "}");
    List<String> horse = asList(
        "package horse;",
        "import animal.Animal;",
        "import net.beanstandard.BeanStandard;",
        "",
        "@BeanStandard",
        "class Horse extends Animal {",
        "}");
    JavaFileObject animalFile = forSourceLines("animal.Animal", animal);
    JavaFileObject horseFile = forSourceLines("horse.Horse", horse);
    assertAbout(javaSources()).that(asList(animalFile, horseFile))
        .processedWith(new BeanStandardProcessor())
        .compilesWithoutError();
  }

  @Test
  public void inheritedAccessorsAreProtected() throws Exception {
    List<String> animal = asList(
        "package animal;",
        "",
        "public class Animal {",
        "  private String name;",
        "  protected Animal() {",
        "  }",
        "  protected String getName() {",
        "    return name;",
        "  }",
        "  protected void setName(String name) {",
        "    this.name = name;",
        "  }",
        "}");
    List<String> horse = asList(
        "package horse;",
        "import animal.Animal;",
        "import net.beanstandard.BeanStandard;",
        "",
        "@BeanStandard",
        "class Horse extends Animal {",
        "}");
    JavaFileObject animalFile = forSourceLines("animal.Animal", animal);
    JavaFileObject horseFile = forSourceLines("horse.Horse", horse);
    JavaFileObject emptyBuilder = forSourceLines("horse.Horse_Builder",
        "package horse;",
        "import javax.annotation.Generated;",
        "",
        "@Generated(\"net.beanstandard.compiler.BeanStandardProcessor\")",
        "final class Horse_Builder {",
        "  private Horse bean;",
        "",
        "  private Horse_Builder() {",
        "  }",
        "",
        "  static Horse_Builder builder() {",
        "    Horse_Builder builder = new Horse_Builder();",
        "    builder.bean = new Horse();",
        "    return builder;",
        "  }",
        "",
        "  static Horse_Builder builder(Horse input) {",
        "    Horse_Builder builder = new Horse_Builder();",
        "    init(builder, input);",
        "    return builder;",
        "  }",
        "",
        "  static PerThreadFactory perThreadFactory() {",
        "    return new PerThreadFactory();",
        "  }",
        "",
        "  private static void init(Horse_Builder builder, Horse input) {",
        "    if (builder.bean == null) {",
        "      builder.bean = new Horse();",
        "    }",
        "  }",
        "",
        "  Horse build() {",
        "    Horse result = this.bean;",
        "    this.bean = null;",
        "    return result;",
        "  }",
        "",
        "  static final class PerThreadFactory {",
        "    private Horse_Builder builder;",
        "",
        "    private PerThreadFactory() {",
        "    }",
        "",
        "    Horse_Builder builder(Horse input) {",
        "      if (this.builder == null || this.builder.bean != null) {",
        "        this.builder = new Horse_Builder();",
        "      }",
        "      Horse_Builder.init(this.builder, input);",
        "      return this.builder;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(asList(animalFile, horseFile))
        .processedWith(new BeanStandardProcessor())
        .compilesWithoutError()
        .and().generatesSources(emptyBuilder);
  }
}