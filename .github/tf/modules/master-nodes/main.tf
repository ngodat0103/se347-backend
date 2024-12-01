resource "google_compute_address" "k8s-master-nodes-internal-ip" {
  count        = var.n-master-nodes
  name         = "${var.name}-${count.index}-internal-ip"
  region       = var.region
  address_type = "INTERNAL"
  subnetwork   = var.subnetwork
}
resource "google_compute_address" "k8s-master-nodes-public-ip" {
  count        = var.n-master-nodes
  name         = "${var.name}-${count.index}-public-ip"
  region       = var.region
  address_type = "EXTERNAL"
}


resource "google_compute_instance" "k8s-master-instances" {
  count        = var.n-master-nodes
  name         = "${var.name}-${count.index}"
  machine_type = var.machine_type
  zone         = var.zone
  tags         = ["${var.name}"]
  boot_disk {
    initialize_params {
      image = var.image
    }
  }

  network_interface {
    network    = var.network
    subnetwork = var.subnetwork
    network_ip = google_compute_address.k8s-master-nodes-internal-ip[count.index].address
    access_config {
      nat_ip = google_compute_address.k8s-master-nodes-public-ip[count.index].address
    }
  }

  metadata = {
    ssh-keys = "k8s-admin:${file("${var.pub_key_path}")}"
  }

  metadata_startup_script = <<-EOT
    #!/bin/bash
    useradd -m -s /bin/bash k8s-admin
    echo "k8s-admin:k8s-admin" | chpasswd
    usermod -aG sudo k8s-admin
  EOT
}
resource "google_compute_address" "master-nodes-internal-lb-ip" {
  name         = "k8s-master-internal-lb-ip"
  region       = var.region
  address_type = "INTERNAL"
  subnetwork   = var.subnetwork
}

resource "google_compute_instance_group" "master-nodes-instance-group" {
  name = "k8s-master-nodes-group"
  instances = google_compute_instance.k8s-master-instances[*].self_link
  zone = var.zone
  named_port {
    name = "k8s-api-server-port"
    port = var.ports[0]
  }
}


module "network_ilb" {
  source  = "GoogleCloudPlatform/lb-internal/google"
  version = "~> 7.0"
  ip_address =  google_compute_address.master-nodes-internal-lb-ip.address
  project      = var.project_id
  network      = var.network
  subnetwork   = var.subnetwork
  region       = var.region
  name         =  "${var.name}-ilb"
  ports        = var.ports 
  source_tags  = var.cluster-tag
  target_tags  = var.master-nodes-tag
  backends     = [
    {
      group = google_compute_instance_group.master-nodes-instance-group.self_link
      balancing_mode = "CONNECTION"
      max_rate = 100
      max_rate_per_instance = 50
      enable_logging = true
      
    }
  ]
  health_check = var.health_check
}
