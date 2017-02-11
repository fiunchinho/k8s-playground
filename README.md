# k8s Workshop

## Minikube
Minikube is a tool that makes it easy to run Kubernetes locally.
Minikube runs a single-node Kubernetes cluster inside a VM on your laptop.

Installing minikube is as easy as downloading both minikube and kubectl binaries

```bash
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.11.0/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
curl -Lo kubectl http://storage.googleapis.com/kubernetes-release/release/v1.4.1/bin/darwin/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/
```

Let's create our single-node Kubernetes cluster with minikube

```bash
$ minikube start
```

Now it's like you have a Kubernetes "cluster" that consist on one machine. You can even ssh to that machine. We'll never need to do that, though.

```bash
$ kubectl get nodes
$ kubectl cluster-info
$ minikube ssh
```

Minikube also comes with a Docker engine so we can use regular docker commands without having Docker installed.
To use docker inside minikube just point the docker client to minikube

```bash
$ eval $(minikube docker-env)
```

## Running applications
Our `hello1` application responds to the `/foo` and `/bar` endpoints. You can run it locally

```bash
$ cd hello1 && ./gradlew clean bootRun
```

Let's deploy the `hello1` application to kubernetes

```bash
$ kubectl apply -f hello1/deployment.yml
```

The deployment object that we just created specified the number of replicas that we want of this pod.
We start with 3 running pods like we can see below

```bash
$ kubectl get pods
NAME                     READY     STATUS    RESTARTS   AGE
hello1-2221995482-kk8nb   1/1       Running   0          3m
hello1-2221995482-c48e3   1/1       Running   0          3m
hello1-2221995482-mlj88   1/1       Running   0          3m
```

And we have a service for all of them, so we have an stable endpoint to use when we want to communicate with these pods.

```bash
$ kubectl apply -f hello1/service.yml
$ kubectl describe services hello1
Name:                   hello1
Namespace:              default
Labels:                 <none>
Selector:               app=hello1
Type:                   ClusterIP
IP:                     10.0.0.215
Port:                   <unset> 80/TCP
Endpoints:              172.17.0.4:8000,172.17.0.5:8000,172.17.0.7:8000
Session Affinity:       None
No events.%
```

Our service has the stable IP `10.0.0.215` and sits in front of our pods (`172.17.0.4`, `172.17.0.5` and `172.17.0.7`), load balancing the traffic coming to the pods.
As we'll see now, the service IP remains the same even during deployments.

The service knows which endpoints to serve based on the label selector that we chose: it only serves endpoints from pods that have a label called `app` with the value `hello1`.

Let's deploy a new version of our application, updating the version of the container image in the deployment object. We'll use a new version like `latest`, and see what happens

```bash
$ ./hello1/deploy.sh k8s-hello1 latest
deployment "hello1" configured
$ kubectl get pods
NAME                     READY     STATUS              RESTARTS   AGE
hello1-177141720-5nlly    0/1      ContainerCreating   0          1s
hello1-177141720-jkkwp    0/1      ContainerCreating   0          2s
hello1-2221995482-c48e3   1/1      Running             0          4m
hello1-2221995482-mlj88   1/1      Running             0          4m
```

One of our pods have been terminated, while two new pods are starting.
This is reflected on our service as well.

```bash
$ kubectl describe services hello1
Name:                   hello1
Namespace:              default
Labels:                 <none>
Selector:               app=hello1
Type:                   ClusterIP
IP:                     10.0.0.215
Port:                   <unset> 80/TCP
Endpoints:              172.17.0.4:8000,172.17.0.5:8000
Session Affinity:       None
No events.%
```

The pod that got terminated is no longer listening behind our service, so no traffic will be routed there.

Let's wait for our pods to get ready.

```bash
$ kubectl get pods
NAME                     READY     STATUS    RESTARTS   AGE
hello1-177141720-5nlly    0/1      Running   0          39s
hello1-177141720-jkkwp    0/1      Running   0          40s
hello1-2221995482-c48e3   1/1      Running   0          5m
hello1-2221995482-mlj88   1/1      Running   0          5m
$ kubectl get pods
NAME                     READY     STATUS              RESTARTS   AGE
hello1-177141720-8ul2s    0/1      ContainerCreating   0          1s
hello1-177141720-5nlly    1/1      Running             0          41s
hello1-177141720-jkkwp    1/1      Running             0          42s
hello1-2221995482-c48e3   1/1      Terminating         0          5m
hello1-2221995482-mlj88   1/1      Terminating         0          5m
```

