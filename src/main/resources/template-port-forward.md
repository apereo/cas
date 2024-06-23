Thank you very much for submitting this pull request!
       
# What happened? 

This pull request is targeted at a maintenance branch. However, there is no parallel pull request that would port the same functionality forward to the `master` branch. Remember that changes applied to previous branches that target CAS versions in maintenance mode **MUST** be ported forward to the `master` branch as well, via a separate pull request.

![image](https://github.com/apereo/cas/assets/1205228/bef10365-2879-40eb-aa0d-25491f3d264b)

If you already have worked out the targeted-at-`master` pull request, preferably one that is merged, please update the body of your pull request description here to include the following reference:

```
master: https://github.com/apereo/cas/pull/$PR_NUMBER
```
            
`$PR_NUMBER` must be replaced by the actual pull request number that targets the `master` branch.

If your pull request does not apply to the `master` branch, because the change here is no longer needed there or has been replaced by another feature or fix, please update the body of your pull request description here to include the following reference:

```  
master: https://github.com/apereo/cas/commit/$COMMIT_SHA
```

`$COMMIT_SHA` must be replaced by the actual commit SHA that represents the change in the `master` branch.

# How to contribute

The recommended contribution strategy is one that would have you start with the `master` branch first. Once the change is accepted and merged, you would then be able to port the change back to the appropriate maintenance branch provided the change fits the scope of the maintenance branch and its tracking release and assuming the branch is still under care, you are more than welcome to move changes across the codebase various branches as much as needed to remove pain and improve.

If you'd like to keep the patch around and open while you make progress, please mark the patch as `WIP` using one of the following methods:

- Construct your pull request title using the syntax `WIP - <title>` 
- Create the initial pull request in "Draft" status.

Please re-open the pull request (or ask project maintainers to do this for you) when you're ready

# Resources

Furthermore,

- To learn about CAS test process and how you may design and development unit/integration tests, please [see this link](https://apereo.github.io/cas/developer/Test-Process.html). If you are unclear on how to write basic unit tests, please [see this link](https://junit.org/).
- See [Contributor Guidelines](https://apereo.github.io/cas/developer/Contributor-Guidelines.html) to learn more about how to contribute to the project and review the criteria that allows for the project to accept contributions.
- If you are seeking assistance and have a question about your CAS deployment, please visit [this page](https://apereo.github.io/cas/Support.html) to learn more about support options.
- If your change-set attempts to solve a problem or defect, be sure to include tests to first and foremost demonstrate the issue. Tests must be able to reproduce the problem **without** changes and fixes first so that project maintainers can ascertain the validity of the issue scenario without resorting to manual steps. Tests and testing done after the fact is not acceptable.
- If your change-set introduces a new CAS feature or improvement, please consider starting discussion with the project to see if your patch can be accepted and maintained by project owners and maintainers. Unless your change is extremely trivial and has been through proper review and is sponsored by a project maintainer, please do not spend time modifying code and configuration in a proposed pull request. 

If you believe this message to be an error, please post your explanation here as a comment and it will be reviewed.

# Support

If you are seeking assistance or have a question about your CAS deployment, please visit [support options](https://apereo.github.io/cas/Support.html) to learn more.

Thanks again!

