DESCRIPTION = "A library for developing device and application clients for IBM Internet of Things Foundation"
HOMEPAGE = "https://github.com/ibm-messaging/iot-nodejs"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=30b3836521b3d65bef598bbc358a3afa"

DEPENDS = "nodejs"
RDEPENDS_${PN} = "bash nodejs"

SRC_NAME = "iot-nodejs"

SRC_URI = "git://github.com/ibm-watson-iot/${SRC_NAME}.git;branch=master"
SRCREV = "14f023825c39441ccf069b8e98b98f72f3613bad"

PR = "r0"

S = "${WORKDIR}/git"

NODE_MODULES_DIR = "${prefix}/lib/node_modules"
NPM_CACHE_DIR ?= "${WORKDIR}/npm_cache"
NPM_REGISTRY ?= "https://registry.npmjs.org/"
NPM_INSTALL_FLAGS = "--production"

do_compile() {
	export NPM_CONFIG_CACHE="${NPM_CACHE_DIR}"
	
	# Clear cache
	npm cache clear

	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} install
	npm prune ${NPM_INSTALL_FLAGS}
}

do_install() {
	install -d ${D}${NODE_MODULES_DIR}/ibmiotf
	cp -r * ${D}${NODE_MODULES_DIR}/ibmiotf
}

PACKAGES = "${PN}"

FILES_${PN} += "${NODE_MODULES_DIR}"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
