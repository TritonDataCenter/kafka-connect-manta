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
variable "kafka_environment" {
  description = "The name of the environment."
  type        = "string"
}

variable "kafka_image" {
  description = "The image to deploy as the Kafka machine(s)."
  type        = "string"
}

variable "kafka_package" {
  description = "The package to deploy as the Kafka machine(s)."
  type        = "string"
}

variable "kafka_networks" {
  description = "The networks to deploy the Kafka machine(s) within."
  type        = "list"
}

variable "kafka_role_tag" {
  description = "The 'role' tag for the Kafka machine(s)."
  type        = "string"
  default     = "kafka"
}

variable "kafka_provision" {
  description = "Boolean 'switch' to indicate if Terraform should do the machine provisioning to install and configure Kafka."
  type        = "string"
}

variable "kafka_user" {
  description = "The User to use for provisioning the Kafka machine(s)."
  type        = "string"
  default     = "root"
}

variable "kafka_private_key_path" {
  description = "Path to the private key to use for connecting to machines."
  type        = "string"
  default     = "~/.ssh/id_rsa"
}

variable "kafka_confluent_version" {
  description = "The version of Kafka Confluent to install. See https://docs.confluent.io/current/."
  type        = "string"
  default     = "4.0"
}

variable "kafka_connect_manta_version" {
  description = "The version of Kafka Connect Manta to install. See https://github.com/joyent/kafka-connect-manta/releases."
  type        = "string"
  default     = "1.0.0-SNAPSHOT"
}

variable "kafka_machine_count" {
  description = "The number of Kafka machines to provision."
  type        = "string"
  default     = "3"
}

variable "kafka_cns_service_name" {
  description = "The Kafka CNS service name. Note: this is the service name only, not the full CNS record."
  type        = "string"
  default     = "kafka"
}

variable "kafka_client_access" {
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

variable "zookeeper_cns_service_name" {
  description = "The Zookeeper CNS service name. Note: this is the service name only, not the full CNS record."
  type        = "string"
}

#
# Outputs
#
output "kafka_ip" {
  value = ["${triton_machine.kafka.*.primaryip}"]
}

output "kafka_role_tag" {
  value = "${var.kafka_role_tag}"
}

output "kafka_cns_service_name" {
  value = "${var.kafka_cns_service_name}"
}
