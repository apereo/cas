---
layout: default
title: CAS - MongoDb Authentication
---

# MongoDb Authentication
Verify and authenticate credentials against a [MongoDb](https://www.mongodb.org/) instance.

{% highlight xml %}
<alias name="mongoAuthenticationHandler" alias="primaryAuthenticationHandler" />
{% endhighlight %}

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-mongo</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

The following settings are applicable:

{% highlight properties %}
cas.authn.mongo.collection.name=users
cas.authn.mongo.db.host=mongodb://user:password@ds061954.somewhere.com:61954/database
cas.authn.mongo.attributes=attribute1,attribute2
cas.authn.mongo.username.attribute=username
cas.authn.mongo.password.attribute=password
{% endhighlight %}
