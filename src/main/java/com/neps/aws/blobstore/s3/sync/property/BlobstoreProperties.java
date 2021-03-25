package com.neps.aws.blobstore.s3.sync.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlobstoreProperties {
	private Credentials backup;
	private Credentials sync;
}
