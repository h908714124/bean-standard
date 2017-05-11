package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

import java.util.ArrayList;
import java.util.List;

@BeanStandard
final class Snake extends Animal {

  private static final ThreadLocal<Snake_Builder.PerThreadFactory> FACTORY =
      ThreadLocal.withInitial(Snake_Builder::perThreadFactory);

  private long length;
  private final List<String> friends =
      new ArrayList<>();

  @Override
  boolean isGood() {
    return false;
  }

  long getLength() {
    return length;
  }

  void setLength(long length) {
    this.length = length;
  }

  List<String> getFriends() {
    return friends;
  }

  Snake_Builder builderize() {
    return FACTORY.get().builder(this);
  }
}
