package com.tworoads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;




public class MyOnlineDecoder {


	long epoch_current=0;

	int prev_hours=0,prev_min=0,prev_sec=0;

	InputStream in;

	PrintWriter out;

	File input;

	File output;

	/*Constructor to Provide Input File and OutFile  */
	public MyOnlineDecoder(String	input_file,	String	output_file) throws FileNotFoundException {

		input = new File(input_file);

		in = new FileInputStream(input_file);

		output = new File(output_file);

		out = new PrintWriter(output);

	}


	/*Parsing the File and decoding it */
	void	parseAndDecode() throws IOException, ParseException{

		byte[] bytes = new byte[4];

		in.read(bytes, 0, 4);

		decodeDate(bytes);


		/*Uncomment them in order to get the Mean and standard deviation, also need to add Apache Commons Math in Build Path*/
		
		long startTime;
		//DescriptiveStatistics stats = new DescriptiveStatistics();

		
		/*Reading the bytes until the end of file and decoding the data by reading in chunks of bytes*/
		while(in.available()!=0){

			//startTime = System.nanoTime();

			decodeData();

		//	stats.addValue(System.nanoTime()-startTime);

		}

		//System.out.println("Mean Time =" + stats.getMean() );

		//System.out.println("Standard Deviation Time =" + stats.getStandardDeviation() );

		out.close();


	}

	/*Decode the date which is in ddmmYYYY format taking 32 bits and converting into epoch time*/
	void decodeDate(byte[] bytes) throws ParseException{

		int result=0;

		result+=valuefromByte(bytes[0], 1, 8, 32-8);

		result+=valuefromByte(bytes[1], 1, 8, 32-8-8);

		result+=valuefromByte(bytes[2], 1, 8, 32-24);

		result+=valuefromByte(bytes[3], 1, 8, 0);

		SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
		df.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		Date date = df.parse(Integer.toString(result));

		epoch_current= date.getTime()/1000;

	}

	/*Decoding the data and writing to the file*/
	void decodeData() throws IOException{

		byte[] bytes = new byte[5];

		in.read(bytes, 0, 3);

		int seconds=-1;
		int min=-1;
		int hours=-1;

		int currentBit=1;

		/*Checking if hours changed, In case they changed extracting the new value*/
		if( (bytes[0] & (1 << (currentBit-1)) )!=0 ){

			hours = decodeHours(bytes);

			currentBit+=5;
		} 

		currentBit++;

		/*Checking if minutes changed, In case they changed decoding the new value*/
		if((bytes[0] & (1 << (currentBit-1))) !=0 ){

			min = decodeMinutes(bytes,currentBit+1);

			currentBit+=6;
		}

		currentBit++;

		/*Checking if seconds changed, In case they changed decoding the new value*/
		if(( bytes[(currentBit-1)/8] & (1<<(((currentBit-1)%8))))!=0 ){

			seconds =	decodeSeconds(bytes,currentBit+1);

			currentBit+=6;

		}

		currentBit++;

		/*Decoding the microseconds value*/
		int microSeconds = decodeMicroSeconds(in, bytes,currentBit);

		checkAndPrint(hours, min, seconds, microSeconds);


	}

	/*Making appropriate calculations to get the timestamp for the new decoded data*/
	private void checkAndPrint(int hours, int min, int seconds, int microSeconds) {

		if(hours==-1 && min==-1 && seconds==-1 )
		{

		} else {

			/*Since we are storing absolute data for hours, seconds, minutes deleting the previous values from the initial time */

			if(hours!=-1){
				epoch_current= epoch_current - prev_hours*3600 + hours*3600;
				prev_hours=hours;
			}

			if(min!=-1){
				epoch_current= epoch_current - prev_min*60 + min*60;
				prev_min=min;

			}

			if (seconds!=-1) {
				epoch_current = epoch_current - prev_sec + seconds;
				prev_sec= seconds;

			}



		}

		/*Formatting the string to add zeros */
		String outputData = epoch_current + "."  + String.format("%06d", microSeconds);

		out.println(outputData);

	}


	/*Decoding Microseconds*/
	private int decodeMicroSeconds(InputStream in, byte[] bytes, int currentBit) throws IOException {


		int  result=0;

		/*Calculating and further reading the bytes needed apart from 3 bytes already present */
		/*Total Bits can range from 23-40 thus the allocated bytes will range from 3 bytes to 5 bytes*/

		/*Calculations to adjust the indexes to be read*/

		in.read(bytes, 3, ((currentBit+20-1-1)/8)+1-3);

		int startingIndex = (currentBit%8) == 0  ? 8 : (currentBit%8)  ;

		int remainingBits = 20 - (8+1-startingIndex);


		result += valuefromByte(bytes[(currentBit-1)/8], startingIndex, 8, remainingBits);

		currentBit+= 8+1-startingIndex ;

		result+= valuefromByte(bytes[(currentBit-1)/8], currentBit%8, 8, remainingBits-8);

		currentBit+= 8;

		remainingBits-=8;

		if (remainingBits>8) {

			result+= valuefromByte(bytes[(currentBit-1)/8 ], currentBit%8, 8, remainingBits-8);
			currentBit+= 8;
			remainingBits-=8;

			result+= valuefromByte(bytes[(currentBit-1)/8 ], currentBit%8, remainingBits, 0);


		} else{

			result+= valuefromByte(bytes[(currentBit-1)/8 ], currentBit%8, remainingBits, 0);

		}

		return result;

	}


	private int decodeHours(byte[] bytes) {

		// since first Bit Indicates if the value changed and Next 5 Bit represent the Hours
		int result=0;


		result = valuefromByte(bytes[0], 2, 6, 0);

		return result;

	}


	private int decodeMinutes(byte[] bytes, int currentBit) {

		int result=0;

		if(currentBit>3)
		{

			int remainingBits = 6-(8+1-currentBit);

			result +=	valuefromByte(bytes[0], currentBit, 8, remainingBits);

			result +=    valuefromByte(bytes[1], 1 , remainingBits , 0 );

			return result;
		}


		result = valuefromByte(bytes[0], currentBit, currentBit+(6-1), 0);

		return result;


	}

	/*Generic function to give value between the given indexes with basepower starting from the lastIndex and incrementing further*/
	private int valuefromByte(byte byt, int startingIndex, int lastIndex , int basePower) {

		int result=0;

		for(int i=lastIndex; i>=startingIndex; i-- ) {

			if((byt & (1 << (i-1)))!=0)
			{
				result+= Math.pow(2, basePower);
			}

			basePower++;

		}

		return result;
	}

	/*Decoding the seconds using the current reading the bits and using the data to calculate the new value*/
	private int decodeSeconds(byte[] bytes, int currentBit) {

		int result=0;

		int startingIndex = currentBit%8;

		int lastIndex = ((startingIndex+5)/8)==0 ? startingIndex+5 : 8;

		int remainingBits = 6-((lastIndex-startingIndex)+1);

		result+=valuefromByte(bytes[currentBit/8], currentBit%8, lastIndex, remainingBits);

		if(remainingBits>0){

			result+=valuefromByte(bytes[currentBit/8 + 1], 1, remainingBits, 0);

		}

		return result;

	}

}

