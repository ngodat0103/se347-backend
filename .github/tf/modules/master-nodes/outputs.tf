output "public-ips" {
  description = "Public IP of master nodes"
  value       = google_compute_instance.k8s-master-instances[*].network_interface[*].access_config[*].nat_ip
}
output "instances" {
  description = "master-instances"
  value       =  google_compute_instance.k8s-master-instances[*].self_link
}
output "internal-lb-ip" {
  description = "Internal LB IP"
  value       = google_compute_address.master-nodes-internal-lb-ip.address
}