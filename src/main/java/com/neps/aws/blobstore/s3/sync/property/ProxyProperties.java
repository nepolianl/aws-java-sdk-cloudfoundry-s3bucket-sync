package com.neps.aws.blobstore.s3.sync.property;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyProperties {
	private String host;
	private int port;
	private boolean enable;
}
