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

  String getName();
  void setName(String name);
}
````

A class `Animal_Builder.java` will be generated in the same package.
A builder instance can be obtained from one of the three generated static methods:

* `Animal_Builders.builder()` to obtain an empty builder.
* `Animal_Builders.builder(Animal input)` for a builder that's initialized from `input`.
* `AnimalBuilders.perThreadFactory()` for a cached builder.

The builder will not modify the object that's passed into `Animal_Builders.builder(Animal input)`.
If you use the factory, you have to wrap it in a `ThreadLocal`; see
[auto-builder's notes on caching](https://github.com/h908714124/auto-builder#caching).

### It's maven time

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>bean-standard</artifactId>
  <version>1.4</version>
  <scope>provided</scope>
</dependency>
````
