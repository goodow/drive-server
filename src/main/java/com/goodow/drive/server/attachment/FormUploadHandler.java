package com.goodow.drive.server.attachment;

import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.realtime.channel.util.IdGenerator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FormUploadHandler implements Handler<HttpServerRequest> {
  private static final Logger log = Logger.getLogger(FormUploadHandler.class.getName());
  @Inject @Named(DriveModule.ATTACHMENTS_DIR) private String attachmentsDir;
  @Inject IdGenerator idGenerator;
  @Inject Vertx vertx;

  @Override
  public void handle(final HttpServerRequest req) {
    req.expectMultiPart(true);
    req.uploadHandler(new Handler<HttpServerFileUpload>() {
      @Override
      public void handle(final HttpServerFileUpload upload) {
        final String id = idGenerator.next(115);
        upload.exceptionHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable e) {
            sendError(req, "Upload failed", e);
          }
        });
        upload.endHandler(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            // prepareIndex(id, req, upload);
          }
        });
        upload.streamToFileSystem(attachmentsDir + "/" + id);
      }
    });
  }

  // private void indexAttachment(final HttpServerRequest req, IndexRequestBuilder
  // indexRequestBuilder) {
  // indexRequestBuilder.execute(new ActionListener<IndexResponse>() {
  // @Override
  // public void onFailure(Throwable e) {
  // sendError(req, "Failed to index", e);
  // }
  //
  // @Override
  // public void onResponse(IndexResponse response) {
  // sendResponse(req, response);
  // }
  // });
  // }
  //
  // private void prepareIndex(final String id, final HttpServerRequest req,
  // final HttpServerFileUpload upload) {
  // final IndexRequestBuilder indexReq =
  // client.prepareIndex(DriveModule.INDEX, AttachmentUtil.isBinary(upload.contentType())
  // ? DriveModule.BINARY_TYPE : DriveModule.READABLE_TYPE, id);
  // vertx.fileSystem().readFile(attachmentsDir + "/" + id, new AsyncResultHandler<Buffer>() {
  // @Override
  // public void handle(AsyncResult<Buffer> ar) {
  // if (!ar.succeeded()) {
  // sendError(req, "Failed to read file", ar.cause());
  // return;
  // }
  // try {
  // XContentBuilder doc =
  // XContentFactory.jsonBuilder().startObject().startObject("file").field("_name",
  // upload.filename()).field("_content_type", upload.contentType()).field("content",
  // ar.result().getBytes()).endObject().endObject();
  // IndexRequestBuilder indexRequestBuilder = indexReq.setSource(doc);
  // indexAttachment(req, indexRequestBuilder);
  // } catch (IOException e) {
  // sendError(req, "Failed to construct document", e);
  // }
  // }
  // });
  // }

  private void sendError(final HttpServerRequest req, String msg, Throwable e) {
    log.log(Level.SEVERE, msg, e);
    req.response().setStatusMessage(msg).setStatusCode(500).end();
  }

  // private void sendResponse(final HttpServerRequest req, IndexResponse response) {
  // if (response.isCreated()) {
  // req.response().end(response.getId());
  // } else {
  // sendError(req, "id was already exits", null);
  // }
  // }
}