package com.neps.aws.blobstore.s3.sync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Api;
import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Sync;
import com.neps.aws.blobstore.s3.sync.client.impl.BlobstoreS3Client;
import com.neps.aws.blobstore.s3.sync.property.BlobstoreProperties;
import com.neps.aws.blobstore.s3.sync.property.MailProperties;
import com.neps.aws.blobstore.s3.sync.property.ProxyProperties;

@Configuration
@Profile(value = {"default"})
public class SyncConfig {
	
	@Autowired
	private BlobstoreProperties blobProperties;
	
	@Autowired
	private ProxyProperties proxyProperties;
	
	@Autowired
	private MailProperties mailProperties;
	
	@Bean
	public BlobstoreS3Sync s3Sync() {
		return new BlobstoreS3Client(this.blobProperties.getBackup(), this.proxyProperties);
	}
	
	@Bean
	public BlobstoreS3Api s3Api() {
		return new BlobstoreS3Client(this.blobProperties.getSync(), this.proxyProperties);
	}
	
	@Bean
	public OAuth2RestTemplate getOAuth2RestTemplate() {
		ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
		details.setClientId(this.mailProperties.getClientId());
		details.setClientSecret(this.mailProperties.getClientSecret());
		details.setAccessTokenUri(this.mailProperties.getTokenUrl());
		details.setScope(this.mailProperties.getTokenScope());

		return new OAuth2RestTemplate(details);
	}
}
