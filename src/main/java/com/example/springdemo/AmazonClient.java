package com.example.springdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class AmazonClient {
	private static final Logger logger = LogManager.getLogger(ImageResizerController.class.getName());
	private AmazonS3 s3client;

	@Value("${amazonProperties.endpointUrl}")
	private String endpointUrl;
	@Value("${amazonProperties.bucketName}")
	private String bucketName;
	@Value("${amazonProperties.accessKey}")
	private String accessKey;
	@Value("${amazonProperties.secretKey}")
	private String secretKey;

	@SuppressWarnings("deprecation")
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	/**
	 * Returns the resized image according to the given modificationType
	 * @param name
	 * @param modificationType
	 * @return
	 * @throws ImageResizingException
	 */
	public byte[] getProcessedImage(String name, ImageModificationType modificationType) throws ImageResizingException {
		return getImage(modificationType.getName() + name);
	}

	/**
	 * Returns the original name with the given name
	 * @param name
	 * @return
	 * @throws ImageResizingException
	 */
	public byte[] getOriginalImage(String name) throws ImageResizingException {
		return getImage(name);
	}
	
	/**
	 * Uploads the given image with the given name to the S3 bucket
	 * @param multipartFile
	 * @param imageName
	 * @return
	 * 			the url of the uploaded image
	 * @throws ImageResizingException
	 */
	public String uploadFile(MultipartFile multipartFile, String imageName) throws ImageResizingException {
		File file = convertMultiPartToFile(multipartFile);
		return uploadFile(file, imageName);
	}
	
	/**
	 * Uploads the given image content with the given name to the S3 bucket
	 * @param content
	 * @param fileName
	 * @return
	 * 			the url of the uploaded image
	 * @throws ImageResizingException
	 */
	public String uploadFile(byte[] content, String fileName) throws ImageResizingException {
		File tempFile = new File(fileName);//TODO delete the temporary file, release resources
		try {
			FileUtils.writeByteArrayToFile(tempFile, content);
		} catch (IOException e) {
			logger.warn(e.getMessage());
			throw new ImageResizingException(e);
		}
		String url = uploadFile(tempFile, fileName);
		try {
			FileUtils.forceDelete(tempFile);
		} catch (IOException e) {
			logger.warn(e.getMessage());
			throw new ImageResizingException(e);
		}
		return url;
	}
	
	/**
	 * Upload the given file with the given name and returns it url
	 * @param file
	 * @param fileName
	 * @return
	 * @throws ImageResizingException
	 */
	private String uploadFile(File file, String fileName) throws ImageResizingException {
		String fileUrl = "";
		try {
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ImageResizingException(e);
		}
		return fileUrl;
	}

	private File convertMultiPartToFile(MultipartFile file) throws ImageResizingException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (IOException e) {
			logger.warn(e.getMessage());
			throw new ImageResizingException(e);
		}
		return convFile;
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file)
					.withCannedAcl(CannedAccessControlList.PublicRead));
	}

	private byte[] getImage(String name) throws ImageResizingException {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(name);
		ObjectListing objectListing;
		objectListing = s3client.listObjects(listObjectsRequest);
		if (objectListing == null || objectListing.getObjectSummaries() == null) {
			logger.warn("Image not found:" + name);//TODO better message
			throw new ImageResizingException("Image not found:" + name);
		}
		
		if (objectListing.getObjectSummaries().size() != 1) {
			logger.warn("Not a unique Image found. Number of found images:" + objectListing.getObjectSummaries().size());
			throw new ImageResizingException("Not a unique Image found. Number of found images:" + objectListing.getObjectSummaries().size());
		}
		S3ObjectSummary summary = objectListing.getObjectSummaries().get(0);
		S3Object obj = s3client.getObject(bucketName, summary.getKey());
		if (obj.getObjectContent() == null) {
			logger.warn("Found image has no content");
			throw new ImageResizingException("Found image has no content");
		}
		try {
			return IOUtils.toByteArray(obj.getObjectContent());
		} catch (IOException e) {
			logger.warn(e.getMessage());
			throw new ImageResizingException(e);
		}
	}
}