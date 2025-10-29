# GCP Hands-On Labs & Study Guide (Combined)

This document contains six detailed sections covering: **Compute Engine**, **Dataflow (Java)**, **Google Kubernetes Engine (GKE)**, **Cloud Run**, **Cloud Functions (Java & Node.js)**, and **App Engine**.
Each section includes: overview, architecture (ASCII), detailed key concepts, command snippets with explanations, sample configuration (where applicable), real-world example, best practices, and references.

---

# 1. Compute Engine (GCE)

## Overview
Google Compute Engine (GCE) offers virtual machines (VMs) on Google Cloud's global infrastructure. It provides fine-grained control of OS, networking, disks, GPUs, and instance lifecycle. Typical uses: legacy app lift-and-shift, custom OS tuning, large single-node workloads, batch jobs, or as nodes backing other services.

## Architecture (ASCII)
```
         +------------------------------------------+
         |            Google Compute Engine         |
         +------------------------------------------+
                      |
        +---------------------------+
        |  Managed Instance Group   |
        +------------+--------------+
                     |
       +-------------+-------------+
       | VM Instance | VM Instance |
       +-------------+-------------+
              |            |
        +-----------+ +-----------+
        | Boot Disk | | Boot Disk |
        +-----------+ +-----------+
                     |
              +--------------+
              | Load Balancer |
              +--------------+
```

## Key Concepts
- **Instance**: VM with chosen machine type and image.
- **Machine Types**: e2, n2, custom sizing; affects cost/perf.
- **Persistent Disks**: zonal or regional SSD/HDD.
- **Preemptible VMs**: low-cost, ephemeral; for fault-tolerant workloads.
- **Snapshots / Images**: for backups and reproducible images.
- **Instance Templates & MIGs**: templates for autoscaling and consistent creation.
- **Metadata & Startup Scripts**: pass configuration or run boot-time installs.
- **Service Accounts & IAM**: grant least-privilege to VMs.

## Common Commands (with explanations)
```bash
# Create a VM with Debian 12
gcloud compute instances create my-vm   --zone=us-central1-a   --machine-type=e2-medium   --image-family=debian-12   --image-project=debian-cloud
```
Creates a standard VM. Use `--preemptible` for cheaper spot-like instances.

```bash
# SSH into a VM
gcloud compute ssh my-vm --zone=us-central1-a
```
Opens a secure shell using generated SSH keys in your account.

```bash
# List all instances
gcloud compute instances list
```
Shows all VMs in project with IPs and status.

```bash
# Create snapshot of disk
gcloud compute disks snapshot my-disk --snapshot-names=my-snap --zone=us-central1-a
```
Snapshot disk for backup/restore or create an image.

```bash
# Create a managed instance group with template
gcloud compute instance-templates create my-template --machine-type=e2-medium --image-family=debian-12 --image-project=debian-cloud

gcloud compute instance-groups managed create my-mig --base-instance-name webserver --size=2 --template=my-template --zone=us-central1-a
```
Used for autoscaling setups and rolling updates.

## Sample Startup Script
```bash
#! /bin/bash
apt update
apt install -y openjdk-17-jdk nginx
systemctl enable nginx
systemctl start nginx
```
Add via metadata `--metadata-from-file startup-script=startup.sh` to automate provisioning.

## Real-World Example
A data analytics firm uses MIGs with preemptible VMs for nightly batch processing, storing intermediate data on regional persistent disks and final outputs in Cloud Storage and BigQuery.

## Best Practices
- Use managed instance groups for availability and autoscaling.
- Use service accounts with minimal permissions.
- Prefer regional persistent disks for redundancy when needed.
- Apply firewall rules and use private IPs + Cloud NAT for outbound-only VMs.
- Schedule snapshots and use labels for cost tracking.

## References
- https://cloud.google.com/compute/docs
- https://cloud.google.com/compute/docs/instances

---

# 2. Cloud Dataflow (Apache Beam, Java)

## Overview
Cloud Dataflow is a fully managed service for executing Apache Beam pipelines. Write pipelines in Java (or Python) using Beam SDK and run them on Dataflow Runner. Supports batch and streaming, windowing, triggers, side inputs/outputs, and connectors to Pub/Sub, BigQuery, and Cloud Storage.

## Architecture (ASCII)
```
+--------------------+       +--------------------+
|   Pub/Sub Topic    |  -->  |  Dataflow Pipeline |
+--------------------+       | (Beam Java SDK)    |
                             +----+----+----+-----+
                                  |    |    |
                       +----------+    |    +-----------+
                       |               |                |
                 +-----v----+     +----v----+       +---v----+
                 | ParDo    |     | Window  |       | Side   |
                 | Transform|     | + Trigger|      |Outputs |
                 +----------+     +---------+       +--------+
                       |               |                |
                       +---------------+----------------+
                                       |
                               +-------v--------+
                               | BigQuery / GCS |
                               +----------------+
```

