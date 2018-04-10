package com.example.springdemo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageResizerUtil {
	public static byte[] resize(byte[] originalImage, int width, int height, String imageType) throws IOException {
		// TODO Auto-generated method stub
		BufferedImage originalBufferedImage = ImageIO.read(new ByteArrayInputStream(originalImage));// TODO close the
																									// stream
		if (originalBufferedImage == null) {
			// log error or throw
			return null;
		}
		Image tmp = originalBufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();// close resources TODO
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(resizedImage, imageType, baos);
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		return imageInByte;
	}

}
