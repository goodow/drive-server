package com.goodow.drive.server.attachment;

import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Attachment {
  @Id
  private String id;
  private String filename;
  private String contentType;
  private String blobKey;
  private Date creation;
  @ElementCollection
  private List<String> tags;
  private String thumbnail;

  public String getBlobKey() {
    return blobKey;
  }

  public String getContentType() {
    return contentType;
  }

  public Date getCreation() {
    return creation;
  }

  public String getFilename() {
    return filename;
  }

  public String getId() {
    return id;
  }

  public List<String> getTags() {
    return tags;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setBlobKey(String blobKey) {
    this.blobKey = blobKey;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setCreation(Date creation) {
    this.creation = creation;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }
}
