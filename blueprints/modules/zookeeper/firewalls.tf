resource "triton_firewall_rule" "ssh" {
  rule        = "FROM tag \"role\" = \"${var.bastion_role_tag}\" TO tag \"role\" = \"${var.zookeeper_role_tag}\" ALLOW tcp PORT 22"
  enabled     = true
  description = "${var.zookeeper_environment} - Allow access from bastion hosts to Zookeeper servers."
}

resource "triton_firewall_rule" "zookeeper_server" {
  rule        = "FROM tag \"role\" = \"${var.zookeeper_role_tag}\" TO tag \"role\" = \"${var.zookeeper_role_tag}\" ALLOW tcp (PORT 2888 AND PORT 3888)"
  enabled     = true
  description = "${var.zookeeper_environment} - Allow access from Zookeeper servers to Zookeeper servers."
}

resource "triton_firewall_rule" "zookeeper_client" {
  count = "${length(var.zookeeper_client_access)}"

  rule        = "FROM ${var.zookeeper_client_access[count.index]} TO tag \"role\" = \"${var.zookeeper_role_tag}\" ALLOW tcp PORT 2181"
  enabled     = true
  description = "${var.zookeeper_environment} - Allow access from Zookeeper servers to Zookeeper servers."
}
