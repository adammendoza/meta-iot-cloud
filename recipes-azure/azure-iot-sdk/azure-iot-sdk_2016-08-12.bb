DESCRIPTION = "Microsoft Azure IoT device SDK"
HOMEPAGE = "https://github.com/Azure/azure-iot-sdks"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4283671594edec4c13aeb073c219237a"

DEPENDS = "azure-c-shared-utility"

inherit cmake pkgconfig python-dir java

SRC_URI = "gitsm://github.com/Azure/azure-iot-sdks.git \
	   file://cmake_fixes.patch \
"
SRCREV = "0a559430fa22575d4e98c227ad4c251e273e7342"

PR = "r3"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

## NPM ##
NODE_MODULES_DIR = "${prefix}/lib/node_modules/"
NPM_CACHE_DIR ?= "${WORKDIR}/npm_cache"
NPM_REGISTRY ?= "https://registry.npmjs.org/"
NPM_INSTALL_FLAGS ?= "--production --no-optional --verbose"

## C ##
C_SAMPLES_DIR = "${B}/iothub_client/samples"
AMQP_SAMPLES_DIR = "${B}/azure-uamqp-c/samples"
MQTT_SAMPLES_DIR = "${B}/azure-umqtt-c/samples"
SERIALIZER_SAMPLES_DIR = "${B}/serializer/samples"
AZURE_INCLUDE_DIR = "${STAGING_INCDIR}/azureiot"

## Node ##
NODE_SRC_DIR = "${S}/node/device/core"
NODE_SAMPLES_DIR = "${S}/node/device/samples"
NODE_TRANSPORT_DIR = "${S}/node/device/transport"
NODE_PN = "azure-iot-device"

## Node-RED ##
NODE_RED_SRC_DIR = "${S}/node/device/node-red"
NODE_RED_PN = "node-red-contrib-azureiothubnode"

## Python ##
PYTHON_SRC_DIR = "${S}/python/device"

## Java ##
JAVA_SRC_DIR = "${S}/java/device"
JAVA_DEST_DIR = "${JAVA_SRC_DIR}/iothub-java-client/target"
JAVA_PN = "iothub-java-device-client"

## IoT Hub Explorer ##
IOTHUB_EXPLORER_SRC_DIR = "${S}/tools/iothub-explorer"
IOTHUB_EXPLORER_PN = "iothub-explorer"

# List of packages to build
PACKAGES = "${PN} ${PN}-dev ${PN}-dbg ${PN}-samples python-${PN} python-${PN}-dbg node-${NODE_PN} node-${NODE_PN}-amqp node-${NODE_PN}-amqp-ws node-${NODE_PN}-http node-${NODE_PN}-mqtt ${NODE_RED_PN} node-${IOTHUB_EXPLORER_PN} ${JAVA_PN}"

# Package configuration options
PACKAGECONFIG ??= "python nodejs node-red java http amqp mqtt"
PACKAGECONFIG[python] = "-Dbuild_python:STRING=2.7, -Dbuild_python:STRING=OFF, ${PYTHON_PN} boost"
PACKAGECONFIG[nodejs] = ",, nodejs-native"
PACKAGECONFIG[node-red] = ",, node-red"
PACKAGECONFIG[java] = ",, maven-native icedtea7-native"
PACKAGECONFIG[http] = "-Duse_http:BOOL=ON,-Duse_http:BOOL=OFF,"
PACKAGECONFIG[amqp] = "-Duse_amqp:BOOL=ON -DUAMQP_INCLUDE_DIR=${AZURE_INCLUDE_DIR},-Duse_amqp:BOOL=OFF, azure-uamqp-c"
PACKAGECONFIG[mqtt] = "-Duse_mqtt:BOOL=ON -DMQTT_INCLUDE_DIR=${AZURE_INCLUDE_DIR},-Duse_mqtt:BOOL=OFF, azure-umqtt-c"

## CMake ##
OECMAKE_SOURCEPATH = "${WORKDIR}/git/c"
EXTRA_OECMAKE = "-DBUILD_SHARED_LIBS:BOOL=ON -DSHARED_UTIL_INCLUDE_DIR=${AZURE_INCLUDE_DIR} -Drun_e2e_tests:BOOL=OFF -Drun_longhaul_tests=OFF -Dskip_unittests:BOOL=ON -Dbuild_javawrapper:STRING=OFF"

