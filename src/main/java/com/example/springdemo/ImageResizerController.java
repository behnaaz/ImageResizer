package com.example.springdemo;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@RestController
@RequestMapping(value = "/image/") 
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
//Create customized Exception class TODO
	//~/image/show/thumbnail/dept-blazer/?reference=%2F027%2F790%2F13_0277901000150001_pro_mod_frt_02_1108_1528_1
	//059540.jpg
	@RequestMapping(value = "/show/{modification_type}/{seo}/", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] getImage(@PathVariable(value = "modification_type") String modificationType, @PathVariable(value = "seo") String seo,
			@RequestPart(value = "reference") String reference) throws IOException {
		byte[] processedImage = null;
		try {
			processedImage = amazonClient.getProcessedImage(reference, ImageModificationType.get(modificationType));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (processedImage != null) {
			logger.info("Processed image found for " + reference);
			return processedImage;
		}

		byte[] originalImage = amazonClient.getOriginalImage(reference);
		if (originalImage == null) {
			logger.info("Original image not found for " + reference);
			return null;
		}
		logger.info("Original image found for " + reference);

		// if null????
		processedImage = new ImageResizerUtil().resize(originalImage, 1, 1, "png");// TODO
		// TODO check null exception
		amazonClient.uploadFile(processedImage, reference);
		logger.info("Processing done for image " + reference);
		return processedImage;
	}
//TODO delete
	@DeleteMapping("/deleteFile")
	@RequestMapping(value = "/deleteFile", method = RequestMethod.DELETE)
	public String deleteFile(@RequestPart(value = "url") String fileUrl) {
		return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
	}
}