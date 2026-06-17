# Read Before Submissions

Thank you for your contributions to Apereo CAS. Read the notes below very carefully before you submit.

When you publish the pull request, keep the checklist below and please check off relevant items below in 
the description of your pull request. **DO NOT** remove the check list.

Please make sure you include the following:

- [ ] Brief description of changes applied
- [ ] Test cases for all modified changes, where applicable
- [ ] Test cases to demonstrate the problem before the fix, where applicable
- [ ] The same pull request targeted at the master branch, if applicable
- [ ] Any documentation on how to configure, test, etc
- [ ] Any possible limitations, side effects, performance issues, etc
- [ ] Reference any other pull requests that might be related

For more information, please see [this page](https://apereo.github.io/cas/developer/Contributor-Guidelines.html).

# Maintenance Branches

If your pull request targets a maintenance branch and is not directed at `master`, make sure you reference the pull request that
has already ported your changes forward to the `master` branch. You may do so by including the 
following in your pull request description:

```
master: https://github.com/apereo/cas/pull/$PR_NUMBER
```

You may also directly point at a commit that represents the change in the `master` branch
if the change is no longer needed there or has been replaced by another feature or fix:

```
master: https://github.com/apereo/cas/commit/$COMMIT_SHA
```

# Remember - Important

**DO NOT** keep this template as is when you submit, except for the above checklist. Please 
remove or adjust the description and the criteria here when you submit your pull request.

**DO NOT** submit pull requests and patches for possible security issues. 
See [security policy](https://apereo.github.io/cas/developer/Sec-Vuln-Response.html) for more information.

**DO NOT** submit a pull request to ask questions, seek guidance or support. Use the designated mailing lists to discuss
ideas, feature requests and bug fixes. See [mailing lists](https://apereo.github.io/cas/Mailing-Lists.html).

**DO NOT** submit pull requests without tests that verify or reproduce your intended use case. The pull request will
most likely be **AUTOMATICALLY CLOSED**, unless the change is extremely trivial. 

If your change-set attempts to solve a problem or defect, **BE SURE** to include tests to first and foremost demonstrate the issue. 
Tests must be able to reproduce the problem **without** changes and fixes first so that project maintainers can ascertain the 
validity of the issue scenario without resorting to manual steps. Tests and testing done after the fact is not acceptable.

**DO NOT** immediately spring into a coding session. If your change-set introduces a new CAS feature or improvement, 
please consider starting discussion with the 
project to see if your patch can be accepted and maintained by project owners and maintainers. Unless your change is 
extremely trivial and has been through proper review and is sponsored by a project maintainer, please do not spend 
time modifying code and configuration in a proposed pull request.
    
**DO NOT** submit pull requests to only upgrade dependencies and libraries. We have a semi-automated process in place
that handles all of that and @apereocas-bot does bulk of the work. Please 
see [security policy](https://apereo.github.io/cas/developer/Sec-Vuln-Response.html)
on how dependency upgrades are handled for security-related issues.

The pull request may be **AUTOMATICALLY CLOSED** by @apereocas-bot if it does not meet the above criteria. **DO NOT**
argue with the bot please. If you believe the closure is an error and the bot has made a mistake, please post your 
explanation here as a comment.

For more information, please see [this page](https://apereo.github.io/cas/developer/Contributor-Guidelines.html).
