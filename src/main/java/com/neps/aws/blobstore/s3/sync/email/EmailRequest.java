package com.neps.aws.blobstore.s3.sync.email;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {	
	String from;
	String to;
	String cc;
	String bcc;
	String subject;
	String body;
	Map<String, String> attachments;
}