echo "script runs"

set JAVA=%JAVA_HOME%\..\bin\java
set JAVAC=%JAVA_HOME%\..\bin\javac
set JAR=%JAVA_HOME%\..\bin\jar

set LOGUTIL_PACKAGE=com\sun\tools\corba\se\logutil
set LOGUTIL_SRC_DIR=%SOURCE_DIR%\%LOGUTIL_PACKAGE%
set LOGUTIL_TARGET=%TARGET_DIR%\logutil
set GENERATED_CLASS_DIR=%TARGET_DIR%\generated-sources\log\com\sun\corba\se\impl\logging
set MC_DIR=%SOURCE_DIR%\com\sun\corba\se\spi\logging\data

md %LOGUTIL_TARGET%\classes
JAVAC -d %LOGUTIL_TARGET%\classes %LOGUTIL_SRC_DIR%\*.java

echo "check " + %BASE_DIR%\logutil\manifest
dir %BASE_DIR%\logutil
echo "check 2 "+ %LOGUTIL_TARGET%\classes
dir %LOGUTIL_TARGET%\classes
echo "check 3" + %LOGUTIL_PACKAGE%
dir %LOGUTIL_TARGET%\classes\%LOGUTIL_PACKAGE%

echo "check 4"
dir %JAVA_HOME%

%JAR% cmf %BASE_DIR%\logutil\manifest %LOGUTIL_TARGET%\logutil.jar %LOGUTIL_TARGET%\classes\%LOGUTIL_PACKAGE%

mkdir %GENERATED_CLASS_DIR%
setlocal enableDelayedExpansion

for /F %%x in ('dir /B/D "%MC_DIR%\*.mc"') do (
  %JAVA% -jar %LOGUTIL_TARGET%\logutil.jar make-class "%MC_DIR%\%%x" %GENERATED_CLASS_DIR%
  %JAVA% -jar %LOGUTIL_TARGET%\logutil.jar make-resource "%MC_DIR%\%%x" %GENERATED_CLASS_DIR%
)
setlocal disableDelayedExpansion

echo type %GENERATED_CLASS_DIR%\*.resource > %GENERATED_CLASS_DIR%\LogStrings.properties
type %GENERATED_CLASS_DIR%\*.resource > %GENERATED_CLASS_DIR%\LogStrings.properties