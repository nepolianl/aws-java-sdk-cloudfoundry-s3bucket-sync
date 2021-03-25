package com.neps.aws.blobstore.s3.sync.client.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.neps.aws.blobstore.s3.sync.client.BlobstoreWebServiceClient;
import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Api;
import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Sync;
import com.neps.aws.blobstore.s3.sync.exception.SyncException;
import com.neps.aws.blobstore.s3.sync.exception.SyncRuntimeException;
import com.neps.aws.blobstore.s3.sync.model.CopyS3Object;
import com.neps.aws.blobstore.s3.sync.model.S3SyncObject;
import com.neps.aws.blobstore.s3.sync.model.CopyS3Object.Type;
import com.neps.aws.blobstore.s3.sync.property.Credentials;
import com.neps.aws.blobstore.s3.sync.property.ProxyProperties;
import com.neps.aws.blobstore.s3.sync.util.Utils;

public class BlobstoreS3Client extends BlobstoreWebServiceClient implements BlobstoreS3Sync {
	private static final Logger logger = Logger.getLogger(BlobstoreS3Client.class.getName());
	
	public BlobstoreS3Client(Credentials credentials, ProxyProperties proxy) {
		super(credentials, proxy);
	}
	
	@Override
	public S3SyncObject sync(BlobstoreS3Api s3Api) throws SyncException {
		try {
			S3SyncObject syncObject = this.getDistinctObjects(s3Api);
			if (syncObject.getCopyList() == null || syncObject.getCopyList().isEmpty()) {
				logger.log(Level.INFO, "No S3Object found for s3 sync");
			} else {
				logger.log(Level.INFO, "Distinct S3Objects to copy: {0}", syncObject.getCopyList().size());
				AtomicInteger iteration = new AtomicInteger(1);
				List<CopyS3Object> copiedList = syncObject.getCopyList().stream().map(copyObject -> copyS3Object(copyObject, s3Api, syncObject, iteration)).collect(Collectors.toList());;
				syncObject.setCopyList(copiedList);
			}
			return syncObject;
		} catch (Exception e) {
			throw new SyncException("Could not complete s3 sync ", e);
		}
		
	}

	@Override
	public Map<String, S3ObjectSummary> getS3ObjectSummaryMap(List<S3ObjectSummary> objectList) throws SyncRuntimeException {
		Map<String, S3ObjectSummary> objectMap = objectList.stream().parallel().collect(
				Collectors.toMap(S3ObjectSummary::getKey, Function.identity()));
		return objectMap;
	}
	
	private S3SyncObject getDistinctObjects(BlobstoreS3Api s3Api) {
		List<S3ObjectSummary> syncList = this.listObjects();
		
		S3SyncObject syncObject = new S3SyncObject();
		ForkJoinPool pool = new ForkJoinPool(50);

		List<S3ObjectSummary> objectList = s3Api.listObjects();
		syncObject.setBlobTotal(objectList.size());
		Future<?> blobSizeTask = pool.submit(()-> {
			long blobSize = objectList.stream().parallel().mapToLong(S3ObjectSummary::getSize).sum();
			syncObject.setBlobSize(blobSize);
		});
		
		Map<String, S3ObjectSummary> syncObjectMap = this.getS3ObjectSummaryMap(syncList);
		syncObject.setS3Objects(syncObjectMap);
		
		Predicate<S3ObjectSummary> isCopyObject = s3Obj -> compare(s3Obj, syncObjectMap.get(s3Obj.getKey()));
		
		Future<?> copyListTask = pool.submit(()->{
			List<CopyS3Object> copyList = objectList.stream().parallel().filter(isCopyObject).map(s3Obj -> createCopyS3Object(s3Obj, syncObjectMap.get(s3Obj.getKey()))).collect(Collectors.toList());
			syncObject.setCopyList(copyList);
		});

		try {
			blobSizeTask.get();
			copyListTask.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.log(Level.SEVERE, "ForkJoinPool failed: ", e);
			Thread.currentThread().interrupt();
		}
		
		return syncObject;
	}
	
	private boolean compare(S3ObjectSummary copyObject, S3ObjectSummary s3Object) {
		if (s3Object == null) { 
			// 1. If not found at backup bucket
			return true;
		} else {
			// 2. If source bucket key modified recently
			return (copyObject.getLastModified().compareTo(s3Object.getLastModified()) > 0);
		}
	}
	
	private CopyS3Object createCopyS3Object(S3ObjectSummary copyObject, S3ObjectSummary s3Object) {
		CopyS3Object object = new CopyS3Object();
		object.setKey(copyObject.getKey());
		object.setLastModified(copyObject.getLastModified());
		object.setFileSize(copyObject.getSize());
		object.setBucket(copyObject.getBucketName());
		object.setOwner(copyObject.getOwner() != null ? copyObject.getOwner().getDisplayName() : "");
		
		if (s3Object == null) {
			object.setType(Type.NEW);
		} else {
			if (copyObject.getLastModified().compareTo(s3Object.getLastModified()) > 0) {
				object.setType(Type.MODIFIED);
			}
		}
		object.setCopied(false);
		return object;
	}
	
	private CopyS3Object copyS3Object(CopyS3Object copyObject, BlobstoreS3Api s3Api, S3SyncObject syncObject, AtomicInteger iteration) {
		final File file = new File(copyObject.getKey());
		file.deleteOnExit();
		try {
			ForkJoinPool pool = new ForkJoinPool(2);
			pool.submit(() -> s3Api.getObject(copyObject.getKey(), file)).get();
			pool.submit(() -> this.copyObject(copyObject.getKey(), file)).get();
			
			copyObject.setCopied(true);
			S3ObjectSummary s3Obj = syncObject.getS3Objects().get(copyObject.getKey());
			if (s3Obj != null && Type.MODIFIED.equals(copyObject.getType())) {
				s3Obj.setSize(copyObject.getFileSize());
			}
			logger.log(Level.INFO, "[{0}/{1}] - Copied ({2}) object key: {3} to bucket: {4}", new Object[] {
					iteration.getAndIncrement(), syncObject.getCopyList().size(), Utils.toHumanReadable(copyObject.getFileSize()),copyObject.getKey(), this.getS3Bucket()});
		} catch (Exception e) {
			copyObject.setCopied(false);
			logger.log(Level.SEVERE, "Failed to copy S3Object to bucket: " + this.getS3Bucket(), e);
		} finally {
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not delete downloaded file: " + file.getName());
			}
		}
		
		return copyObject;
	}
}
