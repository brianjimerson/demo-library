#!/bin/bash

kubectl create ns ecommerce
kubectl label namespace ecommerce istio-injection=enabled
kubectl apply -f stripe-secret.yaml
kubectl apply -f rabbitmq.yaml
kubectl apply -f payment-history-service.yaml
kubectl apply -f payment-service.yaml
kubectl apply -f order-service.yaml
kubectl apply -f catalog-service.yaml
kubectl apply -f catalog-ui.yaml
kubectl apply -f catalog-ui-gateway.yaml

exit 0
