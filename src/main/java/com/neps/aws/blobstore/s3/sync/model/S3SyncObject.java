package com.neps.aws.blobstore.s3.sync.model;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S3SyncObject {
	private int blobTotal;
	private long blobSize;
	private List<CopyS3Object> copyList;
	private Map<String, S3ObjectSummary> s3Objects;
}
