resource "google_compute_address" "k8s-worker-nodes-internal-ip" {
  count        = var.n-worker-nodes
  subnetwork   = var.subnetwork
  name         = "k8s-worker-${count.index}-internal-ip"
  region       = var.region
  address_type = "INTERNAL"
}
resource "google_compute_instance" "k8s-worker-instances" {
  count        = var.n-worker-nodes
  name         = "${var.name}-${count.index}"
  machine_type = var.machine_type
  zone         = var.zone
  tags         = var.target_tags
  boot_disk {
    initialize_params {
      image = var.image
    }
  }
  network_interface {
    network    = var.network
    subnetwork = var.subnetwork
    network_ip = google_compute_address.k8s-worker-nodes-internal-ip[count.index].address
    access_config {}
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

resource "google_compute_instance_group" "worker-nodes-instance-group" {
  name      = "k8s-worker-nodes-group"
  zone = var.zone
  named_port {
    name = "ingress-http-port"
    port = var.ports[0]
  }
  named_port {
    name = "ingress-https-port"
    port = var.ports[1]
  }
  instances = google_compute_instance.k8s-worker-instances[*].self_link
}
module "gce-lb-http" {
  source  = "GoogleCloudPlatform/lb-http/google"
  version = "~> 9.0"
  project =  var.project_id
  name        = "${var.name}-elb"
  target_tags = var.target_tags
  backends = {
    default = {
      port        = var.ports[0]
      protocol    = "HTTP"
      port_name   = "ingress-http-port"
      timeout_sec = 10
      enable_cdn  = false
      health_check = {
        request_path = "/"
        port         = var.ports[0]
      }

      log_config = {
        enable      = true
        sample_rate = 1.0
      }

      groups = [
        {
          group = google_compute_instance_group.worker-nodes-instance-group.self_link
        }
      ]

      iap_config = {
        enable = false
      }
    }
  }
}