When the two new pods are ready to receive traffic, a new pod is created (that makes 3 replicas for the new version, as declared in our deployment), and the remaining old pods are terminated.

Our service is aware of this

```bash
$ kubectl describe services hello1
Name:                   hello1
Namespace:              default
Labels:                 <none>
Selector:               app=hello1
Type:                   ClusterIP
IP:                     10.0.0.215
Port:                   <unset> 80/TCP
Endpoints:              172.17.0.6:8000,172.17.0.7:8000
Session Affinity:       None
No events.%
```

Finally, we ended up with our only our 3 new pods up and running behind our service.

```bash
$ kubectl get pods
NAME                    READY     STATUS    RESTARTS   AGE
hello1-177141720-8ul2s   0/1       Running   0          10s
hello1-177141720-5nlly   1/1       Running   0          50s
hello1-177141720-jkkwp   1/1       Running   0          51s
$ kubectl get pods
NAME                    READY     STATUS    RESTARTS   AGE
hello1-177141720-8ul2s   1/1       Running   0          31s
hello1-177141720-5nlly   1/1       Running   0          1m
hello1-177141720-jkkwp   1/1       Running   0          1m
$ kubectl describe services hello1
Name:                   hello1
Namespace:              default
Labels:                 <none>
Selector:               app=hello1
Type:                   ClusterIP
IP:                     10.0.0.215
Port:                   <unset> 80/TCP
Endpoints:              172.17.0.6:8000,172.17.0.7:8000,172.17.0.8:8000
Session Affinity:       None
No events.%
```

You can also see all the events that happened during this deployment

```bash
$ kubectl describe deployments hello1
Name:                   hello1
Namespace:              default
CreationTimestamp:      Sat, 15 Oct 2016 12:10:18 +0200
Labels:                 app=hello1
                        environment=pro
Selector:               app=hello1,environment=pro
Replicas:               3 updated | 3 total | 3 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  1 max unavailable, 1 max surge
OldReplicaSets:         <none>
NewReplicaSet:          hello1-2221995482 (3/3 replicas created)
Events:
  FirstSeen     LastSeen        Count   From                            SubobjectPath   Type            Reason                  Message
  ---------     --------        -----   ----                            -------------   --------        ------                  -------
  40m           40m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-177141720 to 1
  40m           40m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-2221995482 to 2
  40m           40m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-177141720 to 2
  39m           39m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-2221995482 to 0
  39m           39m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-2221995482 to 1
  39m           39m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-177141720 to 3
  22m           22m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-1545664080 to 1
  22m           22m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-177141720 to 2
  22m           22m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-1545664080 to 2
  17m           17m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-2221995482 to 2
  17m           17m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-1545664080 to 0
  44m           17m             2       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled up replica set hello1-2221995482 to 3
  17m           17m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-177141720 to 1
  17m           17m             1       {deployment-controller }                        Normal          ScalingReplicaSet       Scaled down replica set hello1-177141720 to 0
```

Or if you prefer a nice UI

```bash
$ minikube dashboard
```

All external traffic coming towards our k8s cluster will go through the same point: the ingress controller. All our services will be behind this controller.
First, let's create a default service which will always return 404. This is just to make sure that at least one service is running behind our ingress controller.

```bash
$ kubectl apply -f default-backend-service.yml
$ kubectl apply -f default-backend.yml
```

Now let's use the Nginx ingress from Kubernetes. This will deploy an nginx server that will reload its configuration when services are created/destroyed/scaled.
How this nginx will route traffic depends on our ingress rules. Let's create some rules to route based on host name

```bash
$ kubectl apply -f nginx-ingress.yml
$ kubectl apply -f ingress.yml
$ kubectl get ingress
$ kubectl describe ingress hello
```

You can even check the nginx config file rendered

```bash
$ kubectl exec nginx-ingress-controller-4046061302-pzwuv cat /etc/nginx/nginx.conf
```

How does it change when we scale our deployment?

```bash
$ kubectl scale deployments hello --replicas=3
$ kubectl exec nginx-ingress-controller-4046061302-pzwuv cat /etc/nginx/nginx.conf
```

Don't forget to clean after yourself!

```bash
$ kubectl delete deployment hello
$ kubectl delete services hello
$ minikube stop
```

```bash
$ kubectl get replicasets
$ kubectl rollout history deployment hello
```