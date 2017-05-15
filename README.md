# bean-standard

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/bean-standard/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/bean-standard)

This project generates he builder pattern, specifically for classes that have 
a <i>default constructor</i> and <i>accessor pairs</i>. These are known as JavaBeans or POJOs.
The processor is supposed to generate roughly the same
builder API as [auto-builder](https://github.com/h908714124/auto-builder) and
[readable](https://github.com/h908714124/readable).

Builders for beans can be a useful tool in defensive programming.
For example, they can simply be used to make shallow copies.

### Quick start

Annotate a POJO class with `@BeanStandard`. That's pretty much it, 
you can now go ahead and obtain a builder instance from the generated class `*_Builder`.

#### Example: Adding the BeanStandard annotation to a POJO class

````java
@BeanStandard
class Animal {
  private String name;

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }
}
````

A class `Animal_Builder.java` will be generated in the same package.
A builder instance can be obtained from one of the three generated static methods:

* `Animal_Builders.builder()` to obtain an empty builder.
* `Animal_Builders.builder(Animal input)` for a builder that's initialized from `input`.
* `AnimalBuilders.perThreadFactory()` for a cached builder.

The builder will not modify the object that's passed into `Animal_Builders.builder(Animal input)`.

#### Example: Adding a simple toBuilder method

````java
@BeanStandard
class Animal {
  private String name;

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  Animal_Builder toBuilder() {
    return Animal_Builder.builder(this);
  }
}
````

If you use the factory, you have to wrap it in a `ThreadLocal`; see
[auto-builder's notes on caching](https://github.com/h908714124/auto-builder#caching).

#### Example: Adding a caching toBuilder method

````java
@BeanStandard
class Animal {
  private static final ThreadLocal<Animal_Builder.PerThreadFactory> FACTORY =
      ThreadLocal.withInitial(Animal_Builder::perThreadFactory);

  private String name;

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  Animal_Builder toBuilder() {
    return FACTORY.get().builder(this);
  }
}
````

### FAQ

#### What if there's a setter with no corresponding getter?

It will be ignored.

#### What if there's a getter with no corresponding setter?

It will be ignored, unless it returns `java.util.List<X>`, where `X` is some type.
In this case, the builder will generate code to update this list,
by assuming that it is <em>mutable</em> and <em>never null</em>.

### It's maven time

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>bean-standard</artifactId>
  <version>1.5</version>
  <scope>provided</scope>
</dependency>
````
