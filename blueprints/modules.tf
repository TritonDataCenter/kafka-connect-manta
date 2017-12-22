#
# Modules
#
module "bastion" {
  source = "modules/bastion"

  bastion_environment = "${var.kafka_environment}"
  bastion_package     = "${var.kafka_package}"
  bastion_image       = "${data.triton_image.ubuntu.id}"

  # Public and Private
  bastion_networks = [
    "${data.triton_network.private.id}",
    "${data.triton_network.public.id}",
  ]

  bastion_role_tag = "${var.bastion_role_tag}"
  bastion_user     = "${var.bastion_user}"
}

module "zookeeper" {
  source = "modules/zookeeper"

  zookeeper_environment = "${var.kafka_environment}"
  zookeeper_package     = "${var.kafka_package}"
  zookeeper_image       = "${data.triton_image.ubuntu.id}"

  # Private Only
  zookeeper_networks = [
    "${data.triton_network.private.id}",
  ]

  zookeeper_role_tag  = "${var.zookeeper_role_tag}"
  zookeeper_provision = "${var.zookeeper_provision}"

  zookeeper_client_access = [
    "${var.zookeeper_client_access}",
    "tag \"role\" = \"${var.kafka_role_tag}\"", # allow kafka access to zookeeper
  ]

  bastion_role_tag = "${module.bastion.bastion_role_tag}"
  bastion_user     = "${module.bastion.bastion_user}"
  bastion_host     = "${element(module.bastion.bastion_ip,0)}"
}

module "kafka" {
  source = "modules/kafka"

  kafka_environment = "${var.kafka_environment}"
  kafka_package     = "${var.kafka_package}"
  kafka_image       = "${data.triton_image.ubuntu.id}"

  # Private Only
  kafka_networks = [
    "${data.triton_network.private.id}",
  ]

  kafka_provision = "${var.kafka_provision}"

  kafka_client_access = [
    "${var.kafka_client_access}",
  ]

  zookeeper_cns_service_name = "${module.zookeeper.zookeeper_cns_service_name}"

  bastion_role_tag = "${var.bastion_role_tag}"
  bastion_user     = "${module.bastion.bastion_user}"
  bastion_host     = "${element(module.bastion.bastion_ip,0)}"
}
