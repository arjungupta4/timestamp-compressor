package com.tworoads;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class TestMyOnlineDecoder {

	public static void main(String[] args) throws IOException, ParseException {
		
		String folder = System.getProperty("user.home")+ File.separator;
		
		
		MyOnlineDecoder decoder = new MyOnlineDecoder(folder +"Coded", folder+"DeCoded");
		
		
		decoder.parseAndDecode();
		

	}

}
