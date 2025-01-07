package com.example.bookshelf.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class ImageProxyController {

	@GetMapping("/proxy-image")
	public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
	    try {
	        System.out.println("Proxying image from URL: " + url);
	        
	        RestTemplate restTemplate = new RestTemplate();
	        
	        HttpHeaders headers = new HttpHeaders();
	        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");
	        headers.set("Accept", "image/jpeg,image/png,image/*");
	        
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        
	        ResponseEntity<byte[]> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            entity,
	            byte[].class
	        );
	        
	        System.out.println("Image fetch successful, content length: " + 
	            (response.getBody() != null ? response.getBody().length : 0));
	        
	        HttpHeaders responseHeaders = new HttpHeaders();
	        responseHeaders.setContentType(MediaType.IMAGE_JPEG);
	        responseHeaders.setCacheControl("no-cache");
	        responseHeaders.setAccessControlAllowOrigin("*");
	        
	        return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
	    } catch (Exception e) {
	        System.err.println("Error proxying image: " + e.getMessage());
	        e.printStackTrace();
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
