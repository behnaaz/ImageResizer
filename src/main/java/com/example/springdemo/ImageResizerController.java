package com.example.springdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;

@RestController
@RequestMapping(value = "/storage/") // , method = {RequestMethod.GET, RequestMethod.POST})
public class ImageResizerController {
	private static final Logger logger = LogManager.getLogger(ImageResizerController.class.getName());
	private AmazonClient amazonClient;

	@Autowired
	ImageResizerController(AmazonClient amazonClient) {
		this.amazonClient = amazonClient;
	}

	@PostMapping("/uploadFile")
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public String uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
		return this.amazonClient.uploadFile(file);
	}

	@RequestMapping(value = "/image/{param1}/{param2}/{param3}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] getImage(@PathVariable(value = "param1") String param1, @PathVariable(value = "param2") String param2,
			@PathVariable(value = "param3") String param3) throws IOException {
		byte[] processedImage = null;
		try {
			processedImage = amazonClient.getProcessedImage(param1, 1, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (processedImage != null) {
			logger.info("Processed image found for " + param1);
			return processedImage;
		}

		byte[] originalImage = amazonClient.getOriginalImage(param1);
		if (originalImage == null) {
			logger.info("Original image not found for " + param1);
			return null;
		}

		// if null????
		processedImage = new ImageResizerUtil().resize(originalImage, 1, 1, "png");// TODO
		// TODO check null exception
		amazonClient.uploadFile(processedImage, param1);
		logger.info("Processing done for image " + param1);
		return processedImage;
	}

	@DeleteMapping("/deleteFile")
	@RequestMapping(value = "/deleteFile", method = RequestMethod.DELETE)
	public String deleteFile(@RequestPart(value = "url") String fileUrl) {
		return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
	}
}