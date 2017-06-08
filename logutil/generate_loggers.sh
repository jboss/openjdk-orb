#!/bin/sh

JAVA=${JAVA_HOME}/bin/java
JAVAC=${JAVA_HOME}/bin/javac
JAR=${JAVA_HOME}/bin/jar

LOGUTIL_PACKAGE=com/sun/tools/corba/se/logutil
LOGUTIL_SRC_DIR=${SOURCE_DIR}/${LOGUTIL_PACKAGE}
LOGUTIL_TARGET=${TARGET_DIR}/logutil
GENERATED_CLASS_DIR=${TARGET_DIR}/generated-sources/log/com/sun/corba/se/impl/logging
MC_DIR=${SOURCE_DIR}/com/sun/corba/se/spi/logging/data

mkdir -p ${LOGUTIL_TARGET}/classes
${JAVAC} -d ${LOGUTIL_TARGET}/classes ${LOGUTIL_SRC_DIR}/*.java

${JAR} cmf ${BASE_DIR}/logutil/manifest ${LOGUTIL_TARGET}/logutil.jar -C ${LOGUTIL_TARGET}/classes ${LOGUTIL_PACKAGE}

mkdir -p ${GENERATED_CLASS_DIR}
for file in ${MC_DIR}/*.mc
do
	${JAVA} -jar ${LOGUTIL_TARGET}/logutil.jar make-class ${file} ${GENERATED_CLASS_DIR}
	${JAVA} -jar ${LOGUTIL_TARGET}/logutil.jar make-resource ${file} ${GENERATED_CLASS_DIR}
done

cat ${GENERATED_CLASS_DIR}/*.resource > ${GENERATED_CLASS_DIR}/LogStrings.properties
