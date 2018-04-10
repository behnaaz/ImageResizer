package com.example.springdemo;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Base64;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResizerServiceTest {// clean pom
	byte[] smallPNG = Base64.getDecoder()
			.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z1BbDwAFfwH9h3XwyAAAAABJRU5ErkJggg==");
	byte[] fakeResizedPNG = Base64.getDecoder()
			.decode("jVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z1BbDwAFfwH9h3XwyAAAAABJRU5ErkJggg==");

	
	@Test
	public void test() throws IOException {
		ImageResizerUtil resizer = mock(ImageResizerUtil.class);
		when(resizer.resize(smallPNG, 1, 1, "png")).thenReturn(fakeResizedPNG);//TODO only png remove param
		AmazonClient client = mock(AmazonClient.class);
		when(client.getProcessedImage("a", 1, 1)).thenReturn("hola".getBytes());
		when(client.getOriginalImage("b")).thenReturn(smallPNG);
		when(client.uploadFile(fakeResizedPNG, "b")).thenReturn("OK");//TODO
		
		assertArrayEquals(client.getProcessedImage("a", 1, 1), "hola".getBytes());

		ImageResizerController controller = new ImageResizerController(client);
		byte[] res = controller.getImage("a", "param2", "param3");
		assertArrayEquals(res, "hola".getBytes());

		res = controller.getImage("b", "param2", "param3");
		assertArrayEquals(res, smallPNG);
	}

	@Test
	public void testResizing() throws IOException {
		ImageResizerUtil resizer = new ImageResizerUtil();
		byte[] resized = resizer.resize("hola".getBytes(), 1, 5, "png");
		assertNull(resized);

		resized = resizer.resize(smallPNG, 1, 5, "png");
		assertNotNull(resized);
	}
}