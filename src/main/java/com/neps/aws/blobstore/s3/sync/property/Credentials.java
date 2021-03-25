package com.neps.aws.blobstore.s3.sync.property;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Credentials {
	private String accessKey;
	private String secretKey;
	private String bucket;
	private String url;
	private boolean enableSSE;
}
