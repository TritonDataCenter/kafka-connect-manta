terraform {
  required_version = ">= 0.11.0"
}

#
# Providers.
#
provider "triton" {
  version = ">= 0.3.0"
}

#
# Variables
#
variable "zookeeper_environment" {
  description = "The name of the environment."
  type        = "string"
}

variable "zookeeper_image" {
  description = "The image to deploy as the Zookeeper machine(s)."
  type        = "string"
}

variable "zookeeper_package" {
  description = "The package to deploy as the Zookeeper machine(s)."
  type        = "string"
}

variable "zookeeper_networks" {
  description = "The networks to deploy the Zookeeper machine(s) within."
  type        = "list"
}

variable "zookeeper_role_tag" {
  description = "The 'role' tag for the Zookeeper machine(s)."
  type        = "string"
  default     = "zookeeper"
}

variable "zookeeper_provision" {
  description = "Boolean 'switch' to indicate if Terraform should do the machine provisioning to install and configure Zookeeper."
  type        = "string"
}

variable "zookeeper_user" {
  description = "The User to use for provisioning the Zookeeper machine(s)."
  type        = "string"
  default     = "root"
}

variable "zookeeper_private_key_path" {
  description = "Path to the private key to use for connecting to machines."
  type        = "string"
  default     = "~/.ssh/id_rsa"
}

variable "zookeeper_version" {
  description = "The version of Zookeeper to install. See https://zookeeper.apache.org/releases.html."
  type        = "string"
  default     = "3.4.11"
}

variable "zookeeper_machine_count" {
  description = "The number of Zookeeper to provision."
  type        = "string"
  default     = "3"
}

variable "zookeeper_cns_service_name" {
  description = "The service name to use for Triton CNS."
  type        = "string"
  default     = "zookeeper"
}

variable "zookeeper_client_access" {
  description = <<EOF
'From' targets to allow client access to Prometheus' web port - i.e. access from other VMs or public internet.
See https://docs.joyent.com/public-cloud/network/firewall/cloud-firewall-rules-reference#target
for target syntax.
EOF

  type = "list"
}

variable "bastion_host" {
  description = "The Bastion host to use for provisioning."
  type        = "string"
}

variable "bastion_user" {
  description = "The Bastion user to use for provisioning."
  type        = "string"
}

variable "bastion_role_tag" {
  description = "The 'role' tag for the Prometheus machine(s) to allow access FROM the Bastion machine(s)."
  type        = "string"
}

#
# Outputs
#
output "zookeeper_ip" {
  value = ["${triton_machine.zookeeper.*.primaryip}"]
}

output "zookeeper_role_tag" {
  value = "${var.zookeeper_role_tag}"
}

output "zookeeper_cns_service_name" {
  value = "${var.zookeeper_cns_service_name}"
}
