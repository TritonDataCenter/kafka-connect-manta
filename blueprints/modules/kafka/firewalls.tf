resource "triton_firewall_rule" "ssh" {
  rule        = "FROM tag \"role\" = \"${var.bastion_role_tag}\" TO tag \"role\" = \"${var.kafka_role_tag}\" ALLOW tcp PORT 22"
  enabled     = true
  description = "${var.kafka_environment} - Allow access from bastion hosts to Kafka servers."
}

resource "triton_firewall_rule" "kafka_server" {
  rule        = "FROM tag \"role\" = \"${var.kafka_role_tag}\" TO tag \"role\" = \"${var.kafka_role_tag}\" ALLOW tcp PORT 9092"
  enabled     = true
  description = "${var.kafka_environment} - Allow access from Kafka servers to Kafka servers."
}

resource "triton_firewall_rule" "kafka_client" {
  count = "${length(var.kafka_client_access)}"

  rule        = "FROM ${var.kafka_client_access[count.index]} TO tag \"role\" = \"${var.kafka_role_tag}\" ALLOW tcp PORT 9092"
  enabled     = true
  description = "${var.kafka_environment} - Allow access from clients to Kafka servers."
}
