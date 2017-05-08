package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

@BeanStandard
final class JavaBean {

  private String foo;

  String getFoo() {
    return foo;
  }

  void setFoo(String foo) {
    this.foo = foo;
  }
}
