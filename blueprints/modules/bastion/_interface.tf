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
variable "bastion_environment" {
  description = "The name of the environment."
  type        = "string"
}

variable "bastion_image" {
  description = "The image to deploy as the Bastion machines(s)."
  type        = "string"
}

variable "bastion_package" {
  description = "The package to deploy as the Bastion machines(s)."
  type        = "string"
}

variable "bastion_networks" {
  description = "The networks to deploy the Bastion machines(s) within."
  type        = "list"
}

variable "bastion_machine_count" {
  description = "The number of Bastion machines to provision."
  type        = "string"
  default     = "1"
}

variable "bastion_user" {
  description = <<EOF
The Bastion user to use for provisioning - setting this will NOT change any part of
provisioning. This is a pass-through variable.
EOF

  type    = "string"
  default = "root"
}

variable "bastion_ssh_client_access" {
  description = <<EOF
'From' targets to allow client access to Prometheus' web port - i.e. access from other VMs or public internet.
See https://docs.joyent.com/public-cloud/network/firewall/cloud-firewall-rules-reference#target
for target syntax.
EOF

  type    = "list"
  default = ["any"]
}

variable "bastion_role_tag" {
  description = "The 'role' tag for the Prometheus machine(s) to allow access FROM the Bastion machine(s)."
  type        = "string"
}

variable "bastion_cns_service_name" {
  description = "The Bastion CNS service name. Note: this is the service name only, not the full CNS record."
  type        = "string"
  default     = "bastion"
}

#
# Outputs
#
output "bastion_ip" {
  value = ["${triton_machine.bastion.*.primaryip}"]
}

output "bastion_user" {
  value = "${var.bastion_user}"
}

output "bastion_role_tag" {
  value = "${var.bastion_role_tag}"
}

output "bastion_cns_service_name" {
  value = "${var.bastion_cns_service_name}"
}
