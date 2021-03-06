DESCRIPTION = "Node-RED nodes for connecting to the IBM Watson platform"
HOMEPAGE = "https://github.com/ibm-messaging/iot-nodered"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=30b3836521b3d65bef598bbc358a3afa"

DEPENDS = "nodejs"
RDEPENDS_${PN} = "bash nodejs node-red"

SRC_NAME = "iot-nodered"

SRC_URI = "git://github.com/ibm-watson-iot/${SRC_NAME}.git;branch=master"
SRCREV = "aa4a3c4a3d3a1d0d7f36f85adb26522d4868bc0e"

PR = "r0"

S = "${WORKDIR}/git"

# Modules
IBM_WATSON_IOT = "${S}/node-red-contrib-ibm-watson-iot"

NODE_MODULES_DIR = "${prefix}/lib/node_modules"
NPM_CACHE_DIR ?= "${WORKDIR}/npm_cache"
NPM_REGISTRY ?= "https://registry.npmjs.org/"
NPM_INSTALL_FLAGS = "--production"

do_compile() {
	export NPM_CONFIG_CACHE="${NPM_CACHE_DIR}"
	
	# Clear cache
	npm cache clear

	cd ${IBM_WATSON_IOT}
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install
}

do_install() {
	install -d ${D}${NODE_MODULES_DIR}
	cp -r ${IBM_WATSON_IOT} ${D}${NODE_MODULES_DIR}/
}

PACKAGES = "${PN}"

FILES_${PN} += "${NODE_MODULES_DIR}"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
