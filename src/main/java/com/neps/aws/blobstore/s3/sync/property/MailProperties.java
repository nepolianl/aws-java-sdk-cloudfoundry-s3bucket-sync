package com.neps.aws.blobstore.s3.sync.property;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailProperties {
	private String akanaEmailUrl;
	private String tokenUrl;
	private String clientId;
	private String clientSecret;
	private List<String> tokenScope;
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String template;
}
