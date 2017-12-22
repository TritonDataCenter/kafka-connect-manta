locals {
  name_instance = "${var.zookeeper_environment}-zookeeper"
}

resource "triton_machine" "zookeeper" {
  count = "${var.zookeeper_machine_count}"

  # The machine naming pattern is also used within install_zookeeper.sh to
  # create the zoo.cfg file. Be sure to keep these in sync if this changes.
  name = "${local.name_instance}-${count.index}"

  package = "${var.zookeeper_package}"
  image   = "${var.zookeeper_image}"

  firewall_enabled = true

  networks = ["${var.zookeeper_networks}"]

  tags {
    role = "${var.zookeeper_role_tag}"
  }

  cns {
    services = ["${var.zookeeper_cns_service_name}"]
  }

  metadata {
    zookeeper_version       = "${var.zookeeper_version}"
    zookeeper_name_prefix   = "${local.name_instance}"
    zookeeper_machine_count = "${var.zookeeper_machine_count}"
    zookeeper_machine_index = "${count.index}"
  }
}
