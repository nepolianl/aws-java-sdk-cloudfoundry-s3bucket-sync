package com.neps.aws.blobstore.s3.sync.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;

import com.neps.aws.blobstore.s3.sync.client.impl.BlobstoreS3Client;
import com.neps.aws.blobstore.s3.sync.property.Credentials;
import com.neps.aws.blobstore.s3.sync.property.ProxyProperties;

public class S3BackupServiceConnectorCreator extends AbstractServiceConnectorCreator<BlobstoreS3Client, BlobstoreServiceInfo> {
    private Log log = LogFactory.getLog(S3BackupServiceConnectorCreator.class);

    @Override
    public BlobstoreS3Client create(BlobstoreServiceInfo serviceInfo, ServiceConnectorConfig serviceConnectorConfig) {
        log.info("create() invoked with serviceInfo? = " + (serviceInfo == null));
        return new BlobstoreS3Client(this.getCredentials(serviceInfo), this.getProxy());
    }
    
    private Credentials getCredentials(BlobstoreServiceInfo serviceInfo) {
    	Credentials cred = new Credentials();
    	cred.setAccessKey(serviceInfo.getObjectStoreAccessKey());
    	cred.setSecretKey(serviceInfo.getObjectStoreSecretKey());
    	cred.setBucket(serviceInfo.getBucket());
    	cred.setUrl(serviceInfo.getUrl());
    	cred.setEnableSSE(false);
    	return cred;
    }
    
    private ProxyProperties getProxy() {
    	ProxyProperties proxy = new ProxyProperties();
    	proxy.setEnable(true);
    	return proxy;
    }

}
