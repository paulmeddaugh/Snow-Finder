package com.snowfinder.apiObjects;

public class ZipCodeRequest {
	
	private String zipCode;
	private String radius;
	
	public ZipCodeRequest(String zipCode, String radius) {
		super();
		this.zipCode = zipCode;
		this.radius = radius;
	}
	
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getRadius() {
		return radius;
	}
	public void setRadius(String radius) {
		this.radius = radius;
	}
}
