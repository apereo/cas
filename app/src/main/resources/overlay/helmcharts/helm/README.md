## Helm Chart for CAS

The current helm chart for cas-server demonstrates standing up CAS with a Spring Boot Admin Server.
The chart functionality will grow over time, hopefully with contributions from real world deployments. 
Eventually it might be nice to support a config-server and have cas-management available.  
The chart supports mapping in arbitrary volumes and cas config can be specified in values files. 
The config could be in cloud config rather than kubernetes config maps, the service registry 
could be in a database, git, or a simple json registry in a kubernetes persistent volume. The ticket registry could use a standard helm chart for redis, 
postgresql, or mongo, etc. 
Currently the chart is attempting to use SSL between ingress controller and the CAS and Boot Admin servers. 
This is probably overkill and involves all the pain that comes with SSL (e.g. trust & hostname verification).
This chart uses stateful set for CAS rather than a deployment and this may change in the future.
The bootadmin CAS server discovery method should probably change to "cloud" method eventually.  

#### Warning: semver versioning will not be employed until published to a repository.

### Install Kubernetes (Docker for Windows/Mac, Minikube, K3S, Rancher, etc)

  - [Docker Desktop](https://www.docker.com/products/docker-desktop)

  - [Minikube](https://minikube.sigs.k8s.io/docs/start/)

  - [k3s](https://k3s.io/) - Works on linux, very light-weight and easy to install for development
    ```shell script
    curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="server --disable traefik" sh
    # the following export is for helm
    export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
    ./gradlew clean build jibBuildTar --refresh-dependencies
    k3s ctr images import build/jib-image.tar
    k3s ctr images ls | grep cas
    ./gradlew createKeystore
    cd helm 
    # create secret for tomcat
    kubectl create secret generic cas-server-keystore --from-file=thekeystore=/etc/cas/thekeystore
    # create secret for ingress controller to use with CAS ingress (nginx-ingress will use default if you don't create)
    ./create-ingress-tls.sh
    # install cas-server helm chart
    helm upgrade --install cas-server ./cas-server
    ``` 

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
```shell script
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
# sometimes dry-run fails b/c yaml can't convert to json so use template instead to see problem
helm template cas-server --values values-local.yaml ./cas-server --debug
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
    - host: cas.example.org
      paths: 
        - "/cas"
  tls: 
    - secretName: cas-server-ingress-tls
      hosts:
        - cas.example.org
```

```
# host entry
127.0.0.1 cas.example.org 
```
Browse to `https://cas.example.org/cas/login`
