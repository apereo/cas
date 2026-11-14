# SSO Public Workstations

Source: https://apereo.github.io/cas/development/authentication/Configuring-SSO-Public-Workstations.html

CAS has the ability to allow the user to opt-out of SSO, by indicating on the login page that the authentication is happening at a public workstation. By electing to do so, CAS will not honor the subsequent SSO session and will not generate the TGC that is designed to do so.

```html
...
<input id="publicWorkstation"
       name="publicWorkstation"
       value="false"
       type="checkbox" />
<label for="publicWorkstation" th:utext="#{screen.welcome.label.publicstation}"/>
...
```
