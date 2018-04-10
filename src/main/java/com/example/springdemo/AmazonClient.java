package com.example.springdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class AmazonClient {

	private AmazonS3 s3client;

	@Value("${amazonProperties.endpointUrl}")
	private String endpointUrl;
	@Value("${amazonProperties.bucketName}")
	private String bucketName;
	@Value("${amazonProperties.accessKey}")
	private String accessKey;
	@Value("${amazonProperties.secretKey}")
	private String secretKey;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	public String deleteFileFromS3Bucket(String fileUrl) {
		String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
		return "Successfully deleted";
	}

	public String uploadFile(MultipartFile multipartFile) throws IOException {
		File file = convertMultiPartToFile(multipartFile);
		String fileName = generateFileName(multipartFile);
		return uploadFile(file, fileName);
	}

	public String uploadFile(byte[] content, String fileName) throws IOException {
		File tempFile = new File(fileName);//TODO delete
		FileUtils.writeByteArrayToFile(tempFile, content);
		return uploadFile(tempFile, fileName);//TODO
	}
	
	public String uploadFile(File file, String fileName) {
		String fileUrl = "";
		try {
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileUrl;
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	private byte[] getImage(String name) throws IOException {
		// TODO Auto-generated method stub
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(name);
		ObjectListing objectListing;
		objectListing = s3client.listObjects(listObjectsRequest);
		if (objectListing == null || objectListing.getObjectSummaries() == null) {
			// logger.
			return null;
		}
		/*
		 * SdkClientException If any errors are encountered in the client while making
		 * the request or handling the response.
		 * 
		 * @throws AmazonServiceException If any errors occurred in Amazon S3 while
		 * processing the request.
		 *
		 */
		if (objectListing.getObjectSummaries().size() != 1) {
			// ???
			return null;
		}
		S3ObjectSummary summary = objectListing.getObjectSummaries().get(0);
		// set key summary.getKey()
		// summary.getLastModified(
		S3Object obj = s3client.getObject(bucketName, summary.getKey());
		// ??writeToFile(obj.getObjectContent());
		// String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		if (obj.getObjectContent() == null) {
			return null;// ??
		}
		return IOUtils.toByteArray(obj.getObjectContent());
	}

	public byte[] getProcessedImage(String name, int width, int length) throws IOException {
		return getImage(name + "_" + width + "_" + length);
	}

	public byte[] getOriginalImage(String name) throws IOException {
		return getImage(name + "_original");
		// TODO Auto-generated method stub
	}
}
