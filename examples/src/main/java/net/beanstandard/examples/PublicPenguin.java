package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

import java.util.Optional;
import java.util.OptionalInt;

@BeanStandard
public final class PublicPenguin {

  private String foo;
  private Optional<String> friend;
  private OptionalInt bar;

  public String getFoo() {
    return foo;
  }
  public void setFoo(String foo) {
    this.foo = foo;
  }

  public Optional<String> getFriend() {
    return friend;
  }

  public void setFriend(Optional<String> friend) {
    this.friend = friend;
  }

  public OptionalInt getBar() {
    return bar;
  }
  public void setBar(OptionalInt bar) {
    this.bar = bar;
  }

  public PublicPenguin_Builder toBuilder() {
    return PublicPenguin_Builder.builder(this);
  }
}
