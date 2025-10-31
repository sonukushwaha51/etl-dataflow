
# ☁️ Google Cloud Platform - Core Compute & Application Services

This document provides architecture overviews, ASCII diagrams, commands, and real-world examples for six major Google Cloud services: **Compute Engine**, **Dataflow**, **Google Kubernetes Engine (GKE)**, **Cloud Run**, **Cloud Functions**, and **App Engine**.

---

## 🖥️ Compute Engine

### 📘 Overview
Google Compute Engine (GCE) provides virtual machines (VMs) that run on Google’s global infrastructure. You have full control over operating systems, networking, and storage — ideal for custom workloads, legacy app migration, or ML model hosting.

### 🧱 Architecture (ASCII Diagram)
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
              | LoadBalancer |
              +--------------+
```

### 🧩 Common Commands
```bash
# Create a VM
gcloud compute instances create my-vm   --zone=us-central1-a   --machine-type=e2-medium   --image-family=debian-12   --image-project=debian-cloud

# SSH into VM
gcloud compute ssh my-vm --zone=us-central1-a

# Create a firewall rule
gcloud compute firewall-rules create allow-http   --allow tcp:80 --target-tags http-server
```

### 🌍 Real-World Example
A healthcare analytics firm runs ML training jobs on Compute Engine VMs using TensorFlow, with autoscaling via Managed Instance Groups.

---

## 💧 Cloud Dataflow

### 📘 Overview
Cloud Dataflow is a fully managed service for stream and batch data processing based on Apache Beam.

### 🧱 Architecture (ASCII Diagram)
```
          +--------------------------+
          |   Cloud Pub/Sub Topic    |
          +------------+-------------+
                       |
                       v
          +--------------------------+
          |   Dataflow Pipeline      |
          +--------------------------+
          |  ReadFromPubSub          |
          |  -> Transform (DoFn)     |
          |  -> Filter -> Enrich     |
          |  -> WriteToBigQuery      |
          +------------+-------------+
                       |
                       v
              +-------------------+
              |   BigQuery Table  |
              +-------------------+
```

### 🧩 Example Commands
```bash
# Run locally
mvn compile exec:java -Dexec.mainClass=com.example.MyPipeline   -Dexec.args="--runner=DirectRunner"

# Submit to Dataflow
mvn compile exec:java -Dexec.mainClass=com.example.MyPipeline   -Dexec.args="--runner=DataflowRunner                --project=my-project                --region=us-central1                --stagingLocation=gs://my-bucket/staging                --tempLocation=gs://my-bucket/temp"
```

### 🌍 Real-World Example
A retail company streams sales data via Pub/Sub, processes it in Dataflow, and stores real-time analytics in BigQuery.

---

## 🧩 Google Kubernetes Engine (GKE)

### 📘 Overview
Google Kubernetes Engine (GKE) is a managed Kubernetes service that automates cluster setup, scaling, and upgrades.

### 🧱 Architecture (ASCII Diagram)
```
        +--------------------------------------------+
        |         Google Kubernetes Engine           |
        +--------------------------------------------+
                        |
           +-------------------------------+
           |        Kubernetes Cluster      |
           +---------------+----------------+
                           |
     +---------+-----------+-----------+---------+
     |  Pod A  |   Pod B   |   Pod C   |  Pod D  |
     +----+----+----+------+----+------+----+----+
          |         |            |          |
     +----+----+ +--+---+   +----+----+ +---+---+
     | Service | | Service | | Service | |Ingress|
     +---------+ +---------+ +---------+ +-------+
```

### 🧩 Commands
```bash
# Create cluster
gcloud container clusters create my-cluster --num-nodes=3

# Deploy app
kubectl apply -f deployment.yaml

# Expose service
kubectl expose deployment my-app --type=LoadBalancer --port=80
```

### 🌍 Real-World Example
A logistics platform uses GKE to orchestrate microservices — API, auth, analytics — with seamless scaling and rolling updates.

---

## 🚀 Cloud Run

### 📘 Overview
Cloud Run runs containerized applications in a fully managed, serverless environment.

### 🧱 Architecture (ASCII Diagram)
```
         +--------------------------------------+
         |             Cloud Run                |
         +--------------------------------------+
                         |
             +-----------+------------+
             |  Containerized App     |
             +-----------+------------+
                         |
              +----------+----------+
              |   HTTPS Endpoint    |
              +----------+----------+
                         |
                 +-------+-------+
                 | Cloud LoadBalancer |
                 +-------------------+
```

### 🧩 Commands
```bash
# Build and deploy
gcloud run deploy my-service   --image=gcr.io/my-project/my-image   --platform=managed   --region=us-central1
```

### 🌍 Real-World Example
A fintech company deploys a payment webhook as a container to Cloud Run, scaling instantly with demand.

---

## ⚡ Cloud Functions

### 📘 Overview
Cloud Functions execute lightweight, event-driven code without managing servers.

### 🧱 Architecture (ASCII Diagram)
```
      +---------------------------+
      |      Cloud Pub/Sub        |
      +-------------+-------------+
                    |
                    v
         +--------------------------+
         |     Cloud Function        |
         +-------------+-------------+
                       |
             +---------+---------+
             | Cloud Storage/DB  |
             +-------------------+
```

### 🧩 Commands
```bash
# Deploy function
gcloud functions deploy processData   --runtime=python311   --trigger-topic=my-topic   --region=us-central1
```

### 🌍 Real-World Example
An IoT pipeline triggers Cloud Functions from Pub/Sub messages to preprocess sensor data before storage.

---

## 🌐 App Engine

### 📘 Overview
App Engine (GAE) is a fully managed PaaS for building scalable web applications and APIs.

### 🧱 Architecture (ASCII Diagram)
```
             +--------------------------------------------+
             |            Google App Engine               |
             +--------------------------------------------+
                      |                     |
              +-------+-------+       +-----+-----+
              |  App Service  |       |  App Service  |
              | (Frontend)    |       | (Backend)    |
              +---------------+       +---------------+
                        |                     |
          +---------------------------------------------+
          |            Google Load Balancer             |
          +---------------------------------------------+
                        |                     |
             +----------+----------+   +------+---------+
             |   Auto-Scaled VMs   |   |   Datastore    |
             |   Managed by GAE    |   |   or Firestore |
             +---------------------+   +----------------+
```

### 🧩 Commands
```bash
# Create App Engine app
gcloud app create --region=us-central

# Deploy app
gcloud app deploy

# Split traffic between versions
gcloud app services set-traffic my-service --splits v2=0.5,v1=0.5
```

### 🌍 Real-World Example
A startup deploys a Python Flask API to App Engine, automatically scaling with user requests.

---

✅ **End of Document**
