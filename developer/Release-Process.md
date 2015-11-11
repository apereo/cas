---
layout: default
title: CAS - Release Process
---


# Release Process
This page documents the steps that a release engineer should take for cutting a CAS server release.


##Environment Review

- Set up your environment:
	- Load your SSH key on your local computer and ensure this SSH key is also referenced in Github
	- Load your `gradle.properties` file with the following

{% highlight bash %}
signing.keyId=
signing.password=
signing.secretKeyRingFile=/path/to/secring.gpg
{% endhighlight %}

- Checkout the CAS project:
{% highlight bash %}
mkdir casrelease
cd casrelease
git clone git@github.com:Jasig/cas.git
{% endhighlight %}

##Preparing the Release


##Performing the Release

Follow the process for [deploying artifacts to Maven Central](https://wiki.jasig.org/display/JCH/Deploying+Maven+Artifacts) via Sonatype OSS repository.  

- Log into [https://oss.sonatype.org](https://oss.sonatype.org).
- Find the staged repository for CAS artifacts created by the `mvn release:perform` goal.
- "Close" the repository.
- "Release" the repository.  Both c and d should be accompanied by email confirmation.

## Housekeeping

- Close [the milestone](https://github.com/Jasig/cas/milestones) for this release.
- Find [the release](https://github.com/Jasig/cas/releases) that is mapped to the released tag, update the description with the list of resolved/fixed issues and publish it as released. 

To generate the changelog and release notes, use the below steps:

1. Download and install [this tool](https://github.com/lalitkapoor/github-changes)
2. Generate a github access token [here](https://github.com/settings/tokens)
3. Execute the following command:

{% highlight bash %}
github-changes -o Jasig -r cas -b x.y.z -k <TOKEN> -a --use-commit-body
{% endhighlight %}

Note that `x.y.z` is the name of the branch that is released. The output will be saved in `ChangeLog.md` file. Comb
through the file, edit, format and paste the final content under the release tag. 


- Send an announcement message to @cas-announce, @cas-user and @cas-dev mailing lists. A template follows:

{% highlight bash %}
CAS Community,

CAS x.y.z is available for testing and evaluation. We encourage adopters to grab 
this release from Maven Central, integrate into your environment, and provide feedback.

Regards,
John Smith

{% endhighlight %}

##Post Release

###Documentation
- Documentation site is available under the `gh-changes` branch. Check it out.
- Copy the contents of the `development` directory to a new directory to match the new CAS release version (i.e `4.0.0`)
- Navigate to `Older-Version.md` page and include a link to the new directory that points to the new release.
- Modify the root `index.html` file of the `current` folder to point to the latest stable release such that `location.href = "../4.0.0/index.html";`
- Push the changes to the repository.





