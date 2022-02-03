# Aspen Mesh Packet Inspector

## Introduction

This repository contains a set of default configuration files to get an Aspen Mesh installation running with the Packet Inspector functionality.  This will allow you to demonstrate Packet Inspector functionality.

## Installation

***Make sure you are using the carrier grade version of Aspen Mesh***

``` bash
kubectl create ns istio-system

helm install istio-base <aspenmesh-directory>/manifests/charts/base --namespace istio-system

kubectl create secret generic cacerts -n istio-system --from-file=./certs/cluster1/ca-cert.pem --from-file=./certs/cluster1/ca-key.pem --from-file=./certs/cluster1/root-cert.pem --from-file=./certs/cluster1/cert-chain.pem 

kubectl label --overwrite namespace istio-system ca.istio.io/override=true

kubectl create ns analysis-emulator

kubectl label --overwrite namespace analysis-emulator ca.istio.io/override=true
    
helm install analysis-emulator -n analysis-emulator samples/aspenmesh/analysis-emulator/

helm install istiod <aspenmesh-directory>/manifests/charts/istio-control/istio-discovery/ -n istio-system --values aspenmesh-values.yaml

helm install istio-ingress <aspenmesh-directory>/manifests/charts/gateways/istio-ingress/ -n istio-system --values aspenmesh-values.yaml

```

## Demonstrating Packet Capture

There is a Git repository called [`ecommerce-microservices`](https://github.com/aspenmesh/se-workshop-demo-library/ecommerce-microservices).  You can install these microservices and submit a few orders to generate packets to be captured.  Note that you'll have to install an egress gateway for the `payment-service` if you want the external credit card processing API to succeed (not necessary to generate packets in the mesh): 

```
helm install istio-egress <aspenmesh-directory>/manifests/charts/gateways/istio-egress --namespace istio-system --values aspenmesh-values.yaml 

kubectl apply -f stripe-tls-service-entry.yaml

kubectl apply -f stripe-tls-egress-gateway.yaml
```

Once you have generated some traffic, you can view the packets in the `analysis-emulator`:


```
kubectl exec deploy/analysis-emulator -n analysis-emulator -- ls /tmp
```

You will see all of the `pcap` and `bson` files in the `/tmp` directory.  Select one of the `pcap` files and transfer it to your computer:

```
kubectl cp  analysis-emulator/<analysis emulator pod>:/tmp/<pcap file name> ./capture.pcap
```

You can then view the `pcap` file in [Wireshark](https://www.wireshark.org/)  or a similar tool.
