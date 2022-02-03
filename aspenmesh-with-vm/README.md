# Aspen Mesh with VMs

## Introduction

This repository demonstrates how to add external VMs to Aspen Mesh.  This demonstration uses an external RabbitMQ server VM as the message bus for the [`ecommerce-microservices`](https://github.com/brianjimerson/ecommerce-microservices)  application.  The RabbitMQ VM will have an Envoy proxy installed and joined to the mesh.

This demonstration assumes that you have a Kubernetes cluster in AWS and are going to install RabbitMQ as a standalone EC2 instance in the same VPC, running Ubuntu / Debian Linux.  Other environments should be similar to set up.

## RabbitMQ Setup

### EC2 Setup

First, create an EC2 instance based on Ubuntu 20.04.  A t2.xlarge instance type with a 60GB volume was used for this demonstration.  Also, make sure you assign a public IP address to the instance so that you can access the RabbitMQ Management Console.  If you don't, you'll have forward ports for the console.  The security group for the instance should have the following inbound rules:

 * 5672/TCP from at least the subnets for your Kubernetes cluster (RabbitMQ protocol)
 * 15672/TCP from everything (HTTP port for the Management Console)
 * 22/TCP from everything (SSH port)

### RabbitMQ Installation

Once the EC2 instance is available, establish an SSH session to the instance.  Install RabbitMQ with the Management Console enabled with the following commands:

```
sudo apt-get update
sudo apt install -y rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management
```

By default the guest user can only access the Management Console from `localhost`.  Add an admin user that can access the Management Console from anywhere, and give the user permissions to the default vhost:

```
sudo rabbitmqctl add_user admin password
sudo rabbitmqctl set_user_tags admin administrator
sudo rabbitmqctl set_permissions --vhost / admin  ".*" ".*" ".*"
```

You should be able to access the Management Console at `http://<ec2-instance-public-address>:15672` with the admin user you created.

## Aspen Mesh Installation

Install Aspen Mesh as you normally would.  If you use a values override file other than the one in this repository, make sure you enable Workload Entry Autoregistration like this:

```
pilot:
  env:
    PILOT_ENABLE_WORKLOAD_ENTRY_AUTOREGISTRATION: true
    PILOT_ENABLE_WORKLOAD_ENTRY_HEALTHCHECKS: true
```

_**There is a bug in `istioctl` v1.9.x with Helm installations**_. See issue [AM-3318](https://aspenmesh.atlassian.net/browse/AM-3310) for details.  Make sure that you have the following value override set, or the `istioctl x workload entry` step in the [VM setup](#vm-setup-1) will fail.

```
meshConfig.defaultConfig.proxyMetadata = {}
```

_Note: Do not set values for `global.multiCluster.x` properties.  This will cause the east-west gateway to fail authentication._

From the Aspen Mesh release directory:

```
# Edit this file as needed
export AM_VALUES=<path-to-this-repository>/aspenmesh-values.yaml

kubectl create ns istio-system
helm install istio-base manifests/charts/base -n istio-system
helm install istiod manifests/charts/istio-control/istio-discovery/ -n istio-system --values $AM_VALUES
```

Install the east-west gateway, which allows traffic between the VM and Kubernetes.  Note that the `istioctl` binary should be from the open source Istio distribution, not Aspen Mesh.  The Aspen Mesh `istioctl` tries to pull images from a private repository and fails authentication.  _TODO: figure out how to pull private Aspen Mesh images_

```
samples/multicluster/gen-eastwest-gateway.sh --single-cluster | istioctl install -y -f -
```

And expose the Aspen Mesh control plane services to the east-west gateway:

```
kubectl apply -n istio-system -f samples/multicluster/expose-istiod.yaml
```

## VM Setup

### Environment variables and namespace
First, set up a few environment variables:

```
# Arbitrary name for your VM application
VM_APP="rabbitmq"
# Namespace to contain WorkloadGroups, WorkloadEntries, and Services for the VM
VM_NAMESPACE="vm"
# Temporary directory to output VM configuration to
WORK_DIR="config"
# Arbitrary name for the service account for your VM application
VM_SERVICE_ACCOUNT="rabbitmq-sa"
# Must match VM_NETWORK but an empty string is fine with a single network
CLUSTER_NETWORK=""
# Must match CLUSTER_NETWORK but an empty string is fine with a single network
VM_NETWORK=""
# By default the cluster name is Kubernetes
CLUSTER="Kubernetes" 
```

Create the namespace and service account for the VM.  This namespace will contain `WorkloadGroup`, `WorkloadEntry`, and `Service` objects for external VMs.

```
kubectl create namespace "$VM_NAMESPACE"
kubectl create serviceaccount "$VM_SERVICE_ACCOUNT" -n "$VM_NAMESPACE"
```

### WorkloadGroup creation
Create the WorkloadGroup for your external VM.  A WorkloadGroup is a template for WorkloadEntry objects.  A WorkloadGroup is analogous to a Deployment for VMs, and a WorkloadEntry is like a Pod for VMs, created based on the WorkloadGroup.  You can create a WorkloadGroup for each of your VM definitions, and Aspen Mesh will automatically create WorkloadEntry objects for those VMs.

```
cat << EOF > workloadgroup.yaml
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: "$VM_APP"
  namespace: "$VM_NAMESPACE"
spec:
  metadata:
    labels:
      app: "$VM_APP"
  template:
    serviceAccount: "$VM_SERVICE_ACCOUNT"
    network: "$VM_NETWORK"
EOF
```

And apply the WorkloadGroup definition:

```
kubectl apply -n $VM_NAMESPACE -f workloadgroup.yaml
```

### Get the east-west gateway IP
Next we have to get the `istio-eastwestgateway` service's external IP address.  By default, gateways in AWS are Load Balancers that only use a CNAME record for the service's EXTERNAL-IP.  We have to get the external IP addresses for a host file entry for the vm.

Get the IP address for the east-west gateway load balancer:

```
kubectl get svc -n istio-system istio-eastwestgateway -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" | nslookup
```

Then, set an environment variable with the external IP (use the first one if there are multiple):

```
INGRESS_IP=<first-ip-from-nslookup>
```

### VM setup

Create the `WORK_DIR` directory for the VM files:

```
mkdir -p $WORK_DIR
```

Use the `istioctl x workload entry` command to generate files to be used for the VM configuration.  Make sure to specify the `ingressIP` argument; otherwise the host file entry for `istiod` won't be added to the VM.

```
istioctl x workload entry configure -f workloadgroup.yaml -o "$WORK_DIR" --clusterID "$CLUSTER" --ingressIP "$INGRESS_IP" --autoregister
```

You should see the following files in your Work directory (`config` if you used the values from earlier):

 * cluster.env	
 * hosts		
 * istio-token	
 * mesh.yaml	
 * root-cert.pem

Transfer these files to the home directory of your VM default user.  You can use SFTP, SCP, or any other method that you want:

```
sftp -i <aws-keypair> ubuntu@<public-instance-hostname>

sftp> put config/*  
```

Establish an SSH session with your VM, and install the `istio-sidecar` package:

```
# Change to your istio version
export ISTIO_VERSION=1.11.3

curl -LO https://storage.googleapis.com/istio-release/releases/$ISTIO_VERSION/deb/istio-sidecar.deb
sudo dpkg -i istio-sidecar.deb

# There is an RPM for Istio for RPM-based hosts:
# curl -LO https://storage.googleapis.com/istio-release/releases/$ISTIO_VERSION/rpm/istio-sidecar.rpm
# rpm -i istio-sidecar.rpm
```

Now, move the files that you copied to the proper locations on your VM:

```
sudo mkdir -p /etc/certs
sudo cp "$HOME"/root-cert.pem /etc/certs/root-cert.pem

sudo  mkdir -p /var/run/secrets/tokens
sudo cp "$HOME"/istio-token /var/run/secrets/tokens/istio-token

sudo cp "$HOME"/cluster.env /var/lib/istio/envoy/cluster.env

sudo cp "$HOME"/mesh.yaml /etc/istio/config/mesh

sudo sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

sudo mkdir -p /etc/istio/proxy
sudo chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem
```

Start the sidecar on your VM:

```
sudo systemctl start istio
```

And tail the log file `/var/log/istio/istio.log` to make sure everything starts correctly.

## Application Installation

There is a separate folder called [`ecommerce-microservices`](https://github.com/brianjimerson/ecommerce-microservices) that contains a demonstration of microservices on Aspen Mesh.  Two of the microservices, `payment-service` and `payment-history` communicate to the RabbitMQ server VM.

First, install all of the services.  For more details on configuration see the [`README`](https://github.com/brianjimerson/ecommerce-microservices/README.md) in the `ecommerce-microservices` repository.  At a minimum, you'll want to change the `image.build.name` property in the the `pom.xml`'s `docker-maven-plugin` to point to your Docker Registry.

```
cd ecommerce-microservices/deploy
./install-all.sh 
./deploy-all.sh
```

You also need to patch the `payment-history` and `payment-service` deployments to override the default RabbitMQ connection settings.  Edit `patch-payment-service.yaml`and `patch-payment-history.yaml `, update the username, password, and port settings, and then apply the patches:

```
kubectl patch deployment payment-service -n ecommerce --patch "$(cat payment-service-patch.yaml)"
kubectl patch deployment payment-history -n ecommerce --patch "$(cat payment-history-patch.yaml)"
```

Finally, create a service for RabbitMQ in the `vm` namespace:

```
kubectl apply -f rabbitmq-vm-svc.yaml
```

If you look at the logs for `payment-history`, you should see a connection to the RabbitMQ VM:

```
Attempting to connect to: [rabbitmq.vm.svc:5672]
...

Created new connection: rabbitConnectionFactory#59546cfe:49/SimpleConnection@1f6772cc [delegate=amqp://admin@100.68.169.51:5672/, localPort= 52062]
```

## Load Balancing Across VMs

You can create multiple VMs / WorkloadEntries for the same WorkloadGroup and Aspen Mesh will load balance across the VMs; the setup is very similar.

### VM creation

Create 2 EC2 instances in the same network as your Kubernetes cluster (Ubuntu 20.04 Server was used for this demonstration).  Make sure public access is enabled, there is enough CPU, RAM and storage (this demonstration used t2.large instances with 20GB of storage).  Add port 80 (HTTP) to your security group for the instances.

For each instance, establish an SSH session, and install apache:

```
sudo apt update
sudo apt install apache2 -y
```

Then, create an html file that displays the server's name. _Make sure you change Web server 1 to Web server 2 on the 2nd web server_.

```
sudo sh -c  'echo Web server 1 > /var/www/html/info.html'
```

Test that the page is served correctly:

```
curl http://localhost/info.html
Web server 1
```
### Environment variables and namespace
First, exit your SSH session(s), and set up a few environment variables:

```
# Arbitrary name for your VM application
VM_APP="web"
# Namespace to contain WorkloadGroups, WorkloadEntries, and Services for the VM
VM_NAMESPACE="vm"
# Temporary directory to output VM configuration to
WORK_DIR="config"
# Arbitrary name for the service account for your VM application
VM_SERVICE_ACCOUNT="web-sa"
# Must match VM_NETWORK but an empty string is fine with a single network
CLUSTER_NETWORK=""
# Must match CLUSTER_NETWORK but an empty string is fine with a single network
VM_NETWORK=""
# By default the cluster name is Kubernetes
CLUSTER="Kubernetes" 
```

Create the namespace and service account for the VM.  This namespace will contain `WorkloadGroup`, `WorkloadEntry`, and `Service` objects for external VMs.

```
kubectl create namespace "$VM_NAMESPACE"
kubectl create serviceaccount "$VM_SERVICE_ACCOUNT" -n "$VM_NAMESPACE"
```

### WorkloadGroup creation
Create the WorkloadGroup for your external VM.  A WorkloadGroup is a template for WorkloadEntry objects.  A WorkloadGroup is analogous to a Deployment for VMs, and a WorkloadEntry is like a Pod for VMs, created based on the WorkloadGroup.  You can create a WorkloadGroup for each of your VM definitions, and Aspen Mesh will automatically create WorkloadEntry objects for those VMs.

```
cat << EOF > workloadgroup.yaml
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: "$VM_APP"
  namespace: "$VM_NAMESPACE"
spec:
  metadata:
    labels:
      app: "$VM_APP"
  template:
    serviceAccount: "$VM_SERVICE_ACCOUNT"
    network: "$VM_NETWORK"
EOF
```

And apply the WorkloadGroup definition:

```
kubectl apply -n $VM_NAMESPACE -f workloadgroup.yaml
```

### Get the east-west gateway IP
Next we have to get the `istio-eastwestgateway` service's external IP address.  By default, gateways in AWS are Load Balancers that only use a CNAME record for the service's EXTERNAL-IP.  We have to get the external IP addresses for a host file entry for the vm.

Get the IP address for the east-west gateway load balancer:

```
kubectl get svc -n istio-system istio-eastwestgateway -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" | nslookup
```

Then, set an environment variable with the external IP (use the first one if there are multiple):

```
INGRESS_IP=<first-ip-from-nslookup>
```

### VM setup

Create the `WORK_DIR` directory for the VM files:

```
mkdir -p $WORK_DIR
```

Use the `istioctl x workload entry` command to generate files to be used for the VM configuration.  Make sure to specify the `ingressIP` argument; otherwise the host file entry for `istiod` won't be added to the VM.

```
istioctl x workload entry configure -f workloadgroup.yaml -o "$WORK_DIR" --clusterID "$CLUSTER" --ingressIP "$INGRESS_IP" --autoregister
```

You should see the following files in your Work directory (`config` if you used the values from earlier):

 * cluster.env	
 * hosts		
 * istio-token	
 * mesh.yaml	
 * root-cert.pem

_For each VM, follow the steps until [Testing](#Testing):_

Transfer these files to the home directory of your VM default user.  You can use SFTP, SCP, or any other method that you want:

```
sftp -i <aws-keypair> ubuntu@<public-instance-hostname>

sftp> put config/*  
```

Establish an SSH session with your VM, and install the `istio-sidecar` package.

```
# Change to your istio version
export ISTIO_VERSION=1.11.3

curl -LO https://storage.googleapis.com/istio-release/releases/$ISTIO_VERSION/deb/istio-sidecar.deb
sudo dpkg -i istio-sidecar.deb

# There is an RPM for Istio for RPM-based hosts:
# curl -LO https://storage.googleapis.com/istio-release/releases/$ISTIO_VERSION/rpm/istio-sidecar.rpm
# rpm -i istio-sidecar.rpm
```

Now, move the files that you copied to the proper locations on your VM:

```
sudo mkdir -p /etc/certs
sudo cp "$HOME"/root-cert.pem /etc/certs/root-cert.pem

sudo  mkdir -p /var/run/secrets/tokens
sudo cp "$HOME"/istio-token /var/run/secrets/tokens/istio-token

sudo cp "$HOME"/cluster.env /var/lib/istio/envoy/cluster.env

sudo cp "$HOME"/mesh.yaml /etc/istio/config/mesh

sudo sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

sudo mkdir -p /etc/istio/proxy
sudo chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem
```

Start the sidecar on your VM:

```
sudo systemctl start istio
```

And tail the log file `/var/log/istio/istio.log` to make sure everything starts correctly.

### Testing

Verify the WorkloadEntries were created:

```
kubectl get workloadentry -n vm

NAME                 AGE   ADDRESS
web-192.168.68.247   38m   192.168.68.247
web-192.168.79.194   43m   192.168.79.194
```

The last step is to create a service for your WorkloadEntries:

```
kubectl apply -f web-vm-svc.yaml
```

If you execute a `curl` command from a pod that has `istio` injected, you should see the response come from random instances of your web servers:

```
FLD-ML-00061956:aspenmesh-with-vm jimerson$ kubectl exec -it -n command-runner -c command-runner command-runner-5f9fcc546-nzptf -- curl http://web.vm.svc/info.html
Web server 2
FLD-ML-00061956:aspenmesh-with-vm jimerson$ kubectl exec -it -n command-runner -c command-runner command-runner-5f9fcc546-nzptf -- curl http://web.vm.svc/info.html
Web server 2
FLD-ML-00061956:aspenmesh-with-vm jimerson$ kubectl exec -it -n command-runner -c command-runner command-runner-5f9fcc546-nzptf -- curl http://web.vm.svc/info.html
Web server 1
FLD-ML-00061956:aspenmesh-with-vm jimerson$ kubectl exec -it -n command-runner -c command-runner command-runner-5f9fcc546-nzptf -- curl http://web.vm.svc/info.html
Web server 2
FLD-ML-00061956:aspenmesh-with-vm jimerson$ kubectl exec -it -n command-runner -c command-runner command-runner-5f9fcc546-nzptf -- curl http://web.vm.svc/info.html
Web server 1
```


