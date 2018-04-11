package com.example.springdemo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	public String uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestPart(value = "imageName") String imageName) throws ImageResizingException {
		return this.amazonClient.uploadFile(file, imageName);
	}

	//~/image/show/thumbnail/?reference=imagename.png
	@RequestMapping(value = "/show/{modification_type}/?reference={reference}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] getImage(@PathVariable(value = "modification_type", required=true) String modificationType,
			@RequestParam(value = "reference", required=true) String reference) throws ImageResizingException {
		//TODO Calculate the time
		logger.info("GetImage called with modificationType:" + modificationType + ", reference: " + reference);
		byte[] processedImage = null;
		processedImage = amazonClient.getProcessedImage(reference, ImageModificationType.get(modificationType));
		
		if (processedImage != null) {
			logger.info("Found resized image: " + reference);
			return processedImage;
		}

		byte[] originalImage;
		originalImage = amazonClient.getOriginalImage(reference);
		if (originalImage == null) {
			logger.info("Original image not found for " + reference);
			throw new ImageResizingException("Original image not found for " + reference);
		}
		logger.info("Found original image:" + reference);

		processedImage = new ImageResizerUtil().resize(originalImage, ImageModificationType.Thumbnail, "png");// TODO
		if (processedImage == null) {
			logger.info("Original image not found for " + reference);
			throw new ImageResizingException("Original image not found for " + reference);
		}
		
		amazonClient.uploadFile(processedImage, reference);
		logger.info("Resizing done for " + reference);
		return processedImage;
	}
}