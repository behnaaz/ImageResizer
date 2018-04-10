package com.example.springdemo;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Base64;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResizerServiceTest {//clean pom
	byte[] smallPNG = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z1BbDwAFfwH9h3XwyAAAAABJRU5ErkJggg==");

	@Test
	public void test() throws IOException {		
		        AmazonClient client = mock(AmazonClient.class);
		        when(client.getProcessedImage("a", 1, 1)).thenReturn("hola".getBytes());
		        when(client.getOriginalImage("b")).thenReturn(smallPNG);
		 
		        // use mock in test....
		        assertArrayEquals(client.getProcessedImage("a", 1, 1), "hola".getBytes());
		
		ImageResizerController controller = new ImageResizerController(client);
		byte[] res = controller.getImage("a", "param2", "param3");
        assertArrayEquals(res, "hola".getBytes());

        res = controller.getImage("b", "param2", "param3");
        assertArrayEquals(res, "omg".getBytes());

        
		System.out.println("Fffffffffff"+res);
	}
	
	@Test
	public void testResizing() throws IOException {
		byte[] resized = ImageResizerUtil.resize("hola".getBytes(), 1, 5, "jpg");
		assertNull(resized);
		
		resized = ImageResizerUtil.resize(smallPNG, 1, 5, "png");
		assertNotNull(resized);
	}
}