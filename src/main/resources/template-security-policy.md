Thank you very much for submitting this pull request! 
 
# What happened? 

It appears that this pull request attempts to manually upgrade one or more CAS dependencies to a newer version, and this patch is likely targeted at a CAS branch that is no longer maintained and has reached end of life, or, the targeted CAS version has gone into maintenance mode. 

Please note that third-party libraries and dependencies are not upgraded to versions that have reached end-of-life status or remain in maintenance mode. The targeted CAS release will only receive security fixes and patches in response to security vulnerability reports that are accurately and privately reported to CAS. 

# Security fixes

Generally-speaking and given the amount of effort involved in planning and releasing a security patch and disclosing the issue, communicating descriptions, intent, attack window and fixes, etc it is generally best to keep security releases reserved for issues that **actually can be reproduced** based on a concrete use case or those that truly and in practice **affect the inner workings of the CAS software in a real way**. If you do have such a use case at some point, please be sure to supply details and steps to reproduce issues per the guidelines laid out by the project. Please do NOT comment on this pull request with such details publicly! Stating the fact that some library or dependency used by CAS has published a CVE is not relevant. A valid security report and one that would justify a library upgrade or a patch release **MUST** demonstrate and reproduce the applicability of the vulnerability as it regards CAS operations.

Supposed vulnerabilities that are picked up and reported by security scanners, static code analyzers and such affecting a third party library used by CAS generally do not qualify, unless there is solid evidence provided by the reporter to indicate a **real, practical issue** affecting CAS daily ops. Such warnings often appear superficial in the context of a CAS deployment. Usually, the best course of action would be to make the upgrades either locally to the installation script or switch to a more recent CAS version that might remove such warnings.

# Reviews
   
In scenarios where the dependency upgrade is reviewed and approved by the CAS project, **despite the release and only as an exception**, after due discussions, reviews and proper justification and research, please update the pull request body to signal the approval and review process to include the following syntax anywhere in the pull request description:

```
reviewed by: @[github-userid]
```
    
You may then re-open the pull request or ask the relevant project maintainers to do so for you, or publish a new pull request with the aforementioned changes and syntax. 

# Reporting security vulnerabilities

If you believe the changes in this patch fix a serious, practical security vulnerability found in a third-party library, you likely should not have disclosed this information so very publicly here. Doing so immediately negates the security aspect of it. That being said, please follow the project's security response guidelines to learn how to share details and come to a resolution. For additional details, please review the [security vulnerability response](https://apereo.github.io/cas/developer/Sec-Vuln-Response.html).
          
# Support

If you are seeking assistance or have a question about your CAS deployment, please visit [support options](https://apereo.github.io/cas/Support.html) to learn more.
