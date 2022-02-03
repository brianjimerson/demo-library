#!/bin/bash

kubectl delete -f stripe-secret.yaml
kubectl delete -f rabbitmq.yaml
kubectl delete -f payment-history-service.yaml
kubectl delete -f payment-service.yaml
kubectl delete -f order-service.yaml
kubectl delete -f catalog-service.yaml
kubectl delete -f catalog-ui.yaml
kubectl delete -f catalog-ui-gateway.yaml
kubectl delete ns ecommerce

exit 0
