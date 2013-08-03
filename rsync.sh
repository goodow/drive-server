rsync -avz --delete \
  --exclude "WEB-INF/appengine-generated" \
  target/drive-server-0.0.1-SNAPSHOT \
  dev@192.168.1.15:/home/dev/dev/workspace/drive/drive-server/target
