# Kubernetes

## Learning goals

* Using Kubernetes for application deployment
* Defining linevenss and readiness probes
* Specifying resource limits for containers
* Configuring applications with ConfigMaps

## Overview

In this excercise, you will see how to deploy Spring Boot applications on Kubernetes.

* Create a local cluster with `kind` or `minikube`.
* Define a `Deployment` manifest for Book Service.
* Configure Book Service via a `ConfigMap`.
* Define a `Service` manifest for Book Service.
* Apply the manifest to your local cluster.
* Apply the provided manifests to deploy Suggestion Service and Edge Service.

## Details

### Containerize applications

As a pre-requisite for deploying applications on Kubernetes, go into each project main folder (`begin/book-service`, `begin/suggestion-service`,
`begin/edge-service`) and run the following to package each application as a container image.

```bash
./gradlew bootBuildImage
```

### Creating a local cluster

In the `begin/kubernetes` folder, you'll find convenient scripts to set up a local cluster. If you'd like to use `kind`, run the following.

```bash
./create-cluster-kind.sh
```

If you'd rather use `minikube`, then run the following.

```bash
./create-cluster-minikube.sh
```

Both scripts will also deploy a `polar-postgres` PostgreSQL database in the cluster that you'll use from Book Service.

### Loading images into the local cluster

When working locally, the cluster would not have access to your localhost container registry. For that reason, we need to manually load the images.

If you're using kind, run the following to load all three images to the cluster.

```bash
$ kind load docker-image book-service --name workshop-cluster
$ kind load docker-image edge-service --name workshop-cluster
$ kind load docker-image suggestion-service --name workshop-cluster
```

On macOS with Apple Silicon, the previous command will not work. Instead, run the following.

```bash
$ docker save book-service | docker exec --privileged -i workshop-cluster-control-plane ctr --namespace=k8s.io images import --all-platforms -
$ docker save edge-service | docker exec --privileged -i workshop-cluster-control-plane ctr --namespace=k8s.io images import --all-platforms -
$ docker save suggestion-service | docker exec --privileged -i workshop-cluster-control-plane ctr --namespace=k8s.io images import --all-platforms -
```

If you're using minikube, run the following.

```bash
$ minikube image load book-service --profile workshop
$ minikube image load edge-service --profile workshop
$ minikube image load suggestion-service --profile workshop
```

### Defining a Deployment manifest for Book Service

Let's start defining the Kubernetes manifests for Book Service. In the root project (`begin/book-service`), create a `k8s` folder and add a `deployment.yml` file
as follows.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: book-service
  template:
    metadata:
      labels:
        app: book-service
    spec:
      containers:
        - name: book-service
          image: book-service
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
```

Then, configure liveness and readiness probes.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  ...
  template:
    ...
    spec:
      containers:
        - name: book-service
          ...
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 15
```

Next, it's important to define CPU and memory resources, both for the minimum guaranteed resources and the limits.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  ...
  template:
    ...
    spec:
      containers:
        - name: book-service
          ...
          resources:
            requests:
              memory: 756Mi
              cpu: "0.1"
            limits:
              memory: 756Mi
              cpu: "2"
```

### Configuring Spring Boot on Kubernetes with ConfigMaps

Spring Boot supports reading configuration data from ConfigMaps natively. We can use ConfigMaps to puplate environment variables or mount them as volumes
(recommended approach).

Let's first define a ConfigMap to configure the PostgreSQL URL and graceful shutdown in a new `configmap.yml` file.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: book-config
data:
  application.yml: |
    server:
      shutdown: graceful
    spring:
      lifecycle:
        timeout-per-shutdown-phase: 20s
      r2dbc:
        url: r2dbc:postgresql://polar-postgres:5432/catalog
```

Then, we can mount it to the Book Service container. Update `deployment.yml` as follows.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  ...
  template:
    ...
    spec:
      containers:
        - name: book-service
          ...
          volumeMounts:
            - name: book-config-volume
              mountPath: /workspace/config
      volumes:
        - name: book-config-volume
          configMap:
            name: book-config
```

Since we enabled graceful shutdown, we should take into account that Kubernetes is a distributed system and it can take a few seconds to propagate the information
throughout the cluster that a certain Pod is about to be shut down. For that reason, we can use a preStop hook and prevent having downtime due to signal delays.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  ...
  template:
    ...
    spec:
      containers:
        - name: book-service
          image: book-service
          imagePullPolicy: IfNotPresent
          lifecycle:
            preStop:
              exec:
                command: [ "sh", "-c", "sleep 5" ]
          ...

```

### Defining a Service manifest for Book Service

The previous Deployment is not accessible yet. Let's define a Service in a new `service.yml` file to expose it within the cluster.

```java
apiVersion: v1
kind: Service
metadata:
  name: book-service
  labels:
    app: book-service
spec:
  type: ClusterIP
  selector:
    app: book-service
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
```

### Running Book Service on Kubernetes

It's time to deploy Book Service. Form the project main folder, run the following command.

```bash
$ kubectl apply -f k8s
```

You can see the results with:

```bash
$ kubectl get all
```

Suggestion Service and Edge Service have already Kubernetes manifests defined. Go into each folder and deploy them as well.

```bash
$ kubectl apply -f k8s
```

Finally, you can access the application via the Ingress exposed from your local cluster. From there, Edge Service will give you access to the other
two applications.

```bash
$ http 127.0.0.1.sslip.io/books
$ http 127.0.0.1.sslip.io/suggestions
```
