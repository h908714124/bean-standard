package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

@BeanStandard
class Animal {

  private static final ThreadLocal<Animal_Builder.PerThreadFactory> FACTORY =
      ThreadLocal.withInitial(Animal_Builder::perThreadFactory);

  private String name;
  private boolean good;

  Animal() {
  }

  Animal(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  boolean isGood() {
    return good;
  }

  void setGood(boolean good) {
    this.good = good;
  }

  final Animal_Builder toBuilder() {
    return FACTORY.get().builder(this);
  }
}
