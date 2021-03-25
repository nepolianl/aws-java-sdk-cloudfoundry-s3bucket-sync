package com.neps.aws.blobstore.s3.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.cloudfoundry.CloudFoundryConnector;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class AWSS3SyncApplication {

	public static void main(String[] args) {
		if (new CloudFoundryConnector().isInMatchingCloud()) {
            		System.setProperty("spring.profiles.active", "cloud");
        	}
		SpringApplication.run(AWSS3SyncApplication.class, args);
	}

}
