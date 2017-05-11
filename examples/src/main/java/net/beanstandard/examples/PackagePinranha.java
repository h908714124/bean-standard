package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

@BeanStandard
final class PackagePinranha {
  private boolean foo;

  boolean isFoo() {
    return foo;
  }

  void setFoo(boolean foo) {
    this.foo = foo;
  }
}
