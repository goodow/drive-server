export DEV_SERVER=$HOME/.m2/repository/com/google/appengine/appengine-java-sdk/1.8.1.1/appengine-java-sdk/appengine-java-sdk-1.8.1.1/bin/dev_appserver.sh
chmod 770 $DEV_SERVER
$DEV_SERVER \
  --jvm_flag=-Xmx2048m \
  --disable_update_check --address=0.0.0.0 --port=8880 \
  target/drive-server-0.0.1-SNAPSHOT
