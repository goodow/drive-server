package com.goodow.drive.server.attachment;

import com.goodow.realtime.operation.id.IdGenerator;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

@Api(name = "attachment", version = "v0.0.1", defaultVersion = AnnotationBoolean.TRUE, namespace = @ApiNamespace(ownerDomain = "goodow.com", ownerName = "Goodow", packagePath = "api.services"))
public class AttachmentEndpoint {
  @Inject
  private IdGenerator idGenerator;
  @Inject
  private BlobstoreService blobstoreService;
  @Inject
  private BlobInfoFactory blobInfoFactory;
  @Inject
  private Provider<EntityManager> em;

  @ApiMethod(name = "get")
  public Attachment get(@Named("id") String id) {
    return em.get().find(Attachment.class, id);
  }

  @ApiMethod(name = "getFormAction")
  public BoxedString getFormAction() {
    String formAction = blobstoreService.createUploadUrl("/upload");
    if (formAction == null) {
      throw new RuntimeException("Null blobstore upload url");
    }
    return new BoxedString(formAction.substring(formAction.indexOf("/_ah/upload/")));
  }

  @ApiMethod(name = "insert")
  @Transactional
  public Attachment insert(Attachment attachment) {
    if (attachment.getId() == null) {
      attachment.setId(idGenerator.next(115));
    }
    if (contains(attachment)) {
      throw new EntityExistsException("Object already exists");
    }
    em.get().persist(attachment);
    return attachment;
  }

  @ApiMethod(name = "loadBlobInfo")
  public BlobInfo loadBlobInfo(@Named("blobKey") String blobKey) {
    return blobInfoFactory.loadBlobInfo(new BlobKey(blobKey));
  }

  @ApiMethod(name = "remove", httpMethod = HttpMethod.POST)
  public Attachment remove(@Named("id") String id) {
    Attachment attachment = em.get().find(Attachment.class, id);
    blobstoreService.delete(new BlobKey(attachment.getBlobKey()));
    em.get().remove(attachment);
    return attachment;
  }

  @SuppressWarnings({"unchecked", "unused"})
  @ApiMethod(name = "search", httpMethod = HttpMethod.GET)
  public CollectionResponse<Attachment> search(@Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit, @Nullable @Named("filename") String filename,
      @Nullable @Named("tags") List<String> tags,
      @Nullable @Named("contentType") String contentType, @Nullable @Named("before") Date before,
      @Nullable @Named("after") Date after) {

    Cursor cursor = null;
    List<Attachment> execute = null;

    StringBuilder select = new StringBuilder("select from Attachment as a");
    StringBuilder join = new StringBuilder("");
    StringBuilder where = new StringBuilder("");
    Map<String, Object> params = new HashMap<String, Object>();

    if (filename != null && !filename.isEmpty()) {
      where.append(" and a.filename like :filename");
      params.put("filename", filename + "%");
    }
    if (tags != null && !tags.isEmpty()) {
      join.append(" join a.tags tags");
      int i = 0;
      for (String tag : tags) {
        where.append(" and tags = :tag").append(i);
        params.put("tag" + i, tag);
        i++;
      }
    }
    if (contentType != null && !contentType.isEmpty()) {
      where.append(" and a.contentType like :contentType");
      params.put("contentType", contentType + "%");
    }
    if (before != null) {
      where.append(" and a.creation < :before");
      params.put("before", before);
    }
    if (after != null) {
      where.append(" and a.creation > :after");
      params.put("after", after);
    }
    int indexOf = where.indexOf("and ");
    if (indexOf != -1) {
      where.replace(indexOf, indexOf + "and ".length(), " where ");
    }
    Query query = em.get().createQuery(select.append(join).append(where).toString());
    if (cursorString != null && !"".equals(cursorString)) {
      cursor = Cursor.fromWebSafeString(cursorString);
      query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
    }
    if (limit != null) {
      query.setFirstResult(0);
      query.setMaxResults(limit);
    }
    for (Map.Entry<String, Object> param : params.entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
    }

    execute = query.getResultList();
    cursor = JPACursorHelper.getCursor(execute);
    if (cursor != null) {
      cursorString = cursor.toWebSafeString();
    }

    // Tight loop for fetching all entities from datastore and accomodate
    // for lazy fetch.
    for (Attachment obj : execute) {
      ;
    }
    return CollectionResponse.<Attachment> builder().setItems(execute).setNextPageToken(
        cursorString).build();
  }

  @ApiMethod(path = "update", httpMethod = HttpMethod.POST)
  public Attachment update(Attachment attachment) {
    if (!contains(attachment)) {
      throw new EntityNotFoundException("Object does not exist");
    }
    em.get().persist(attachment);
    return attachment;
  }

  private boolean contains(Attachment attachment) {
    boolean contains = true;
    Attachment item = em.get().find(Attachment.class, attachment.getId());
    if (item == null) {
      contains = false;
    }
    return contains;
  }
}
