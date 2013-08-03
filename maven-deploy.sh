mvn appengine:endpoints_get_client_lib
cd target/generated-sources/appengine-endpoints/WEB-INF/
rm -rf attachment
unzip -q attachment-v0.0.1-java.zip
cd attachment
unzip -q `ls | grep sources.jar` -d maven
cd maven
sed -i.bak "s/com.google.apis/com.goodow.apis/;s/google-api-services/goodow-api-services/;s/v0.0.1-[^<]*/0.0.1-SNAPSHOT/" pom.xml
mvn clean deploy -Psonatype-oss-release
