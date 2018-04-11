package com.example.springdemo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

/**
 * This class provides image resize functionality
 * @author B
 *
 */
public class ImageResizerUtil {
	private static final Logger logger = LogManager.getLogger(ImageResizerUtil.class.getName());
	private final List<String> supportedImageTypes = Arrays.asList("png");
	
	/**
	 * Checks if the image type is supported by the application
	 * @param type 
	 * 			Image type
	 * @return
	 * 			true/false
	 */
	public boolean isImageTypeSupported(String imageType) {
		return supportedImageTypes.contains(imageType);
	}
	
	/**
	 * 
	 * @param originalImage
	 * 			image to be resized as a byte array
	 * @param width
	 * 			width of the resized image
	 * @param height
	 *			height of the resized image
	 * @param imageType
	 * @return
	 * @throws ImageResizingException
	 */
	public byte[] resize(byte[] originalImage, ImageModificationType modificationType, String imageType) throws ImageResizingException {
		if (modificationType == null) {
			logger.warn("No valid image modification type is provided.");
			throw new ImageResizingException("No valid image modification type is provided.");
		}
		return resize(originalImage, modificationType.getWidth(), modificationType.getHeigth(), imageType);
	}
	
	private byte[] resize(byte[] originalImage, int width, int height, String imageType) throws ImageResizingException {
		String notNullImageType = (Strings.isBlank(imageType)) ? "" : imageType;
		if (!isImageTypeSupported(notNullImageType)) {
			logger.warn(notNullImageType + " image type is not supported");
			throw new ImageResizingException(notNullImageType + " image type is not supported");
		}
		
		BufferedImage originalBufferedImage;
		try {
			originalBufferedImage = ImageIO.read(new ByteArrayInputStream(originalImage));
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
			throw new ImageResizingException(e);
		}
											
		if (originalBufferedImage == null) {
			logger.warn("Failed to retrieve the image.");
			throw new ImageResizingException("Failed to retrieve the image.");
		}
		
		Image tmp = originalBufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] imageInByte = null;
		try {
			ImageIO.write(resizedImage, imageType, baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
			throw new ImageResizingException(e);
		}
		return imageInByte;
	}
}
