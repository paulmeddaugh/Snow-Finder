package com.snowfinder.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.snowfinder.apiObjects.CityResponse;
import com.snowfinder.apiObjects.CityString;
import com.snowfinder.apiObjects.ZipCodeRequest;

import static com.snowfinder.logic.SnowLogic.loadFromZip;
import static com.snowfinder.logic.SnowLogic.loadMajorCities;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SnowFindController {
	
	@Autowired
    SimpMessagingTemplate template;

	@MessageMapping("/snowUsingZip")
	public void receiveZipMessage(@Payload ZipCodeRequest request) {
		loadFromZip(
			Integer.valueOf(request.getZipCode()), 
			Float.valueOf(request.getRadius()),
			(string) -> template.convertAndSend("/results/snow", string)
		);
	}
	
	@MessageMapping("/snowInUS")
	public void receiveMajorUSMessage() {
		loadMajorCities(
			(string) -> template.convertAndSend("/results/snow", string)
		);
	}
	
	@SendTo("/results/snow")
	public String broadcastMessage(@Payload String res) {
		return res;
	}
}