package com.neps.aws.blobstore.s3.sync.exception;

@SuppressWarnings("serial")
public class SyncException extends Exception {

	public SyncException() {
	}
	
	public SyncException(Throwable e) {
		super(e);
	}
	
	public SyncException(String error) {
		super(error);
	}
	
	public SyncException(String error, Throwable e) {
		super(error, e);
	}
}
