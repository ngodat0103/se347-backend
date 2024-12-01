variable "n-master-nodes" {
  description = "Number of instances for master nodes"
  type        = number
  default     = 2
}
variable "name" {
  description = "The name"
  type        = string
  default    = "master-nodes"
  
}
variable "machine_type" {
  description = "Machine type for instances"
  type        = string
  default     = "e2-medium"
}
variable "zone" {
  description = "Zone for instances"
  type        = string
  default     = "asia-southeast1-a"
}

variable "environment" {
  description = "Environment for instances"
  type        = string
  default     = "dev"
}
variable "image" {
    description = "Image for instances"
    type        = string
    default     = "ubuntu-2204-jammy-v20240927"
}
variable "pub_key_path" {
    description = "Path to public key"
    type        = string
    default     = "/tmp/secrets/id_rsa.pub"
}
variable boot_disk_size {
  description = "Size of boot disk"
  type        = number
  default     = 30
}
variable "network" {
  description = "Network for instances"
  type        = string
}


variable "subnetwork" {
  description = "Subnetwork "
  type        = string
}
variable "region" {
  description = "Region for the master load balancer"
  type        = string
}
variable "project_id" {
  description = "The project ID"
  type        = string
}


variable "master-nodes-tag" {
  type = list(string)
  description = ""
  default = ["master-nodes"]
}
variable "worker-nodes-tag" {
  type = list(string)
  description = ""
  default = ["worker-nodes"]
}
variable "cluster-tag" {
  type = list(string)
  description = ""
  default = ["master-nodes", "worker-nodes"]
}
variable "ports" {
  type = list(number)
  description = "The port for K8s API server"
  default = [6443]
}
variable "health_check" {
  type = map(string)
  description = "The health check configuration"
  default = {
    type                = "https"
    check_interval_sec  = 1
    healthy_threshold   = 4
    timeout_sec         = 1
    unhealthy_threshold = 5
    proxy_header        = "NONE"
    port                = 6443
    port_name           = "master-nodes-api-server-port"
    request             = ""
    request_path        = "/readyz"
    host                = ""
    enable_log = true
  }
}