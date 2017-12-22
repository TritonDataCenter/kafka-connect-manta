resource "triton_firewall_rule" "ssh" {
  count = "${length(var.bastion_ssh_client_access)}"

  rule        = "FROM ${var.bastion_ssh_client_access[count.index]} TO tag \"role\" = \"${var.bastion_role_tag}\" ALLOW tcp PORT 22"
  enabled     = true
  description = "${var.bastion_environment} - Allow access from clients to Bastion servers."
}
