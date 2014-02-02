---
layout: nosidebar
title: CAS - Code Conventions
---

# Code Conventions
Following the lead of well established projects such as Apache and Eclipse, all code in CAS will comply with the [Code Conventions for the Java](http://java.sun.com/docs/codeconv/html/CodeConvTOC.doc.html) and additionally with the CAS specific conventions listed below. Javadoc should exist on all publicly exported class members and follow the [standard guidelines](http://java.sun.com/j2se/javadoc/writingdoccomments/index.html).

The following document describes the set of coding conventions that are specific to the CAS project:

##Brackets
All brackets should appear in compact form and are mandatory even for single line statements.

{% highlight java %}
public class FooClass {
    public void barMethod() {
        if (...) {
            // single line statement
        }
    }
}
{% endhighlight %}

##Needless else clauses
{% highlight java %}
public class FooClass {
    public String barMethod() {
        if (...) {
            return "foo";
        }
         
        return bar;
    }
}
{% endhighlight %}

##Indentations
Code indentation should be set to use 4 spaces. Tabs should never be used for indentation.

##Static Members
Static member variables will always be in uppercase wherein each word is separated by an underscore:

{% highlight java %}
private static final String SOME_OBJECT = "TheObject"; 
{% endhighlight %}

##Logging
We use [SLF4J](http://www.slf4j.org/index.html) for logging. In abstract classes, the provided logger should be mark as `protected` so that it can be reused in subclasses. In the case where we create our own Log instance, we will use the [recommended practice](http://www.slf4j.org/faq.html#declaration_pattern) of declaring logger objects by SLF4j:

{% highlight java %}
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
{% endhighlight %}

[Parameterized log messages](http://www.slf4j.org/faq.html#logging_performance) are preferred:
{% highlight java %}
final Object entry = new SomeObject();
log.debug("The entry is {}.", entry);
{% endhighlight %}

This is the preferred method over checking for a particular logging level and concatenating parameters through String objects.

##Qualifying instance variables with this
We qualify all instance variables with `this` with the exception of the Logging instances. We don't qualify that variable with "this" because it is well-known to be threadsafe. `logger.warn("Message")` becomes more idiom than invocation of instance variable.

##Use of the final keyword
We use the keyword `final` keyword on local variables, parameters. Classes and methods are preferred to also be marked as `final` unless there are specific design decisions.

##Naming testcases
If we were writing a JUnit testcase for code defined in `Foo.java`, we would name it `FooTests.java`. We do not allow any code which is not a testcase to have a name ending in "Tests". All testcase must use annotations `(@Test)` instead of extending `junit.framework.TestCase`. Furthermore, the usage of `junit.framework` classes is generally discouraged. 

##Injection
For required dependencies, the constructor injection must be used whereas setters can be used for optional dependencies.

##equals() and hash() methods
The recommend way to build the `hash()` and `equals()` methods is to use the `EqualsBuilder` and `HashCodeBuilder `classes form the `commons-lang(3)` library.

##Template for commit messages
Short (50 chars or less) summary of changes.

More detailed explanatory text, if necessary.  Wrap it to about 72 characters or so.  In some contexts, the first line is treated as the subject of an email and the rest of the text as the body.  The blank line separating the summary from the body is critical (unless you omit the body entirely); tools like rebase can get confused if you run the two together. 

- Further paragraphs come after blank lines. 
- Bullet points are okay, too 
- Typically a hyphen or asterisk is used for the bullet, preceded by a  single space, with blank lines in between.
- The summary should contain both a Jira issue number where possible and a brief description, e.g.
{% highlight bash %}
CAS-31415: Upgrade JDK source version to Java 9. 
 
Upgrading to JDK version 9 in order to solve a number of problems with the code.
{% endhighlight %}

Placing _both_ the issue number and brief description would improve commit history in SCM. The old practice where the Jira number exclusively is on the first line makes it hard to follow commit history in a pull request and other situations.

##Checkstyle
The current codebase takes advantage of the [Checkstyle engine](checkstyle.sourceforge.net) to [enforce conventions](https://github.com/Jasig/cas/blob/master/checkstyle-rules.xml) as much as possible.
 

