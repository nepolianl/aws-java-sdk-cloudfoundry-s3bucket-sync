package com.neps.aws.blobstore.s3.sync.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Api;
import com.neps.aws.blobstore.s3.sync.client.api.BlobstoreS3Sync;
import com.neps.aws.blobstore.s3.sync.email.EmailRequest;
import com.neps.aws.blobstore.s3.sync.exception.SyncException;
import com.neps.aws.blobstore.s3.sync.model.CopyS3Object;
import com.neps.aws.blobstore.s3.sync.model.S3SyncObject;
import com.neps.aws.blobstore.s3.sync.model.CopyS3Object.Type;
import com.neps.aws.blobstore.s3.sync.property.MailProperties;
import com.neps.aws.blobstore.s3.sync.report.WorkbookHelper;
import com.neps.aws.blobstore.s3.sync.service.BlobstoreSyncService;
import com.neps.aws.blobstore.s3.sync.service.EmailService;
import com.neps.aws.blobstore.s3.sync.util.Utils;

@Service
public class BlobstoreSyncServiceImpl implements BlobstoreSyncService {
	private static final Logger logger = Logger.getLogger(BlobstoreSyncServiceImpl.class.getName());
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	@Autowired
	private BlobstoreS3Api s3Api;
	
	@Autowired
	private WorkbookHelper helper;
	
	@Autowired
	private EmailService email;
	
	@Autowired
	private TemplateEngine template;
	
	@Autowired
	private MailProperties mailPropertis;
	
	@Override
	public void sync(BlobstoreS3Sync s3Sync) {
		try {
			logger.log(Level.INFO, "Initiated blobstore s3 sync operation");
			S3SyncObject syncObject = s3Sync.sync(this.s3Api);
			logger.log(Level.INFO, "Completed blobstore s3 sync operation");
			
			this.notify(syncObject);
		} catch (SyncException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void notify(S3SyncObject syncObject) {
		File attachment = null;
		try {
			logger.log(Level.INFO, "Initiated preparing attachment");
			attachment = this.helper.getAttachment(syncObject.getCopyList());
			logger.log(Level.INFO, "Completed preparing attachment");
			
			EmailRequest request = new EmailRequest();
			request.setFrom(this.mailPropertis.getFrom());
			request.setTo(this.mailPropertis.getTo());
			request.setCc(this.mailPropertis.getCc());
			if (this.mailPropertis.getBcc() != null && !this.mailPropertis.getBcc().isEmpty()) {
				request.setBcc(this.mailPropertis.getBcc());
			}
			
			request.setSubject(this.mailPropertis.getSubject());
			request.setBody(this.getEmailBody(this.prepareVariables(syncObject)));
			
			String encodedString = this.getBase64Encoder(attachment);
			if (encodedString != null) {
				Map<String, String> attachments = new HashMap<>();
				attachments.put(attachment.getName(), encodedString);
				request.setAttachments(attachments);
			}
			
			logger.log(Level.INFO, "Sending email notification");
			this.email.sendEmail(request);
		} catch (SyncException | IOException e) {
			logger.log(Level.SEVERE, "Could not send mail with attachment: ", e);
		} finally {
			if (attachment != null) {
				try {
					Files.delete(attachment.toPath());
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Could not delete attachment file: " + attachment.getName());
				}
			}
		}
	}
	
	private Map<String, Object> prepareVariables(S3SyncObject syncObject) {
		List<CopyS3Object> copyList = syncObject.getCopyList();
		long createdTotal = copyList.stream().parallel().filter(CopyS3Object::isCopied).filter(obj -> Type.NEW.equals(obj.getType())).count();
		long createdSize = copyList.stream().parallel().filter(CopyS3Object::isCopied).filter(obj -> Type.NEW.equals(obj.getType())).mapToLong(CopyS3Object::getFileSize).sum();
		long modifiedTotal = copyList.stream().parallel().filter(CopyS3Object::isCopied).filter(obj -> Type.MODIFIED.equals(obj.getType())).count();
		long modifiedSize = copyList.stream().parallel().filter(CopyS3Object::isCopied).filter(obj -> Type.MODIFIED.equals(obj.getType())).mapToLong(CopyS3Object::getFileSize).sum();
		long syncTotal = (createdTotal + modifiedTotal);
		long syncSize = (createdSize + modifiedSize);
		
		long s3Total = syncObject.getS3Objects() != null ? syncObject.getS3Objects().size() : 0;
		long s3Size = syncObject.getS3Objects() == null ? 0 : syncObject.getS3Objects().entrySet().stream().parallel().mapToLong(e -> e.getValue().getSize()).sum();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("syncDate", dateFormat.format(new Date()));
		variables.put("createdTotal", createdTotal);
		variables.put("createdSize", Utils.toHumanReadable(createdSize));
		variables.put("modifiedTotal", modifiedTotal);
		variables.put("modifiedSize", Utils.toHumanReadable(modifiedSize));
		variables.put("syncTotal", syncTotal);
		variables.put("syncSize", Utils.toHumanReadable(syncSize));
		variables.put("blobTotal", syncObject.getBlobTotal());
		variables.put("blobSize", Utils.toHumanReadable(syncObject.getBlobSize()));
		variables.put("s3Total", (s3Total + createdTotal));
		variables.put("s3Size", Utils.toHumanReadable(s3Size + createdSize));
		
		return variables;
	}
	
	private String getEmailBody(Map<String, Object> variables) {
		Context context = new Context();
		context.setVariables(variables);
		return this.template.process(this.mailPropertis.getTemplate(), context);
	}
	
	private String getBase64Encoder(File file) {
		String base64String = null;
		try {
			byte[] contents = Files.readAllBytes(file.toPath());
			base64String = Base64.getEncoder().encodeToString(contents);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not read file as byte array base64 encoder: ", e);
		}
		return base64String;
	}
	
}
