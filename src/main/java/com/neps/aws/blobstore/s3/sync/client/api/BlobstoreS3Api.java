package com.neps.aws.blobstore.s3.sync.client.api;

import java.io.File;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public interface BlobstoreS3Api {
	public List<S3ObjectSummary> listObjects();
	public File getObject(String key, File file);
	public void copyObject(String key, File file);
	
	public AmazonS3 getS3Client();
	public String getS3Bucket();
}
