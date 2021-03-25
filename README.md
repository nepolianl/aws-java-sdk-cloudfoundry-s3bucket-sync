## AWS S3 Sync Scheduler Application

This is a scheduler application for integrating with Predix Blobstore service to backup objects into another Predix Blobstore service. This is built on Java 1.8 and Spring (Spring Boot, Spring MVC) technology and uses an AWS S3 client (S3 APIs) to connect to the store. The application will perform the following tasks:

- Run a scheduler
- List all objects in the store
- Download an object
- Upload an object
- Copy an object between stores
- Email copy results as attachment

**Note**: Recommended Java 1.8, Spring-2.0.0.RELEASE and aws-java-sdk-1.11.327

## Getting started:

-	Login to Predix
-	Create an instance of the predix-blobstore service for backup, for example: <p>
  `cf create-service predix-blobstore <plan> <my_blobstore_instance>`
-	Clone this project.
  `git clone <>`

## Build and Configuration
cd `<BASEDIR>/aws-java-sdk-cloudfoundry-s3bucket-sync` <p>
Run `mvn clean install`

In the manifest.yml file, enter the unique name of blobstore application, update the BROKER_SERVICE_NAME value with source Blobstore instance name, S3BACKUP_SERVICE_NAME value with created backup service instance name, and follow the rest of configurations.

**Application Properties Configuration**:

Update the application.properties with cron expression and other email configurations

## Manifest Configuration & Deploy
```
applications:
  - name: aws-blobstore-s3sync-scheduler
    memory: 2G
    disk_quota: 2G
    path: target/aws-java-sdk-cloudfoundry-s3bucket-sync.jar
    timeout: 180
    buildpack: java_buildpack
    env:
     BLOBSTORE_SERVICE_NAME: <source_blobstore_instance>
     S3BACKUP_SERVICE_NAME: <backup_blobstore_instance>
     ENABLE_SERVER_SIDE_ENCRYPTION: false
    services:
     - <backup_blobstore_instance>
     - <source_blobstore_instance>
```

**Deploy**:

From the project's home directory, push the application: `cf push` <p>
View the environment variables for your application: `cf env <application_name>`

*Note*: If the application deployed successfully then s3sync scheduler application will be executed based on the configurations.
