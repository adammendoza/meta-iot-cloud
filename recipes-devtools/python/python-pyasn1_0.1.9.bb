SUMMARY = "ASN.1 types and codecs"
AUTHOR = "Ilya Etingof"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=425e62320d430219736139b134db2fc4"

inherit setuptools

PR = "r1"

SRC_NAME = "pyasn1"

SRC_URI = "https://pypi.python.org/packages/source/p/${SRC_NAME}/${SRC_NAME}-${PV}.tar.gz"
SRC_URI[md5sum] = "f00a02a631d4016818659d1cc38d229a"
SRC_URI[sha256sum] = "853cacd96d1f701ddd67aa03ecc05f51890135b7262e922710112f12a2ed2a7f"

S = "${WORKDIR}/${SRC_NAME}-${PV}"
