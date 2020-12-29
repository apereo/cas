The following properties are additionally supported, when delegating authentication to GitHub:

```properties
# cas.authn.pac4j.github.scope=user|read:user|user:email|...
```       

The default scope is `user`, i.e. `read/write` access to the GitHub user account.

For a full list of possible scopes, please [see this link](https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/). 