do_compile_append() {
    export NPM_CONFIG_CACHE="${NPM_CACHE_DIR}"

    if ${@bb.utils.contains('PACKAGECONFIG','nodejs','true','false',d)}; then
	# Node Device SDK
	cd ${NODE_SRC_DIR}
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# Node AMQP Transport
	cd ${NODE_TRANSPORT_DIR}/amqp
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# Node AMQP-WS Transport
	cd ${NODE_TRANSPORT_DIR}/amqp-ws
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# Node HTTP Transport
	cd ${NODE_TRANSPORT_DIR}/http
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# Node MQTT Transport
	cd ${NODE_TRANSPORT_DIR}/mqtt
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# IoT Hub Explorer
	cd ${IOTHUB_EXPLORER_SRC_DIR}
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# FIXME: This is only required until the xml2js dependency is update in the azure-storage package
	find ${S}/node/device -type f -name "switch-bench.js" -exec rm -f {} \;
	find ${S}/tools/iothub-explorer -type f -name "switch-bench.js" -exec rm -f {} \;
    fi

    if ${@bb.utils.contains('PACKAGECONFIG','node-red','true','false',d)}; then
	cd ${NODE_RED_SRC_DIR}
	npm cache clear
	npm --registry=${NPM_REGISTRY} --arch=${TARGET_ARCH} --target_arch=${TARGET_ARCH} ${NPM_INSTALL_FLAGS} install

	# FIXME: This is only required until the xml2js dependency is update in the azure-storage package
	find . -type f -name "switch-bench.js" -exec rm -f {} \;
    fi

    if ${@bb.utils.contains('PACKAGECONFIG','java','true','false',d)}; then
	export JAVA_HOME="${STAGING_LIBDIR_JVM_NATIVE}/icedtea7-native"
	export M3_HOME="${STAGING_DIR_NATIVE}/usr/bin/maven-native"
	cd ${JAVA_SRC_DIR}
	mvn install
    fi
}

