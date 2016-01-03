package com.tworoads;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.TimeZone;

public class MyOnlineCoder {

	//read	next	timestamp	from	input	file	and	return	as	char*

	BufferedReader reader;

	String	getNextLine(	File	fp	) throws IOException{

		return reader.readLine();
	} 

	DataOutputStream output;

	File input;

	/*Constructor to Provide Input File and OutFile  */
	public MyOnlineCoder(String fileInputName ,String fileOutputName) throws FileNotFoundException {

		output = new DataOutputStream(new FileOutputStream(fileOutputName));

		input = new File(fileInputName);

		reader = new BufferedReader(new FileReader(input));

	}

	boolean flag=false;

	int currentData=0;

	BitSet bitArray = new BitSet();

	int index=0;


	/*Code the Given Timestamp String*/

	void codeData(String dataString) throws IOException{

		int data = Integer.parseInt(dataString.split("\\.")[0]);

		/* As only hours minutes and seconds will be changing and overall 86400 seconds available*/ 

		data = data%86400;

		int microData = Integer.parseInt(dataString.split("\\.")[1]);

		/*		Calculating Any difference in Hour from the previous timestamp*/

		codeHours((data/3600)%24);

		/*		Calculating Any difference in Minute from the previous timestamp*/
		codeMinutes(((data%3600)/60)%60);

		/*		Calculating Any difference in Second from the previous timestamp*/
		codeSeconds(data%60);

		/*      Coding Microseconds data */
		codeMicroseconds(microData);

		currentData= data;

		byte[] binaryData = bitArray.toByteArray();


		/*To Unset the Bit Set in order to convert the data into appropriate number of bytes*/
		/*In case of Bitset while converting to bytes it takes the length until the highest set Bit which might give wrong length of data*/
		if(flag){

			binaryData[(index-1)/8] &= ~(1<<(index-1)%8); 

			flag=false;
		}



		output.write(binaryData);

		bitArray.clear();
		index=0;

	}





	void codeHours(int hoursData){

		/*Checking if there is any difference in the hours data from the previous timeStamp*/
		if((currentData/3600)%24 == hoursData){
			bitArray.set(index++,false);
		}
		else{


			/*In case of Difference setting one bit as true which indicates the hours data has changed*/

			bitArray.set(index++,true);

			/*Converting the hours data into Binary , allocating 5 bits as hours value will range between 0-23*/
			pushInBinary(hoursData  , 5);


		}

	}


	private void pushInBinary(long data, int number_of_bits) {



		//converting int data into binary according to the bits allocated
		for(int i=number_of_bits-1; i >=0;i--){

			bitArray.set(index++, (data & (1 << i))!=0   );

		}


		/*In case of TimeStamp MicroSeconds Data will be last data added to bitSet before converting into bytes*/
		/*Setting the last Bit as True in case it is not set so that appropriate number of bytes allocated*/
		if(number_of_bits==20){

			if(!bitArray.get(index-1)){

				bitArray.set(index-1, true);

				flag=true;

			}

		}


	}


	void codeMinutes(int minutesData){

		/*Checking if there is any difference in the minutes data from the previous timeStamp*/
		if((((currentData%3600)/60)%60)==minutesData){
			bitArray.set(index++,false);
		}
		else{

			/*In case of Difference setting one bit as true which indicates the minutes data has changed*/
			bitArray.set(index++,true);

			/*Converting the hours data into Binary , allocating 6 bits as minutes value will range between 0-59*/
			pushInBinary( (minutesData) , 6);
		}
	}

	void codeSeconds(int secondsData){

		/*Checking if there is any difference in the seconds data from the prehoursvious timeStamp*/
		if((currentData%60)==secondsData){
			bitArray.set(index++,false);
		}
		else{

			/*In case of Difference setting one bit as true which indicates the seconds data has changed*/	
			bitArray.set(index++,true);

			/*Converting the hours data into Binary , allocating 6 bits as seconds value will range between 0-59*/
			pushInBinary(secondsData, 6);
		}

	}

	void codeMicroseconds(int microData){


		/*Converting the microdata data into Binary , allocating 20 bits as microseconds value will range between 0-999999*/
		pushInBinary(microData, 20);


	}

	/*Extracting the Date from epoch time and converting into binary as Date will be a constant data*/
	void codeDate(String dateString) throws IOException{

		String date = dateString.split("\\.")[0];

		DateFormat format = new SimpleDateFormat("ddMMyyyyHHmmss");
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		long longDate = (Integer.parseInt(date))*1000l;

		Date date2 = new Date(longDate);

		long formattedDate = Long.parseLong(format.format(date2));


		/*Removing last 6 digits which gives information related to hours, minutes and seconds*/
		pushInBinary(formattedDate/1000000, 32);

		output.write(bitArray.toByteArray());

		bitArray.clear();
		index=0;
		//String day month and year//String day month and year
	}




	void codeFile() throws IOException{


		String date  = 	getNextLine(input);

		codeDate(date);

		/*Uncomment them in order to get the Mean and standard deviation, also need to add Apache Commons Math in Build Path*/
		
		//DescriptiveStatistics stats = new DescriptiveStatistics();

		//long startTime = System.nanoTime();

		codeData(date);

		//stats.addValue((System.nanoTime()-startTime));

		String temp = getNextLine(input);



		while(temp!=null){

			//startTime = System.nanoTime();

			codeData(temp);

			//stats.addValue((System.nanoTime()-startTime));

			temp = getNextLine(input);

		}

		//System.out.println("Mean Time =" + stats.getMean());

		//System.out.println("Std Deviation Time =" + stats.getStandardDeviation());

		output.close();



	}


}
