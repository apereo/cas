---
layout: default
title: CAS - Code Conventions
---

# Code Conventions
Following the lead of well established projects such as Apache and Eclipse, all code in CAS will comply with the [Code Conventions for the Java](http://java.sun.com/docs/codeconv/html/CodeConvTOC.doc.html) and additionally with the CAS specific conventions listed below. Javadoc should exist on all publicly exported class members and follow the [standard guidelines](http://java.sun.com/j2se/javadoc/writingdoccomments/index.html).

The following document describes the set of coding conventions that are specific to the CAS project:

## Brackets
All brackets should appear in compact form and are mandatory even for single line statements.

```java
public class FooClass {
    public void barMethod() {
        if (...) {
            // single line statement
        }
    }
}
```


## Needless else clauses
```java
public class FooClass {
    public String barMethod() {
        if (...) {
            return "foo";
        }
         
        return bar;
    }
}
```


## Indentations
Code indentation should be set to use 4 spaces. Tabs should **never** be used for indentation.

## Arrays vs. Lists
Where it's possible, instances of `ImmutableCollection` should be used instead of their `Collection` counterpart
or equivalent array definition. Returning or passing an argument as a native java `Collection` or array exposes
internal implementations to the caller/client and would allow in theory for malicious
modifications to the program state. 


## Static Members
Static member variables will always be in uppercase wherein each word is separated by an underscore:

```java
private static final String SOME_OBJECT = "TheObject"; 
```


## Logging
We use [SLF4J](http://www.slf4j.org/index.html) for logging. In abstract classes, the provided logger should be mark as `protected` so that it can be reused in subclasses. In the case where we create our own Log instance, we will use the [recommended practice](http://www.slf4j.org/faq.html#declaration_pattern) of declaring logger objects by SLF4j:

```java
package some.package;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
       
public final class MyClass {
  private final [static] Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
  ... etc
}
...
public class MyClass {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  ... etc
}
```

[Parameterized log messages](http://www.slf4j.org/faq.html#logging_performance) are preferred:
```java
final Object entry = new SomeObject();
log.debug("The entry is {}.", entry);
```

This is the preferred method over checking for a particular logging level and concatenating parameters through String objects.


## Qualifying instance variables with this
We qualify all instance variables with `this` with the exception of the Logging instances. We don't qualify that variable with "this" because it is well-known to be threadsafe. `logger.warn("Message")` becomes more of an idiom than invocation of instance variable.

##Use of the final keyword
We use the keyword `final` keyword on local variables and parameters. Classes and methods are preferred to also be marked as `final` unless there are specific design decisions.


## Naming testcases
If we were writing a JUnit testcase for code defined in `Foo.java`, we would name it `FooTests.java`. We do not allow any code which is not a testcase to have a name ending in "Tests". All testcase must use annotations `(@Test)` instead of extending `junit.framework.TestCase`. Furthermore, the usage of `junit.framework` classes is generally discouraged. 


## Injection
For required dependencies, the constructor injection must be used whereas setters can be used for optional dependencies.

## equals() and hashCode() methods
The recommend way to build the `hashCode()` and `equals()` methods is to use the `EqualsBuilder` and `HashCodeBuilder `classes form the `commons-lang(3)` library.


## Template for commit messages
Short (50 chars or less) summary of changes.

More detailed explanatory text, if necessary.  Wrap it to about 72 characters or so.  In some contexts, the first line is treated as the subject of an email and the rest of the text as the body.  The blank line separating the summary from the body is critical (unless you omit the body entirely); tools like rebase can get confused if you run the two together. 

- Further paragraphs come after blank lines. 
- Bullet points are okay, too 
- Typically a hyphen or asterisk is used for the bullet, preceded by a  single space, with blank lines in between.
- The summary should contain both a Jira issue number where possible and a brief description, e.g.

Placing _both_ the issue number and brief description would improve commit history in SCM. 

## Creating Pull Requests
When creating a pull request, make sure that the pull references the Github issue number:

![](https://camo.githubusercontent.com/0d91dc7e679d86bd4814faae37f0316279074571/68747470733a2f2f662e636c6f75642e6769746875622e636f6d2f6173736574732f3539372f3439383937372f64383262643761382d626332362d313165322d383663652d3835613435336334643638332e706e67)

This allows the pull request to be linked to the issue. When the pull is merged, the issue will automatically be closed as well.

## Checkstyle
The current codebase takes advantage of the [Checkstyle engine](http://checkstyle.sourceforge.net) to [enforce conventions](https://github.com/Jasig/cas/blob/master/checkstyle-rules.xml) as much as possible.
 

