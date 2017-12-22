resource "triton_machine" "kafka" {
  count = "${var.kafka_machine_count}"

  name = "${var.kafka_environment}-kafka-${count.index}"

  package = "${var.kafka_package}"
  image   = "${var.kafka_image}"

  firewall_enabled = true

  networks = ["${var.kafka_networks}"]

  tags {
    role = "${var.kafka_role_tag}"
  }

  cns {
    services = ["${var.kafka_cns_service_name}"]
  }

  metadata {
    kafka_confluent_version     = "${var.kafka_confluent_version}"
    kafka_connect_manta_version = "${var.kafka_connect_manta_version}"

    zookeeper_cns_service_name = "${var.zookeeper_cns_service_name}"
  }
}
