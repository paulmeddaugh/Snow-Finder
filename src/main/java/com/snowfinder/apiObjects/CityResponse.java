package com.snowfinder.apiObjects;

public class CityResponse {
	
	private String name;
	private String state;
	private float snowDepth;
	private float temp;
	private float distanceAway;
	
	public CityResponse(String name, String state, float snowDepth, float temp, float distanceAway) {
		super();
		this.name = name;
		this.state = state;
		this.snowDepth = snowDepth;
		this.temp = temp;
		this.distanceAway = distanceAway;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public float getSnowDepth() {
		return snowDepth;
	}
	public void setSnowDepth(float snowDepth) {
		this.snowDepth = snowDepth;
	}
	public float getTemp() {
		return temp;
	}
	public void setTemp(float temp) {
		this.temp = temp;
	}
	public float getDistanceAway() {
		return distanceAway;
	}
	public void setDistanceAway(float distanceAway) {
		this.distanceAway = distanceAway;
	}
	
}
