provider "google" {
  credentials = file("~/.config/gcloud/application_default_credentials.json")
  project     = var.project_id
  region      = "asia-southeast1"
  zone        = "asia-southeast1-a"
}

module "network" {
  source       = "terraform-google-modules/network/google"
  version      = "9.3.0"
  network_name = var.network_name
  project_id   = var.project_id
  subnets = [
    {
      subnet_name   = var.master-nodes-subnet
      subnet_ip     = var.master-nodes-subnet-ip
      subnet_region = var.master-nodes-region
      description   = "Subnet for k8s-network"
    },
    {
      subnet_name   = var.worker-nodes-subnet
      subnet_ip     = var.worker-nodes-subnet-ip
      subnet_region = var.worker-nodes-region
      description   = "Subnet for worker nodes"
    }
  ]
  firewall_rules = [
    {
      name      = "allow-ping-ingress"
      direction = "INGRESS"
      allow = [
        {
          protocol = "icmp"
        }
      ]
    },
    {
      name = "allow-ssh-ingress"
      # network       = "k8s-network"
      direction = "INGRESS"
      allow = [
        {
          protocol = "tcp"
          ports    = ["22"]
        }
      ]
      source_ranges = ["0.0.0.0/0"]
    }
    # {
    #   name      = "allow-port-for-worker-nodes"
    #   direction = "INGRESS"
    #   allow = [
    #     {
    #       protocol = "tcp"
    #       ports    = ["30080", "30443"]
    #     }
    #   ]
    #   source_ranges = ["0.0.0.0/0"]
    #   target_tags   = var.worker-nodes-tags
    # },
    # {
    #   name          = "allow-api-server-endpoint-for-master-nodes"
    #   source_ranges = var.cluster-tag
    #   target_tags =  var.master-nodes-tags
    #   priority =  100
    #   enable_log = true
    #   allow = [
    #     {
    #       protocol = "tcp"
    #       ports    = ["6443"]
    #     }
    #   ]
    # }
  ]
}
module "master-nodes" {
  depends_on     = [module.network]
  source         = "../modules/master-nodes"
  region         = var.master-nodes-region
  zone           = var.master-nodes-zone
  project_id     = var.project_id
  n-master-nodes = 2
  network        = var.network_name
  subnetwork     = module.network.subnets["${var.master-nodes-region}/${var.master-nodes-subnet}"].name
}
module "worker-nodes" {
  depends_on     = [module.network]
  n-worker-nodes = 4
  source         = "../modules/worker-nodes"
  region         = var.worker-nodes-region
  zone           = var.worker-nodes-zone
  network        = var.network_name
  subnetwork     = module.network.subnets["${var.worker-nodes-region}/${var.worker-nodes-subnet}"].name
  project_id     = var.project_id
}