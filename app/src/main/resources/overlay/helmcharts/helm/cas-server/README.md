This helm chart needs a lot of work, and this is mostly generated using helm create.

CAS is deployed as a StatefulSet mainly because it may prove easier for the pods to have well known names rather than the random names you get with Deployment.
Maybe at some point whether it is a StatefulSet or Deployment will be configurable. 

```
./gradlew clean build dockerJibBuild
cd helm
helm install cas-server ./cas-server
```

Issues:

Currently crashing on startup but not showing stacktrace. 