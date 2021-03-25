package com.neps.aws.blobstore.s3.sync.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neps.aws.blobstore.s3.sync.exception.SyncException;
import com.neps.aws.blobstore.s3.sync.property.MailProperties;
import com.neps.aws.blobstore.s3.sync.service.EmailService;

@Component
public class EmailApi implements EmailService {
	private static final Log logger = LogFactory.getLog(EmailApi.class);
	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private OAuth2RestTemplate restTemplate;
	
	@Autowired
	private MailProperties mailProperties;
	
	public String sendEmail(EmailRequest request) throws SyncException {
		final HttpEntity<Object> httpEntity = new HttpEntity<>(this.jsonBody(request), httpHeaders());
		String response = null;
		try {
			response = restTemplate.postForObject(this.mailProperties.getAkanaEmailUrl(), httpEntity, String.class);
			if (!"Success".equals(response)) {
				throw new SyncException("Could you send email");
			} else {
				logger.debug("Email sent: To=" + request.getTo()+", CC="+ request.getCc() +", Subject=" + request.getSubject());
			}
		} catch (Exception e) {
			logger.error("Could not complete akana email execute: ", e);
		}
		return response;
	}
	
	private HttpHeaders httpHeaders() {
		final String token = this.restTemplate.getAccessToken().getValue();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set(AUTHORIZATION, BEARER + token);
		return httpHeaders;
	}
	
	private String jsonBody(EmailRequest request) {
		String body = "";
		try {
			body = mapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			logger.error("Could not writeValueAsString for EmailRequest: ", e);
		}
		return body;
	}
	
}
