package net.beanstandard.examples;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class PackagePiranhaTest {

  @Test
  public void testAccess() throws Exception {
    String classModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getModifiers());
    assertThat(classModifiers, not(containsString("public")));
    assertThat(classModifiers, containsString("final"));
    String builderMethodModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getDeclaredMethod("builder").getModifiers());
    assertThat(builderMethodModifiers, not(containsString("public")));
    String toBuilderMethodModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getDeclaredMethod("builder", PackagePiranha.class)
            .getModifiers());
    assertThat(toBuilderMethodModifiers, not(containsString("public")));
    String factoryMethodModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getDeclaredMethod("perThreadFactory")
            .getModifiers());
    assertThat(factoryMethodModifiers, not(containsString("public")));
    String setterMethodModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getDeclaredMethod("foo", Boolean.TYPE)
            .getModifiers());
    assertThat(setterMethodModifiers, not(containsString("public")));
    assertThat(setterMethodModifiers, not(containsString("final")));
    String buildMethodModifiers = Modifier.toString(
        PackagePiranha_Builder.class.getDeclaredMethod("build")
            .getModifiers());
    assertThat(buildMethodModifiers, not(containsString("public")));
    assertThat(Modifier.toString(PackagePiranha_Builder.PerThreadFactory.class
        .getDeclaredConstructor().getModifiers()), containsString("private"));
    assertThat(Modifier.toString(PackagePiranha_Builder.class
        .getDeclaredConstructor().getModifiers()), containsString("private"));
  }
}