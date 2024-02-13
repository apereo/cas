Thank you very much for submitting this pull request! You are off to a very good start.

# What happened?

Your pull request is currently at commit id `${commitId}`. We looked at your [forked repository](${forkedRepository}) to find this commit id to verify if there are any [*GitHub Actions workflow runs*](${forkedRepository}/actions) on your forked repository that are run for this commit id, so we can determine if the test suite has in fact run prior to the submission here.

Just in case: you do not have to wait for the CAS project or its members to run tests for you here. You can do them yourself.

We have not found any successful workflow runs on your forked repository that would show passing tests for this commit id. Our decision at this point is that the CAS test suite has not run (successfully) on your fork to verify how your changes affect all CAS functionality. They might indeed verify the particular functionality you have worked on, but we need to ensure that the changes do not break other parts of CAS.

The following [*GitHub Actions workflow runs*](${forkedRepository}/actions) are missing on your fork: `${missingRuns}`.

![image](${link})

# Remember

- If you believe this message to be an error, please post your explanation here as a comment and it will be reviewed.
- If you are a member of the Apereo Foundation and do support the project financially, please post a note here.

# What's next?

The CAS project runs its test suite via [GitHub Actions](https://github.com/apereo/cas/actions). Our subscription to GitHub Actions is now paid for and sponsored by Apereo foundation members and can no longer be run freely. To proceed, you should activate GitHub Actions for your [forked repository](${forkedRepository}) and allow it to run the CAS test suite for you. Once tests and workflows pass for your most recent commit, please post a note here and we can resume with the review proceed.
         
Furthermore, your current changeset starts off with the branch `${branch}`. To activate GitHub Actions and workflow runs for your forked repository, it would be best if you rename your branch to match `pr-${branch}`. Your branch name essentially should start with `pr-` and then followed by the name of your branch. This allows the right jobs and runs to trigger correctly.

If you have done so already, please ignore the digital dust here and move on.

# Why is this happening?

- We intend to use run CAS GitHub Actions workflows for Apereo Foundation members that pay for this subscription and help the project pay its bills. If you would like to use this service free of charge, GitHub does allow you to run the same workflows on your [forked repository](${forkedRepository}), which is, as of this writing, is completely free.
- It would be ideal for the CAS project to determine and evaluate that contributors have in fact run the test suite before money can be spent on evaluating if the same tests pass here. If you are a contibutor and a sponsoring member, please speak up.
- Running the test suite on your forked repository also allows you to verify that your changes do not break any existing functionality and puts you in full control of the changes and the workflow runs. You can stop, start and resume the workflow runs as you see fit, and you do not have to compete with the rest of the project members or contributors for GitHub Actions CI time and availability of hardware, since your time on your own [forked repository](${forkedRepository}) is yours and yours only to use as you see fit. As a result, you will make progress much faster, without having to wait for a Github Actions workflow job to start and complete here.
- It also makes the review process that much quicker and easier, since we can focus on the changes and the code and not worry about the test suite and the test results and will stop bothering you with review comments such as *this change violates the style guide*, *this breaks the build*, *this breaks the test suite*, etc. 
       
# Is this more difficult?

Yes, indeed. You as the contributor are now asked to:

1. Write tests, verify behavior, check code style, etc.
2. Run the test suite on your [forked repository](${forkedRepository}).
3. Demonstrate that you have run the test suite and everything works and runs as it should be.
                                                                              
It's more work. You also get to make progress quicker and you are in control of your time. All free of charge.
 
# Is this better?

Quite so. As discussed, your progress is that much quicker and is challenged with only a few dependencies and roadblocks. It is true that the Apereo CAS test suite that runs here is the most comprehensive of them all, and not all tests can comprehensively run on a forked repository as some require special configuration, settings and tokens. However, the majority of the tests can run on your forked repository and getting them to pass is a major milestone.

More tests simply equal more confidence and a better project.

# Resources

To learn about CAS test process and how you may design and develop unit/integration tests, please [see this link](https://apereo.github.io/cas/developer/Test-Process.html). If you are unclear on how to write basic unit tests, please [see this link](https://junit.org/).

# Support

If you are seeking assistance or have a question about your CAS deployment, please visit [support options](https://apereo.github.io/cas/Support.html) to learn more.

Thanks again!
   

