#
# Data Sources
#
data "triton_image" "ubuntu" {
  name        = "ubuntu-16.04"
  type        = "lx-dataset"
  most_recent = true
}

data "triton_network" "public" {
  name = "Joyent-SDC-Public"
}

data "triton_network" "private" {
  name = "My-Fabric-Network"
}

#
# General Variables
#
variable "kafka_environment" {
  description = "See module documentation."
  default     = "kafka-env"
}

variable "kafka_package" {
  description = "See module documentation."
  default     = "g4-general-4G"
}

variable "kafka_role_tag" {
  description = "See module documentation."
  default     = "kafka"
}

variable "kafka_provision" {
  description = "See module documentation."
  default     = "true"
}

variable "kafka_client_access" {
  description = "See module documentation."
  type        = "list"
  default     = ["all vms"]
}

#
# Zookeeper Variables
#
variable "zookeeper_role_tag" {
  description = "See module documentation."
  default     = "zookeeper"
}

variable "zookeeper_provision" {
  description = "See module documentation."
  default     = "true"
}

variable "zookeeper_client_access" {
  description = "See module documentation."
  type        = "list"
  default     = ["all vms"]
}

#
# Bastion Variables
#
variable "bastion_role_tag" {
  default = "bastion"
}

variable "bastion_user" {
  default = "root"
}

#
# Outputs
#
output "bastion_ip" {
  value = ["${module.bastion.bastion_ip}"]
}

output "kafka_ip" {
  value = ["${module.kafka.kafka_ip}"]
}

output "zookeeper_ip" {
  value = ["${module.zookeeper.zookeeper_ip}"]
}
