resource "null_resource" "kafka_install" {
  count = "${var.kafka_provision == "true" ? var.kafka_machine_count : 0}"

  triggers {
    machine_ids = "${triton_machine.kafka.*.id[count.index]}"
  }

  connection {
    bastion_host        = "${var.bastion_host}"
    bastion_user        = "${var.bastion_user}"
    bastion_private_key = "${file(var.kafka_private_key_path)}"

    host        = "${triton_machine.kafka.*.primaryip[count.index]}"
    user        = "${var.kafka_user}"
    private_key = "${file(var.kafka_private_key_path)}"
  }

  provisioner "remote-exec" {
    inline = [
      "mkdir -p /tmp/kafka_installer/",
    ]
  }

  provisioner "file" {
    source      = "${path.module}/scripts/install_kafka.sh"
    destination = "/tmp/kafka_installer/install_kafka.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod 0755 /tmp/kafka_installer/install_kafka.sh",
      "/tmp/kafka_installer/install_kafka.sh",
    ]
  }

  # clean up
  provisioner "remote-exec" {
    inline = [
      "rm -rf /tmp/kafka_installer/",
    ]
  }
}
