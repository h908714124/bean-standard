package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

@BeanStandard
class Badger {

  private static final ThreadLocal<Badger_Builder.PerThreadFactory> FACTORY =
      ThreadLocal.withInitial(Badger_Builder::perThreadFactory);

  private String builder;
  private boolean perThreadFactory;

  Badger() {
  }

  String getBuilder() {
    return builder;
  }

  void setBuilder(String builder) {
    this.builder = builder;
  }

  boolean isPerThreadFactory() {
    return perThreadFactory;
  }

  void setPerThreadFactory(boolean perThreadFactory) {
    this.perThreadFactory = perThreadFactory;
  }

  final Badger_Builder toBuilder() {
    return FACTORY.get().builder(this);
  }
}
