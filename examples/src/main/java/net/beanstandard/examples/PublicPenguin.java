package net.beanstandard.examples;

import net.beanstandard.BeanStandard;

@BeanStandard
public final class PublicPenguin {

  private String foo;

  public String getFoo() {
    return foo;
  }
  public void setFoo(String foo) {
    this.foo = foo;
  }
}
