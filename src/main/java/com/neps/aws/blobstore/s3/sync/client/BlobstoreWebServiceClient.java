package com.neps.aws.blobstore.s3.sync.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Api;
import com.neps.aws.blobstore.s3.sync.exception.SyncRuntimeException;
import com.neps.aws.blobstore.s3.sync.property.Credentials;
import com.neps.aws.blobstore.s3.sync.property.ProxyProperties;
import com.neps.aws.blobstore.s3.sync.util.Constants;
import com.neps.aws.blobstore.s3.sync.util.Utils;

public abstract class BlobstoreWebServiceClient implements BlobstoreS3Api {
	private static final Logger logger = Logger.getLogger(BlobstoreWebServiceClient.class.getName());
	
	private AmazonS3 client;
	private String bucket;
	
	public abstract Map<String, S3ObjectSummary> getS3ObjectSummaryMap(List<S3ObjectSummary> objectList);
	
	public BlobstoreWebServiceClient(Credentials credentials, ProxyProperties proxy) {
		if (credentials == null || credentials.getAccessKey() == null || credentials.getAccessKey().isEmpty() || credentials.getSecretKey() == null 
				|| credentials.getSecretKey().isEmpty() || credentials.getBucket() == null || credentials.getBucket().isEmpty()) {
			logger.log(Level.SEVERE, "Could not create s3 client, invalid or empty credentials {0}", (credentials == null ? "" : credentials.toString()));
			throw new SyncRuntimeException("Could not create s3 client, invalid or empty credentials {0}" + (credentials == null ? "" : credentials.toString())) ;
		}
		
		AWSCredentials awsCredentials = new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
        
        ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxConnections(100)
        		.withConnectionTimeout(120*1000).withMaxErrorRetry(15);
        if (proxy.isEnable()) {
        	clientConfiguration.setProxyHost(proxy.getHost());
            clientConfiguration.setProxyPort(proxy.getPort());
        }
        clientConfiguration.setProtocol(Protocol.HTTP);
        
        this.client = AmazonS3ClientBuilder.standard().withRegion(Constants.DEFAULT_REGION).withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
        		.withClientConfiguration(clientConfiguration).build();
        this.bucket = credentials.getBucket();
	}
	
	@Override
	public AmazonS3 getS3Client() {
		return this.client;
	}
	
	@Override
	public String getS3Bucket() {
		return this.bucket;
	}
	
	@Override
	public List<S3ObjectSummary> listObjects() {
        List<S3ObjectSummary> all = new ArrayList<>();
        
        ListObjectsV2Result result;
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(this.bucket);
        do {
            result = this.client.listObjectsV2(request);
            all.addAll(result.getObjectSummaries());
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        
		logger.log(Level.FINE, "Total objects: {0} in bucket: {1}", new Object[] {all.size(), this.bucket});
		return all;
	}
	
	@Override
	public void copyObject(String key, File file) {
		TransferManager transfer = TransferManagerBuilder.standard().withS3Client(this.client)
				.withMultipartUploadThreshold((long)(5 * 1024 * 1025))
				.withDisableParallelDownloads(true)
				.withExecutorFactory(()->Executors.newFixedThreadPool(60)).build();
		
		PutObjectRequest request = new PutObjectRequest(this.bucket, key, file);
		Upload upload = transfer.upload(request);
		try {
			ProgressListener progressListener = progressEvent -> {
		        if (upload.isDone()) {
		        	logger.log(Level.FINE, "Upload complte for key: {0}--{1}/{2}({3})--{4}%", new Object[] { 
		        			key, upload.getProgress().getBytesTransferred(), 
		        			upload.getProgress().getTotalBytesToTransfer(),
		        			Utils.toHumanReadable(upload.getProgress().getBytesTransferred()),
		        			upload.getProgress().getPercentTransferred()});
		        }
			};
			upload.addProgressListener(progressListener);
			upload.waitForCompletion();
		} catch (AmazonClientException e) {
			upload.abort();
			logger.log(Level.SEVERE, String.format("Could not upload object key: %s to bucket: %s", key, this.bucket), e);
			throw new SyncRuntimeException(String.format("Could not upload object key: %s to bucket: %s", key, this.bucket), e);
		} catch (InterruptedException e) {
			upload.abort();
			logger.log(Level.SEVERE, String.format("S3 interrupted, Could not upload object key: %s to bucket: %s", key, this.bucket), e);
			Thread.currentThread().interrupt();
			throw new SyncRuntimeException(String.format("S3 interrupted, Could not upload object key: %s to bucket: %s", key, this.bucket), e);
		} finally {
			transfer.shutdownNow(false);
		}
	}
	
	@Override
	public File getObject(String key, File file) {
		TransferManager transfer = TransferManagerBuilder.standard().withS3Client(this.client)
				.withMultipartUploadThreshold((long)(5 * 1024 * 1025))
				.withDisableParallelDownloads(true)
				.withExecutorFactory(()->Executors.newFixedThreadPool(60)).build();
		
		GetObjectRequest request = new GetObjectRequest(this.bucket, key);
		Download download = transfer.download(request, file);
		try {
			ProgressListener progressListener = progressEvent -> {
				if (download.isDone()) {
					logger.log(Level.FINE, "Download complte for key: {0}--{1}/{2}({3})--{4}%", new Object[] { 
		        			key, download.getProgress().getBytesTransferred(), 
		        			download.getProgress().getTotalBytesToTransfer(),
		        			Utils.toHumanReadable(download.getProgress().getBytesTransferred()),
		        			download.getProgress().getPercentTransferred()});
		        }
			};
			download.addProgressListener(progressListener);
			download.waitForCompletion();
		} catch (AmazonClientException e) {
			try {
				download.abort();
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Could not execute download.abort()", e1);
			}
			logger.log(Level.SEVERE, String.format("Could not download object key: %s to bucket: %s", key, this.bucket), e);
			throw new SyncRuntimeException(String.format("Could not download object key: %s to bucket: %s", key, this.bucket), e);
		} catch (InterruptedException e) {
			try {
				download.abort();
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Could not execute download.abort()", e1);
			}
			logger.log(Level.SEVERE, String.format("S3 interrupted, Could not download object key: %s to bucket: %s", key, this.bucket), e);
			Thread.currentThread().interrupt();
			throw new SyncRuntimeException(String.format("S3 interrupted, Could not download object key: %s to bucket: %s", key, this.bucket), e);
		} finally {
			transfer.shutdownNow(false);
		}
		
		return file;
	}
	
}
