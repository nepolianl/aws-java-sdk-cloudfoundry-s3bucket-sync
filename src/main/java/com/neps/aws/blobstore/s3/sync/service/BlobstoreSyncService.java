package com.neps.aws.blobstore.s3.sync.service;

import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Sync;

public interface BlobstoreSyncService {
	public void sync(BlobstoreS3Sync s3Sync);
}
