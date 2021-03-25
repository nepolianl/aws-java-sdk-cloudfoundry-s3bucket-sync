package com.neps.aws.blobstore.s3.sync.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import com.neps.aws.blobstore.s3.sync.util.Constants;

import java.util.Map;
import java.util.StringTokenizer;

public class S3BackupServiceInfoCreator extends CloudFoundryServiceInfoCreator<BlobstoreServiceInfo> {

    private static String blobstoreServiceName = System.getenv(Constants.S3BACKUP_SERVICE_NAME);
    private Log log = LogFactory.getLog(S3BackupServiceInfoCreator.class);

    public S3BackupServiceInfoCreator() {
        super(new Tags(blobstoreServiceName));
    }

    @Override
    public BlobstoreServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> credentials = (Map<String, Object>) serviceData.get("credentials");

        String id = (String) serviceData.get("name");
        String objectStoreAccessKey = (String) credentials.get("access_key_id");
        String objectStoreSecretKey = (String) credentials.get("secret_access_key");
        String endPointWithBucket = (String) credentials.get("url");
        String host = (String) credentials.get("host");
        String bucket = (String) credentials.get("bucket_name");

        // Extract protocol for endpoint without bucket name
        StringTokenizer st = new StringTokenizer(endPointWithBucket, "://");
        String protocol = "";
        if (st.hasMoreTokens()) {
            protocol = st.nextToken();
        }

        String url = "";
        url = protocol + "://" + host;

        BlobstoreServiceInfo objectStoreInfo = new BlobstoreServiceInfo(id, objectStoreAccessKey, objectStoreSecretKey, bucket, url);
        log.info("createServiceInfo()- "+ blobstoreServiceName +": " + objectStoreInfo);

        return objectStoreInfo;
    }

    @Override
    public boolean accept(Map<String, Object> serviceData) {
        if (log.isDebugEnabled())
            log.debug("accept(): invoked with service data? = " + (serviceData == null));
        String name = (String) serviceData.get("name");
        return name.startsWith(blobstoreServiceName);
    }

}
