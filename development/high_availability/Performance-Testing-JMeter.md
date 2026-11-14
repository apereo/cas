# JMeter Performance Testing

Source: https://apereo.github.io/cas/development/high_availability/Performance-Testing-JMeter.html

Apache JMeter is a great performance testing tool that is used heavily within the Java community.

## Install JMeter

- Download the [JMeter binary](http://jmeter.apache.org/download_jmeter.cgi).
- Unzip apache-jmeter-*.tgz to your preferred location
- Run `bin/jmeter.sh|bat`
  - Note: Mac users can also use the popular HomeBrew package manager to install JMeter.

## Sample Test Scripts

Below you will find three generic runnable login scripts for the three most popular CAS implementation flavors. Please feel free to edit and use for your needs.

Although the scripts support different login methodologies, they do share some common traits.

## Common Settings

- *User Defined Variables*
  - *ThreadCount* - Number of Threads (Kind of like Users). Recommend starting with 100 users or so.
  - *Duration* - How long should the test run. Usually, the more threads(users) the longer the duration should be
  - *RampUpPeriod* - How long to take to ramp up to full set of thread count
- *Thread Group (or Tests)*:
  - *Loop Count* - # of Loops, or more correctly the # of users to run through the test.
    - Count will be associated to the total users that will run through the test
    - *Forever* check box will loop through file and keep going till manually stopped or until reaches Duration from “User Defined Variables” page
- *CSV Get Users/Passwords*:
  - Name and location of file containing test user credentials
  - Should be in the format of `User,Password`, with no spaces between “User”, the “comma” and “Password”

## Scripts

The scripts can be downloaded from [here](https://github.com/apereo/cas/raw/master/etc/loadtests/jmeter).

### CAS Protocol

Test script: `CAS_CAS.jmx` used to test the server acting as a CAS identity provider.

- Vanilla installation of CAS using standard CAS login process
- No SP (Service Provider) is needed
- User Defined Variables:
  - *IdPHost* - URL of your CAS instance
  - *CasSP* - SP (Service Provider) URL but does not have to be active
- Test Fragments:
  - *GET - CAS Login Page* – Access login page for a typical CAS login
  - *POST - Login Credentials* – Post credentials from user file into CAS instance
  - *GET - User Info with Service Ticket* – Get user info with Service Ticket that CAS generated when user logged in
    - Under Assertion, may need to update expected user results
  - *GET - User Logout* – Logout user from CAS session via CAS logout

### OAuth Protocol

Test script: `CAS_Oauth.jmx` used to test the server acting as an OAuth provider.

- CAS supporting OAuth login process
- An active SP is optional
- Script reflects the most common way that OAuth is used, the Authorization Code method
- *User Defined Variables*:
  - *IdPHost* - URL of your CAS instance
  - *CasSP* - SP (Service Provider) URL but does not have to be active
  - *SpClientId* - The clientId of the SP within the CAS service file
  - *SpRedirectUri* - Endpoint in SP that will be used to receive the “Authorization Code”
  - *SpState* - CSRF token used
  - *SpClientName* - The OAuth call type being used for authentication
  - *SpResponseType* - The OAuth method being used, in this case “code”, which stands for *Authorization Code*
  - *SpClientSecret* - Secret phrase or word shared between the SP and CAS
- *Test Fragments*:
  - *Verify Service Provider* – Verifies URL to SP is correct (Optional, can be disabled)
  - *Start CAS Login process* – Accessing CAS login page for OAuth with all parameters set
  - *1a-1d* – Post login credentials for user, followed by redirects to get code in Access Token
    - broken into several processes due to encoding issues when testing
  - *GET - User Profile with Access Token* – Call to CAS to get the user’s info with Access Token
    - Under Assertion, may need to update expected user results
  - *GET - User Logout* – Logout user from CAS session via CAS logout

### OpenID Connect Protocol

Test script: `CAS_OIDC.jmx` used to test the server acting as an OpenID Connect provider.

The instructions and the test sequence is almost identical to the OAuth workflow, with the addition of ID token verifications.

### SAML2 Protocol

Test script: `CAS_SAML2.jmx` used to test the server acting as a SAML2 identity provider.

- CAS support for SAML2 Login process
- An active SP is required.
  - For this test used SimpleSAMLphp
- *User Defined Variables*:
  - *CasSP* - Domain of registered CAS SP using SAML
  - *ProviderId* - SAML EntityID stated in metadata for SP
- *Test Fragments*:
  - *Go To SP for CAS Login* – SP page protected by SAML2 that will redirect to CAS login endpoint
  - *POST - Login User* – Post credentials from user file into CAS SAML2 login
  - *POST - CAS Authorization to SP* – Send response from CAS to SP for processing and final request for user info
    - May need to updated Assertion for successful user information returned
  - *GET - User Logout* – Logout user from CAS session via CAS logout

## Run Test Scripts

Once you have saved the test scripts to your system, you can either run within the JMeter GUI or via command line. It is highly recommended that the GUI be used for troubleshooting the scripts to work within your environment. Then, when you actually start load testing, you do that via the command line.

To activate the JMeter GUI, from the command line type:

```bash
> /usr/local/bin/jmeter
```

This path should correspond to the location you chose to install JMeter.

A simple example of a JMeter startup via command line:

```bash
> /usr/local/bin/jmeter -n -t your_script.jmx
```

`-n` run JMeter in non-GUI mode. `-t` path to .jmx test file.

More examples can be found on the [JMeter site](http://jmeter.apache.org/).
