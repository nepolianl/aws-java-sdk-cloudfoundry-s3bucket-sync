package com.neps.aws.blobstore.s3.sync.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyS3Object {
	private String key;
	private String bucket;
	private String owner;
	private Date lastModified;
	private long fileSize;
	private Type type;
	private boolean copied;
	
	public enum Type {NEW, MODIFIED, DELETED, NONE}
}