do_install_append() {
    # Python
    if [ -e ${B}/python/src/iothub_client.so ]; then
	install -d ${D}${PYTHON_SITEPACKAGES_DIR}
	oe_libinstall -C ${B}/python/src -so iothub_client ${D}${PYTHON_SITEPACKAGES_DIR}
    fi

    # Node
    install -d ${D}${NODE_MODULES_DIR}/${NODE_PN}
    install -d ${D}${NODE_MODULES_DIR}/${NODE_PN}-amqp
    install -d ${D}${NODE_MODULES_DIR}/${NODE_PN}-amqp-ws
    install -d ${D}${NODE_MODULES_DIR}/${NODE_PN}-http
    install -d ${D}${NODE_MODULES_DIR}/${NODE_PN}-mqtt
    cp -r ${NODE_SRC_DIR}/* ${D}${NODE_MODULES_DIR}/${NODE_PN}
    cp -r ${NODE_TRANSPORT_DIR}/amqp/* ${D}${NODE_MODULES_DIR}/${NODE_PN}-amqp
    cp -r ${NODE_TRANSPORT_DIR}/amqp-ws/* ${D}${NODE_MODULES_DIR}/${NODE_PN}-amqp-ws
    cp -r ${NODE_TRANSPORT_DIR}/http/* ${D}${NODE_MODULES_DIR}/${NODE_PN}-http
    cp -r ${NODE_TRANSPORT_DIR}/mqtt/* ${D}${NODE_MODULES_DIR}/${NODE_PN}-mqtt

    # Node-RED
    install -d ${D}${NODE_MODULES_DIR}/${NODE_RED_PN}
    cp -r ${NODE_RED_SRC_DIR}/* ${D}${NODE_MODULES_DIR}/${NODE_RED_PN}

    # IoT Hub Explorer
    install -d ${D}${NODE_MODULES_DIR}/${IOTHUB_EXPLORER_PN}
    cp -r ${IOTHUB_EXPLORER_SRC_DIR}/* ${D}${NODE_MODULES_DIR}/${IOTHUB_EXPLORER_PN}

    # Java
    if [ -e ${JAVA_DEST_DIR} ]; then
	cd ${JAVA_DEST_DIR}
	jar_version=$(ls iothub-java-client-*-with-deps.jar | cut -d '-' -f4) 
        oe_jarinstall -r ${JAVA_PN}-${jar_version}.jar ${JAVA_DEST_DIR}/iothub-java-client-${jar_version}-with-deps.jar ${JAVA_PN}.jar
	
        # Java Samples
        install -d ${D}${datadir}/azureiotsdk/samples/java/device
        install -m 0755 ${JAVA_SRC_DIR}/samples/handle-messages/target/handle-messages-*-with-deps.jar ${D}${datadir}/azureiotsdk/samples/java/device/handle-messages.jar
        install -m 0755 ${JAVA_SRC_DIR}/samples/send-event/target/send-event-*-with-deps.jar ${D}${datadir}/azureiotsdk/samples/java/device/send-event.jar
        install -m 0755 ${JAVA_SRC_DIR}/samples/send-receive-sample/target/send-receive-sample-*-with-deps.jar ${D}${datadir}/azureiotsdk/samples/java/device/send-receive-sample.jar
        install -m 0755 ${JAVA_SRC_DIR}/samples/send-serialized-event/target/send-serialized-event-*-with-deps.jar ${D}${datadir}/azureiotsdk/samples/java/device/send-serialized-event.jar
    fi
}

## C ##
FILES_${PN} = "${libdir}/*.so"
FILES_${PN}-dev += "${includedir}"
INSANE_SKIP_${PN} += "rpaths"

## Python ##
RDEPENDS_python-${PN} += "python"
RPROVIDES_python-${PN} += "iothub_client.so"
FILES_python-${PN} += "${PYTHON_SITEPACKAGES_DIR}/*.so"
FILES_python-${PN}-dbg += "${PYTHON_SITEPACKAGES_DIR}/.debug"
INSANE_SKIP_python-${PN} += "rpaths"

## Node ##
RDEPENDS_node-${NODE_PN} += "nodejs bash"
RDEPENDS_node-${NODE_PN}-amqp += "nodejs bash"
RDEPENDS_node-${NODE_PN}-amqp-ws += "nodejs bash"
RDEPENDS_node-${NODE_PN}-http += "nodejs bash"
RDEPENDS_node-${NODE_PN}-mqtt += "nodejs bash"
FILES_node-${NODE_PN} += "${NODE_MODULES_DIR}${NODE_PN}"
FILES_node-${NODE_PN}-amqp += "${NODE_MODULES_DIR}${NODE_PN}-amqp"
FILES_node-${NODE_PN}-amqp-ws += "${NODE_MODULES_DIR}${NODE_PN}-amqp-ws"
FILES_node-${NODE_PN}-http += "${NODE_MODULES_DIR}${NODE_PN}-http"
FILES_node-${NODE_PN}-mqtt += "${NODE_MODULES_DIR}${NODE_PN}-mqtt"
INHIBIT_PACKAGE_DEBUG_SPLIT_node-${NODE_PN} = "1"

## Node-RED ##
RDEPENDS_${NODE_RED_PN} += "nodejs node-red"
FILES_${NODE_RED_PN} += "${NODE_MODULES_DIR}${NODE_RED_PN}"
INHIBIT_PACKAGE_DEBUG_SPLIT_${NODE_RED_PN} = "1"

## IoT Hub Explorer ##
RDEPENDS_node-${IOTHUB_EXPLORER_PN} += "nodejs bash"
FILES_node-${IOTHUB_EXPLORER_PN} += "${NODE_MODULES_DIR}${IOTHUB_EXPLORER_PN}"
INHIBIT_PACKAGE_DEBUG_SPLIT_node-${IOTHUB_EXPLORER_PN} = "1"

pkg_postinst_node-${IOTHUB_EXPLORER_PN}() {
#!/bin/sh
# Post installation script

ln -s ${NODE_MODULES_DIR}${IOTHUB_EXPLORER_PN}/${IOTHUB_EXPLORER_PN}.js ${bindir}/${IOTHUB_EXPLORER_PN}
chmod 755 ${bindir}/${IOTHUB_EXPLORER_PN}

}

pkg_prerm-node-${IOTHUB_EXPLORER_PN}() {
#!/bin/sh
# Pre removal script

rm ${bindir}/${IOTHUB_EXPLORER_PN}

}

## Java ##
FILES_${JAVA_PN} += "${datadir_java}"

## Samples ##
FILES_${PN}-samples += "${datadir}/azureiotsdk/samples/java"

RRECOMMENDS_azure-iot-sdk-dev = "glibc-dev azure-c-shared-utility-dev azure-uamqp-c azure-umqtt-c"
RRECOMMENDS_azure-iot-sdk-dev[nodeprrecs] = "1"