## Key Concepts
- **PCollection**: distributed dataset.
- **ParDo / DoFn**: element-wise processing with lifecycle methods.
- **GroupByKey/Combine**: grouping & aggregation.
- **Windowing & Triggers**: handle late data and grouping in streams.
- **Side Inputs / Side Outputs**: enrich processing with additional data or route errors.
- **State & Timers** (advanced): maintain per-key state in streaming.

## Commands & Workflow
```bash
# Enable APIs
gcloud services enable dataflow.googleapis.com pubsub.googleapis.com bigquery.googleapis.com

# Build JAR
mvn clean package -DskipTests

# Run locally
mvn compile exec:java -Dexec.mainClass=com.example.MyPipeline -Dexec.args="--runner=DirectRunner"

# Run on Dataflow
mvn compile exec:java -Dexec.mainClass=com.example.MyPipeline -Dexec.args="--runner=DataflowRunner --project=my-project --region=us-central1 --stagingLocation=gs://my-bucket/staging --tempLocation=gs://my-bucket/temp --inputTopic=projects/my-project/topics/input --outputTable=mydataset.out"
```
Use templating (Flex Templates) for repeatable deployments and CI/CD integration.

## Example (simplified Java snippet)
```java
PCollection<String> lines = p.apply("Read", PubsubIO.readStrings().fromTopic(inputTopic));
PCollection<MyRecord> parsed = lines.apply("Parse", ParDo.of(new ParseFn()));
PCollection<MyRecord> filtered = parsed.apply(Filter.by(r -> r.isValid()));
filtered.apply("ToBQ", BigQueryIO.writeTableRows()...);
```

## Real-World Example
Real-time clickstream aggregation: events published to Pub/Sub, Dataflow aggregates per-minute metrics and writes to BigQuery for dashboards and alerting.

## Best Practices
- Use windowing that matches business semantics; set allowed lateness.
- Emit side outputs for bad data into dead-letter topics or storage.
- Monitor job metrics; set labels for cost tracking.
- Use regional resources to avoid cross-region egress costs.

## References
- https://beam.apache.org/documentation/
- https://cloud.google.com/dataflow/docs

---

# 3. Google Kubernetes Engine (GKE)

## Overview
GKE provides managed Kubernetes control plane and node management. It’s suited for containerized microservices with complex orchestration needs, service meshes, and large-scale deployments.

## Architecture (ASCII)
```
+--------------------------------------------+
|                GKE Cluster                 |
+--------------------------------------------+
| Node Pool A | Node Pool B | Node Pool C    |
|  (VMs)      |  (VMs)      |  (VMs)         |
+----+----+---+----+----+---+----+----+-------+
     |                 |                 |
  +--v--+           +--v--+           +--v--+
  | Pod |           | Pod |           | Pod |
  +-----+           +-----+           +-----+
     |                 |                 |
  +--v--+           +--v--+           +--v--+
  |Service|         |Service|         |Ingress|
  +------+           +------+           +-----+
```

## Key Concepts
- **Pods, Deployments, ReplicaSets**: core workload units.
- **Services (ClusterIP, NodePort, LoadBalancer)**: networking abstraction.
- **Ingress / Ingress Controllers**: L7 routing into cluster.
- **ConfigMaps & Secrets**: config management.
- **Network Policies**: Pod-to-pod network controls.
- **Workload Identity**: map Kubernetes service accounts to GCP service accounts for secure access.

## Commands (with examples)
```bash
# Create a GKE cluster
gcloud container clusters create my-cluster --zone=us-central1-a --num-nodes=3

# Get credentials for kubectl
gcloud container clusters get-credentials my-cluster --zone=us-central1-a

# Deploy using kubectl
kubectl apply -f deployment.yaml

# Expose service
kubectl expose deployment my-app --type=LoadBalancer --port=80
```

## Sample deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: my-app
        image: gcr.io/my-project/my-app:latest
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: my-app-service
spec:
  type: LoadBalancer
  selector:
    app: my-app
  ports:
  - port: 80
    targetPort: 8080
```

## Real-World Example
A SaaS company runs backend microservices on GKE, using HorizontalPodAutoscaler for CPU-based scaling, Ingress for routing, and persistent volumes for stateful components like Redis or NFS-backed storage.

## Best Practices
- Use Workload Identity; do not mount long-lived keys into pods.
- Define readiness/liveness probes to enable graceful rollouts.
- Set resource requests/limits to enable proper bin-packing and avoid OOMs.
- Use private clusters and node auto-repair for production security and reliability.

## References
- https://cloud.google.com/kubernetes-engine/docs
- https://kubernetes.io/docs/home/

---

# 4. Cloud Run

## Overview
Cloud Run runs stateless containers in a serverless fashion. It supports HTTP and event-driven workloads, auto-scales to zero, and bills per request/CPU/GB-second.

## Architecture (ASCII)
```
+------------------------+
|     Cloud Run Service  |
+------------------------+
|  Revision 1 / Revision2|
|  Container Instances   |
+-----------+------------+
            |
      +-----v-----+
      |  HTTP LB  |
      +-----------+
            |
         Clients
