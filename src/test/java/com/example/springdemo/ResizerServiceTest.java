package com.example.springdemo;

import static org.mockito.Mockito.*;

import java.util.Base64;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResizerServiceTest {
	byte[] smallPNG = Base64.getDecoder()
			.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z1BbDwAFfwH9h3XwyAAAAABJRU5ErkJggg==");
	byte[] fakeResizedPNG = Base64.getDecoder()
			.decode("jVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z1BbDwAFfwH9h3XwyAAAAABJRU5ErkJggg==");
	
	@Test
	public void testFlow() throws ImageResizingException {
		ImageResizerUtil resizer = mock(ImageResizerUtil.class);
		when(resizer.resize(smallPNG, ImageModificationType.Thumbnail, "png")).thenReturn(fakeResizedPNG);//TODO only png remove param
		AmazonClient client = mock(AmazonClient.class);
		when(client.getProcessedImage("a", ImageModificationType.Thumbnail)).thenReturn("hola".getBytes());
		when(client.getOriginalImage("b")).thenReturn(smallPNG);
		when(client.uploadFile(fakeResizedPNG, "b")).thenReturn("imageurl");
		
		assertArrayEquals(client.getProcessedImage("a", ImageModificationType.Thumbnail), "hola".getBytes());

		ImageResizerController controller = new ImageResizerController(client);
		byte[] res = controller.getImage(ImageModificationType.Thumbnail.getName(), "a");
		assertArrayEquals(res, "hola".getBytes());
		
		res = controller.getImage(ImageModificationType.Thumbnail.getName(), "b");
		//TODO assertArrayEquals(res, smallPNG);

		//TODO break in smaller test methods test exception
		controller = new ImageResizerController(client);
		try {
			res = controller.getImage("invalid", "a");
		} catch (ImageResizingException e) {
			//TODO assert that exception occurred
		}
	}

	@Test
	public void testResizing() throws ImageResizingException {
		ImageResizerUtil resizer = new ImageResizerUtil();
		try {
			resizer.resize("hola".getBytes(), ImageModificationType.Thumbnail, "png");
		} catch (ImageResizingException e) {
			//TODO assert that exception occurred
		}
		
		byte[] resized = resizer.resize(smallPNG, ImageModificationType.Thumbnail, "png");
		assertNotNull(resized);
	}
}