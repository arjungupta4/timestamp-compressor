package com.tworoads;

import java.io.File;
import java.io.IOException;

public class TestMyOnlineCoder {

	public static void main(String[] args) throws IOException {
		
		String folder = System.getProperty("user.home")+ File.separator;
		
		MyOnlineCoder coder = new MyOnlineCoder(folder +"timestamps", folder+"Coded");
		
				
		coder.codeFile();
		

	}

}
