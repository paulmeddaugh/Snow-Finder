package com.snowfinder.logic;

public class ZipCodeAPIException extends RuntimeException {
	public ZipCodeAPIException(String message) {
		super("ZipCodeAPIException: " + message);
	}
}
