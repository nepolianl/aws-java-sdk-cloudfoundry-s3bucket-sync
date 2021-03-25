package com.neps.aws.blobstore.s3.sync.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Sync;
import com.neps.aws.blobstore.s3.sync.service.BlobstoreSyncService;

@Component
public class BlobstoreSyncScheduler {
	private static final Logger logger = Logger.getLogger(BlobstoreSyncScheduler.class.getName());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    @Autowired
    private BlobstoreSyncService syncService;
    
    @Autowired
    private BlobstoreS3Sync s3Sync;
	
	@Scheduled(cron = "${blobstore.sync.job.weekly.cron}")
	public void syncBlobstore() {
		logger.log(Level.INFO, "The sync time is -> {0} ", this.dateFormat.format(new Date()));
		this.syncService.sync(this.s3Sync);
	}
}
