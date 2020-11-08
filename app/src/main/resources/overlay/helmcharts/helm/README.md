## Helm Charts for CAS

The current helm chart for cas-server is a WIP. 
It needs work and probably won't maintain backwards compatibility.
Contributions welcome. Eventually it would be nice to support running containers with bootadmin and config-server
pre-configured to work, and to have cas-management available as its own helm chart (or running as part of this one).
The chart supports mapping in arbitrary volumes but should probably support a configmap in the values file with properties.  

### Install Kubernetes (Docker for Windows/Mac, Minikube, K3S, Rancher, etc)
[Docker Desktop](https://www.docker.com/products/docker-desktop)
[Minikube](https://minikube.sigs.k8s.io/docs/start/)

### Install Helm and Kubectl
Helm v3 and Kubectl are just single binary programs. Kubectl may come with your kubernetes 
installation, but you can download both of programs and put them in your path.
- Install [Helm](https://helm.sh/docs/intro/install/)
- Install [Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

### Install ingress controller
CAS helm chart only tested with Kubernetes ingress-nginx, feel free to add support for other ingress controllers.

[Kubernetes Nginx Ingress Installation Guide](https://kubernetes.github.io/ingress-nginx/deploy/)

### Create secret containing keystore
Assuming you have run `./gradlew createKeystore` or put you server keystore in `/etc/cas/thekeystore`,
run the following to create a secret containing the keystore: 
```
kubectl create secret generic cas-server-keystore --from-file=thekeystore=/etc/cas/thekeystore
```

### Install CAS Server helm chart
Helm charts consist of templates which are combined with values from one or more values files 
(and command line set arguments) to produce kubernetes yaml. The templates folder contains a default
values.yaml that is used by default but additional values files can be specified on the command line. 
The following examples use the `default` namespace but `--namespace cas` can be added to any resources
created by the helm command to use the specified kubernetes namespace. 
```
# delete cas-server helm chart install
helm delete cas-server
# install cas-server chart 
helm install cas-server ./cas-server
# install or update cas-server
helm upgrade --install cas-server ./cas-server
# use local values file to override defaults 
helm upgrade --install cas-server --values values-local.yaml ./cas-server
# see kubernetes yaml without installing  
helm upgrade --install cas-server --values values-local.yaml ./cas-server --dry-run --debug
```

### Useful `kubectl` Commands 
```
# tail the console logs
kubectl logs cas-server-0 -f
# exec into container
kubectl exec -it cas-server-0 sh
# bounce CAS pod
kubectl delete pod cas-server-0
```

### Browse to CAS
Make sure you have host entries for whatever host is listed in values file for this entry:
```
ingress:
  hosts:
    - host: kubernetes.docker.internal
      paths: 
        - "/cas"
  tls: 
    - secretName: cas-server-tls
      hosts:
        - kubernetes.docker.internal
```

```
# host entry
127.0.0.1 kubernetes.docker.internal
```
Browse to `https://kubernetes.docker.internal/cas/login`