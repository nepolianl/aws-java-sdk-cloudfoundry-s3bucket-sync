package com.neps.aws.blobstore.s3.sync.service;

import com.neps.aws.blobstore.s3.sync.email.EmailRequest;
import com.neps.aws.blobstore.s3.sync.exception.SyncException;

public interface EmailService {
	public String sendEmail(EmailRequest request) throws SyncException;
}
