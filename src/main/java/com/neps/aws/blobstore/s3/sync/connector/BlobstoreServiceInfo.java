package com.neps.aws.blobstore.s3.sync.connector;

import org.springframework.cloud.service.BaseServiceInfo;

public class BlobstoreServiceInfo extends BaseServiceInfo {

    private String objectStoreAccessKey;
    private String objectStoreSecretKey;
    private String bucket;
    private String url;
    private boolean enableSSE;

    public BlobstoreServiceInfo(String id, String objectStoreAccessKey, String objectStoreSecretKey, String bucket) {
        super(id);
        this.objectStoreAccessKey = objectStoreAccessKey;
        this.objectStoreSecretKey = objectStoreSecretKey;
        this.bucket = bucket;
    }

    public BlobstoreServiceInfo(String id, String objectStoreAccessKey, String objectStoreSecretKey,
                                String bucket, String url) {
        super(id);
        this.objectStoreAccessKey = objectStoreAccessKey;
        this.objectStoreSecretKey = objectStoreSecretKey;
        this.bucket = bucket;
        this.url = url;
    }

    public BlobstoreServiceInfo(String id, String objectStoreAccessKey, String objectStoreSecretKey,
                                String bucket, String url, boolean enableSSE) {
        super(id);
        this.objectStoreAccessKey = objectStoreAccessKey;
        this.objectStoreSecretKey = objectStoreSecretKey;
        this.bucket = bucket;
        this.url = url;
        this.enableSSE = enableSSE;
    }

    @ServiceProperty
    public String getObjectStoreAccessKey() {
        return objectStoreAccessKey;
    }

    @ServiceProperty
    public String getObjectStoreSecretKey() {
        return objectStoreSecretKey;
    }

    @ServiceProperty
    public String getBucket() {
        return bucket;
    }

    @ServiceProperty
    public String getUrl() {
        return url;
    }

    @ServiceProperty
    public boolean getEnableSSE() {
        return enableSSE;
    }

    @Override
    public String toString() {
        return "BlobstoreServiceInfo [objectStoreAccessKey="
                + objectStoreAccessKey + ", bucket=" + bucket + ", url=" + url + " enableSSE=" + enableSSE
                + "]";
    }
}
