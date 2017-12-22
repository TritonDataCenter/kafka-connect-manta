resource "triton_machine" "bastion" {
  count = "${var.bastion_machine_count}"

  name    = "${var.bastion_environment}-bastion-${count.index}"
  package = "${var.bastion_package}"
  image   = "${var.bastion_image}"

  firewall_enabled = true

  networks = ["${var.bastion_networks}"]

  tags {
    role = "${var.bastion_role_tag}"
  }

  cns {
    services = ["${var.bastion_cns_service_name}"]
  }
}
