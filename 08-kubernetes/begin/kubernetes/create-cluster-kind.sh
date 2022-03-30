#!/bin/sh

echo "\n📦 Initializing Kubernetes cluster...\n"

kind create cluster --config kind-config.yml

sleep 5

echo "\n🔌 Installing NGINX Ingress...\n"

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

echo "\n⌛ Waiting for NGINX Ingress to be ready...\n"

sleep 10

kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=300s

sleep 5

echo "\n📦 Deploying PostgreSQL..."

kubectl apply -f services/postgresql.yml

sleep 5

echo "\n⌛ Waiting for PostgreSQL to be deployed..."

while [ $(kubectl get pod -l db=polar-postgres | wc -l) -eq 0 ] ; do
  sleep 5
done

echo "\n⌛ Waiting for PostgreSQL to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=db=polar-postgres \
  --timeout=180s

echo "\n⛵ Happy Sailing!\n"