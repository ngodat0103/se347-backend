output "worker-nodes-public-ip" {
  description = "Public IP of worker nodes"
  value       = google_compute_instance.k8s-worker-instances[*].network_interface[*].access_config[*].nat_ip
}