# Aspen Mesh Ingress and Egress Gateways

## Introduction

This repository describes how to set up ingress and egress gateways in Aspen Mesh.

## Installation

Install `istiod`, as well as the `istio-ingress` and `istio-egress` charts:

``` 
kubectl create ns istio-system

helm install istio-base <aspenmesh-directory>/manifests/charts/base -n istio-system

helm install istiod <aspenmesh-directory>/manifests/charts/istio-control/istio-discovery/ -n istio-system --values aspenmesh-values.yaml

helm install istio-ingress <aspenmesh-directory>/manifests/charts/gateways/istio-ingress/ -n istio-system --values aspenmesh-values.yaml

helm install istio-egress <aspenmesh-directory>/manifests/charts/gateways/istio-egress/ -n istio-system --values aspenmesh-values.yaml
```

The ingress and egress gateways are standalone Envoy proxy pods configured to handle inbound and outbound (north / south) traffic for the mesh:

```
kubectl get pods -n istio-system | grep -E "istio-(egressgateway|ingressgateway)"
istio-egressgateway-7946d6b4cb-jb9mk                  1/1     Running   0          9m19s
istio-egressgateway-7946d6b4cb-lrlxt                  1/1     Running   0          9m35s
istio-ingressgateway-d746f49f8-6zm5m                  1/1     Running   0          9m49s
istio-ingressgateway-d746f49f8-st89c                  1/1     Running   0          10m
```

There is a sample application in the [`command-runner`](https://github.com/brianjimerson/command-runner/tree/main) folder that can be installed and used to test the ingress and egress gateways:

```
kubectl apply -f command-runner/deploy/command-runner.yaml
```

## Ingress Gateway

Depending on your cloud provider, ingress gateways are exposed a number of ways.  In Amazon EKS they are exposed as AWS Load Balancers; for on-premise Kubernetes they could be implemented to through different ingress controllers such as Kong or Nginx:

```
kubectl get svc -n istio-system istio-ingressgateway
istio-ingressgateway             LoadBalancer   10.100.191.13    a0c04c5d96f1b40ac93ba7e238c9bbb4-1337916195.us-east-2.elb.amazonaws.com   15021:32070/TCP,80:31046/TCP,443:30532/TCP,15012:30075/TCP,15443:32260/TCP   11m
```

To configure an ingress gateway to route traffic to your application, you need to create a Gateway object that uses the `istio-ingressgateway` gateway, and a Virtual Service to route traffic to your pod.  For example, to route all traffic on port 80 to the `command-runner` application:

```
cat << EOF | kubectl apply -f -
---
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: command-runner-gw
  namespace: command-runner
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: command-runner-vs
  namespace: command-runner
spec:
  hosts:
  - "*"
  gateways:
  - command-runner-gw
  http:
  - route:
    - destination:
        host: command-runner 
        port:
          number: 8080
EOF
```

This configuration is also located in the [`command-runner/deploy/command-runner-gw.yaml`](https://github.com/brianjimerson/command-runner/tree/main/deploy/command-runner-gw.yaml) file:

```
kubectl apply -f command-runner/deploy/command-runner-gw.yaml
```

Open the `istio-ingressgateway` external host name to verify that traffic is being routed to the `command-runner` application:

```
open http://$(eval kubectl get svc -n istio-system -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" istio-ingressgateway)
```

## Egress Gateway

### Service Entry

By default, outbound traffic from the service mesh is allowed regardless if there is an `egress-gateway` or not.  To ensure that outbound traffic is allowed only if there is an internal registry entry for the external service, add this to your override values file:

```
meshConfig:
  outboundTrafficPolicy:
    mode: REGISTRY_ONLY
```

Note that this does not force all traffic through the `egress-gateway`; it only requires that a Service Entry exists for the external service.  In the `command-runner` application, try to `curl` an external service without a Service Entry:

```
curl http://www.google.com
```

And there should be no response, since the request is sent to the `black hole` cluster in Aspen Mesh.

Add a Service Entry for the external service, and try the `curl` command again.  You should see a valid response from the external service:

```
cat << EOF | kubectl apply -f -
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: google
  namespace: istio-system
spec:
  hosts:
  - www.google.com
  - google.com
  ports:
  - number: 80
    name: http
    protocol: HTTP
  - number: 443
    name: tls
    protocol: TLS
  resolution: DNS
EOF
```

This configuration is also located in the [`google-service-entry.yaml`](google-service-entry.yaml) file:

```
kubectl apply -f command-runner/deploy/google-service-entry.yaml
```

Note that applying this Service Entry to the `istio-system` namespace effectively makes it available to all namespaces.  You can also scope this Service Entry to a particular namespace for your application.

### Sending Traffic Through Egress Gateway

Adding a Service Entry does *not* configure egress traffic to go through the `istio-egressgateway`.  To configure egress traffic to go through the `istio-egressgateway`, add a Gateway, Destination Rule, and Virtual Service for the application:

```
cat << EOF | kubectl apply -f -
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: istio-egressgateway
  namespace: command-runner
spec:
  selector:
    istio: egressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - www.google.com
    - google.com
  - port:
      number: 443
      name: tls
      protocol: TLS
    hosts:
    - www.google.com
    - google.com
    tls:
      mode: PASSTHROUGH
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: egressgateway-for-google
  namespace: command-runner
spec:
  host: istio-egressgateway.istio-system.svc.cluster.local
  subsets:
  - name: google
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: direct-google-through-egress-gateway
  namespace: command-runner
spec:
  hosts:
  - www.google.com
  - google.com
  gateways:
  - mesh
  - istio-egressgateway
  http:
  - match:
    - gateways:
      - mesh
      port: 80
    route:
    - destination:
        host: istio-egressgateway.istio-system.svc.cluster.local
        subset: google
        port:
          number: 80
  - match:
    - gateways:
      - istio-egressgateway
      port: 80
    route:
    - destination:
        host: www.google.com
        port:
          number: 80
  tls:
  - match:
    - gateways:
      - mesh
      port: 443
      sniHosts:
      - www.google.com
    route:
    - destination:
        host: istio-egressgateway.istio-system.svc.cluster.local
        subset: google
        port:
          number: 443
  - match:
    - gateways:
      - istio-egressgateway
      port: 443
      sniHosts:
      - www.google.com
    route:
    - destination:
        host: www.google.com
        port:
          number: 443
EOF
```
This configuration is also located in the [`google-egress-gateway.yaml`](google-egress-gateway.yaml) file:

```
kubectl apply -f command-runner/deploy/google-egress-gateway.yaml
```

With this configuration applied, you should now see all egress traffic to `www.google.com` going through the `istio-egressgateway`.  To test, execute this command in `command-runner`:

```
curl http://www.google.com
```

Inspect the logs in the `istio-egressgateway` pod and verify the HTTP request is going through the `istio-egressgateway`:

```
kubectl logs -n istio-system istio-egressgateway-7946d6b4cb-lrlxt

[2021-09-10T16:47:30.115Z] "GET / HTTP/2" 200 - via_upstream - "-" 0 14892 141 123 "192.168.77.145" "curl/7.64.0" "a829b98e-001a-41af-9e0e-1bea4f163111" "www.google.com" "142.250.191.196:80" outbound|80||www.google.com 192.168.29.202:39050 192.168.29.202:8080 192.168.77.145:52826 - -
```



