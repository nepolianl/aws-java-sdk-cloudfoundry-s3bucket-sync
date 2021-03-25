package com.neps.aws.blobstore.s3.sync.exception;

@SuppressWarnings("serial")
public class SyncRuntimeException extends RuntimeException {

	public SyncRuntimeException(Throwable e) {
		super(e);
	}
	
	public SyncRuntimeException(String error) {
		super(error);
	}
	
	public SyncRuntimeException(String error, Throwable e) {
		super(error, e);
	}
}
