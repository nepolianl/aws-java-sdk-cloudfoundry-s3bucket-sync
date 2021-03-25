package com.neps.aws.blobstore.s3.sync.client.api;

import com.neps.aws.blobstore.s3.sync.exception.SyncException;
import com.neps.aws.blobstore.s3.sync.model.S3SyncObject;

public interface BlobstoreS3Sync {
	public S3SyncObject sync(BlobstoreS3Api s3Api) throws SyncException;
}