```

## Key Concepts
- **Service & Revision**: immutable revisions created on deploy.
- **Concurrency**: requests per instance.
- **Min/Max instances**: control cold starts and cost.
- **VPC Connector**: access private resources like Cloud SQL.

## Commands
```bash
# Build & push image
gcloud builds submit --tag gcr.io/my-project/my-app

# Deploy to Cloud Run
gcloud run deploy my-service --image gcr.io/my-project/my-app --region=us-central1 --platform=managed --allow-unauthenticated

# Set min instances to avoid cold starts
gcloud run services update my-service --min-instances=1
```

## Real-World Example
An API service packaged as a Spring Boot fat-jar inside a container runs on Cloud Run and scales to meet sudden spikes for event-driven workloads.

## Best Practices
- Tune concurrency for latency vs cost trade-offs.
- Use health checks and readiness to ensure graceful traffic shifts.
- Use Secrets + IAM for database credentials.

## References
- https://cloud.google.com/run/docs

---

# 5. Cloud Functions (Java & Node.js)

## Overview
Cloud Functions provide event-driven serverless execution for single-purpose functions. Ideal for glue code, event processing, and small HTTP endpoints.

## Architecture (ASCII)
```
Trigger (HTTP / PubSub / GCS) -> Cloud Function (runtime: Java/Node) -> Downstream (BigQuery, GCS, PubSub)
```

## Key Concepts
- **Triggers**: event source to invoke function.
- **Entry Point**: handler or method name for runtime.
- **Execution environment**: ephemeral container per invocation.
- **Cold starts**: first invocation overhead for new instances.

## Commands
```bash
# Deploy Java function (Pub/Sub)
gcloud functions deploy processOrders --runtime=java17 --trigger-topic=orders-topic --entry-point=com.example.ProcessOrders --region=us-central1

# Deploy Node.js function (HTTP)
gcloud functions deploy hello --runtime=nodejs20 --trigger-http --allow-unauthenticated --entry-point=helloHttp --region=us-central1
```

## Sample Node.js function (helloHttp)
```javascript
exports.helloHttp = (req, res) => {
  res.send("Hello from Cloud Function!");
};
```

## Real-World Example
Image processing pipeline: upload to GCS triggers a Node.js function to generate thumbnails and store metadata in Firestore; a Java function consumes Pub/Sub events for heavy processing.

## Best Practices
- Keep functions small and focused.
- Avoid large cold-start libraries; use lightweight dependencies.
- Use retries and dead-lettering for Pub/Sub triggers.
- Secure functions via IAM or allow-unauthenticated appropriately.

## References
- https://cloud.google.com/functions/docs

---

# 6. App Engine

## Overview
App Engine (standard & flexible) is a PaaS that runs apps without managing servers. Standard env is fast and sandboxed; flexible env runs Docker containers for custom runtimes.

## Architecture (ASCII)
```
Users -> App Engine (Service + Versions) -> Datastore / Cloud SQL / Cloud Storage
```

## Key Concepts
- **app.yaml**: config for runtime and scaling.
- **Versioning**: each deploy creates a new version; traffic can be split.
- **Scaling types**: automatic, basic, manual.
- **Standard vs Flexible**: prebuilt runtimes vs custom Docker.

## Commands
```bash
gcloud app create --region=us-central
gcloud app deploy app.yaml
gcloud app browse
gcloud app services list
```

## app.yaml (example)
```yaml
runtime: java17
instance_class: F2
automatic_scaling:
  min_instances: 1
  max_instances: 5
env_variables:
  SPRING_PROFILES_ACTIVE: prod
```

## Real-World Example
A SaaS product deploys its API and web front-end on App Engine standard, leveraging built-in memcache and auto-scaling for peak usage with minimal ops overhead.

## Best Practices
- Use separate services or modules for microservices.
- Use traffic splitting to roll out features gradually.
- Monitor costs and tune instance classes accordingly.

## References
- https://cloud.google.com/appengine/docs

---

# Appendix: Quick Command Reference

- `gcloud compute instances create ...` — create VM
- `gcloud container clusters create ...` — create GKE cluster
- `gcloud run deploy ...` — deploy Cloud Run
- `gcloud functions deploy ...` — deploy Cloud Function
- `gcloud app deploy` — deploy App Engine

---

End of document.
