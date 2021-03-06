package net.beanstandard.examples;

import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class PublicPenguinTest {

  @Test
  public void testAccess() throws Exception {
    String classModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getModifiers());
    assertThat(classModifiers, containsString("public"));
    assertThat(classModifiers, containsString("final"));
    String builderMethodModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getDeclaredMethod("builder").getModifiers());
    assertThat(builderMethodModifiers, not(containsString("public")));
    String toBuilderMethodModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getDeclaredMethod("builder", PublicPenguin.class)
            .getModifiers());
    // the static methods are never public
    assertThat(toBuilderMethodModifiers, not(containsString("public")));
    String factoryMethodModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getDeclaredMethod("perThreadFactory")
            .getModifiers());
    assertThat(factoryMethodModifiers, not(containsString("public")));
    String setterMethodModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getDeclaredMethod("foo", String.class)
            .getModifiers());
    assertThat(setterMethodModifiers, containsString("public"));
    assertThat(setterMethodModifiers, not(containsString("final")));
    String buildMethodModifiers = Modifier.toString(
        PublicPenguin_Builder.class.getDeclaredMethod("build")
            .getModifiers());
    assertThat(buildMethodModifiers, containsString("public"));
    assertThat(Modifier.toString(PublicPenguin_Builder.class
        .getDeclaredConstructor().getModifiers()), containsString("private"));
    assertThat(Modifier.toString(PublicPenguin_Builder.PerThreadFactory.class
        .getDeclaredConstructor().getModifiers()), containsString("private"));
    assertThat(Modifier.toString(PublicPenguin_Builder.class
        .getDeclaredConstructor().getModifiers()), containsString("private"));

  }

  @Test
  public void testOptionalNull() {
    String nobody = null;
    PublicPenguin p0 = PublicPenguin_Builder.builder().foo("").bar(1).build();
    PublicPenguin p1 = p0.toBuilder().friend("steven").build();
    PublicPenguin p2 = p1.toBuilder().friend(nobody).build();
    assertThat(p0.getFriend(), is(Optional.empty()));
    assertThat(p1.getFriend(), is(Optional.of("steven")));
    assertThat(p2.getFriend(), is(Optional.empty()));
    assertThat(p2.getBar(), is(OptionalInt.of(1)));
  }
}