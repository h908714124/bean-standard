package net.beanstandard.examples;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class BadgerTest {
  @Test
  public void potentiallyProblematicPropertyNames() throws Exception {
    Badger bugs = Badger_Builder.builder()
        .builder("bugs")
        .perThreadFactory(true)
        .build();
    Badger elmer = bugs.toBuilder()
        .builder("elmer")
        .perThreadFactory(false)
        .build();
    assertThat(bugs.getBuilder(), is("bugs"));
    assertThat(bugs.isPerThreadFactory(), is(true));
    assertThat(elmer.getBuilder(), is("elmer"));
    assertThat(elmer.isPerThreadFactory(), is(false));
  }
}