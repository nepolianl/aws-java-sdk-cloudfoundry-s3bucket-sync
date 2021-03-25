package com.neps.aws.blobstore.s3.sync.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(value = {"default","cloud"})
public class ConfigProperties {
	
	@ConfigurationProperties(prefix="blobstore")
	@Bean
	public BlobstoreProperties blobstoreProperties() {
		return new BlobstoreProperties();
	}
	
	@ConfigurationProperties(prefix="mail")
	@Bean
	public MailProperties mailProperties() {
		return new MailProperties();
	}
	
	@ConfigurationProperties(prefix="proxy")
	@Bean
	public ProxyProperties proxyProperties() {
		return new ProxyProperties();
	}
}
