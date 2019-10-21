

/**
 * <p>
 * Title: Computer Networks II
 * </p>
 * 
 * <p>
 * Description: Assignment for year 2019
 * </p>
 * 
 * <p>
 * Company: A.U.Th.
 * </p>
 * 
 * @author  Kirmizis Athanasios
 * @AEM     8835
 * @email   athakirm@ece.auth.gr
 * @phone   6951233051
 * @version 4.0
 */

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

public class userApplication {
	
	///////////////Global Definitions///////////////
	
	String echoesFileName1 = "F:\\EchoesPings.txt";
	String echoesFileName2 = "F:\\EchoesThroughput.txt";
	String imageFileName1 = "F:\\ImageCAM1.jpg";
	String imageFileName2 = "F:\\ImageCAM2.jpg";
	String audioFileName1 = "F:\\AudioSampleValues.txt";
	String audioFileName2 = "F:\\AudioSampleDiffs.txt";
	String audioFileName3 = "F:\\AudioSampleValuesAQ.txt";
	String audioFileName4 = "F:\\AudioSampleDiffsAQ.txt";
	String audioFileName5 = "F:\\AudioStepsAQ.txt";
	String audioFileName6 = "F:\\AudioMeansAQ.txt";
	String copterFileName1 = "F:\\CopterTelemetry.txt";
	String OBDFileName1 = "F:\\OBDParameterValues1.txt";
	String OBDFileName2 = "F:\\OBDParameterValues2.txt";
	String OBDFileName3 = "F:\\OBDParameterValues3.txt";
	String OBDFileName4 = "F:\\OBDParameterValues4.txt";
	String OBDFileName5 = "F:\\OBDParameterValues5.txt";
	int audioPacketLength = 128;
	int audioPacketOverhead = 4;
	int ASCIIConstantValue = 48;
	
	////////////////////////////////////////////////
	
	//////////////////Main Function/////////////////
	
	public static void main(String[] param) 
	{
		
		(new userApplication()).demo();

	}
	
	////////////////////////////////////////////////
	
	///////Sender and Receiver Set Functions////////
	
	public void setSenderAndSendPacket(String packetInfo, int serverPort) 
	{
		
		try {
			
			DatagramSocket s = new DatagramSocket();
			byte[] txbuffer = packetInfo.getBytes();
		
			byte[] hostIP = { (byte)155,(byte)207,(byte)18,(byte)208 };
			InetAddress hostAddress = InetAddress.getByAddress(hostIP);
			DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
			s.send(p);
			
			s.close();
		}
		catch (Exception x) {
			System.out.println(x);
		}
		
	}
	
	public String setReceiverAndReceiveString(int clientPort, int timeout) 
	{
		
		String message = "";
		
		try{
			
			DatagramSocket r = new DatagramSocket(clientPort);
			
			r.setSoTimeout(timeout);
			
			byte[] rxbuffer = new byte[2048];
			DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
		
			try {
				r.receive(q);
				message = new String(rxbuffer,0,q.getLength());
				
			} catch (Exception x) {
				System.out.println(x);
			}
		
			r.close();
			
		} catch (Exception x) {
			System.out.println(x);
			System.out.print("Abrupt end of packets.\n");
		}
		
		return message;
		
	}
	
	public byte[] setReceiverAndReceiveImage(int clientPort, int timeout) 
	{
		
		int counter = 0;
		byte[] theImage = new byte[80000];
		
		boolean flagToStart = false;
		boolean flagToEnd = false; 
		
		try {
			
			DatagramSocket r = new DatagramSocket(clientPort);
			
			r.setSoTimeout(timeout);
			
			byte[] rxbuffer = new byte[2048];
			DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
			
			while(true) {
				
				try {
					r.receive(q);
					
					for(int j=0; j<q.getLength(); j++) {
						
						//Adjust signed bytes to int for comparison
						int ib = rxbuffer[j] & 0xFF;
						int ib2 = rxbuffer[j+1] & 0xFF;
						
						//Image Start Delimiters
						if(ib == 255 && ib2 == 216) {
							
							System.out.print("STARTED RECEIVING...\n");
							flagToStart = true;
					    }
						
						//Image Ending Delimiters	
						if(ib==255 && ib2==217) {
							
							System.out.print("FINISHED RECEIVING.\n");
							theImage[counter] = rxbuffer[j];
							theImage[counter+1] = rxbuffer[j+1];
							flagToEnd = true;
							break;
						}
						
						if(flagToStart) {
							
							theImage[counter] = rxbuffer[j];
							counter++;
						}
						
					}
					
					if(flagToEnd) {
						
						break;
					}
					
				} catch (Exception x) {
					System.out.println(x);
					System.out.print("Abrupt end of packets.\n");
					break;
				}
				
			}
			
			r.close();
			
		} catch (Exception x) {
			System.out.println(x);
		}
		
		return theImage;
		
	}
	
	public byte[] setReceiverAndReceiveAudio(int clientPort, int timeout, int seconds)
	{
		
		byte[] encodedAudio = new byte[seconds*32*audioPacketLength];
		int counter = 0;
		
		try{
			
			DatagramSocket r = new DatagramSocket(clientPort);
			
			r.setSoTimeout(timeout);
			
			byte[] rxbuffer = new byte[2048];
			DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
			
			System.out.println("Loading Audio...");
			
			while(true){
					
					try {
						
						r.receive(q);

						for(int i=0;i<audioPacketLength;i++) {
							encodedAudio[i+counter*audioPacketLength] = rxbuffer[i];
						}
						counter++;
						
						if(counter==seconds*32) {
							System.out.println("Audio Loaded.");
							break;
						}
						
					} catch (Exception x) {
						System.out.println(x);
						System.out.print("Abrupt end of packets.\n");
						break;
					}
				
			}
			
			r.close();
			
		} catch (Exception x) {
			System.out.println(x);
		}
		
		return encodedAudio;
		
	}

	public ArrayList<byte[]> setReceiverAndReceiveAudioWithAQ(int clientPort, int timeout, int seconds)
	{
		
		byte[] encodedAudio = new byte[seconds*32*audioPacketLength];
		byte[] header = new byte[seconds*32*audioPacketOverhead];
		int counter = 0;
		
		try{
			
			DatagramSocket r = new DatagramSocket(clientPort);
			
			r.setSoTimeout(timeout);
			
			byte[] rxbuffer = new byte[2048];
			DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
			
			System.out.println("Loading Audio...");
			
			while(true){
					
					try {
						
						r.receive(q);
						
						for(int i=0;i<audioPacketOverhead;i++) {
							header[i+counter*audioPacketOverhead] = rxbuffer[i];
						}
						
						for(int i=0;i<audioPacketLength;i++) {
							encodedAudio[i+counter*audioPacketLength] = rxbuffer[i+audioPacketOverhead];
						}
						counter++;
						
						if(counter==seconds*32) {
							System.out.println("Audio Loaded.");
							break;
						}
						
					} catch (Exception x) {
						System.out.println(x);
						System.out.print("Abrupt end of packets.\n");
						break;
					}
				
			}
			
			r.close();
			
		} catch (Exception x) {
			System.out.println(x);
		}
		
		ArrayList<byte[]> audioAndHeader = new ArrayList<byte[]>();
		audioAndHeader.add(header);
		audioAndHeader.add(encodedAudio);
	
		return audioAndHeader;
		
	}
	
	///////////////////////////////////////////////
	
	/////////////Sub-questions Functions////////////
	
	public void receiveEchoes(String packetInfo, int serverPort, int clientPort, int timeout)
	{
		int j = 0;

		long[] responseTimes = new long[25000];
		long startTime = System.currentTimeMillis();
		
		// 241000ms = 4 minutes and 1 sec
		long timeOutTime = 241000;                       						
		
		//For ~4 minutes get pings from server and calculate response times 		
		while(System.currentTimeMillis() - startTime < timeOutTime) {	
			
			long time1 = System.currentTimeMillis();
		
			setSenderAndSendPacket(packetInfo, serverPort);
			setReceiverAndReceiveString(clientPort, timeout);
			
			responseTimes[j] = System.currentTimeMillis()-time1;
			System.out.println(responseTimes[j]+"\n");
			
			j++;
		
		}
		
		int numberOfEchoes = j - 1;
		long[] averages = new long[25000];
		
		//Could be either 8000, 16000, or 32000
		long samplingTime = 16000;
		
		//Get the moving average of throughput based on previous response times
		averages = calculateMovingAverage(responseTimes, numberOfEchoes, samplingTime);
		
		//Save response times to .txt file		
		saveLongArrayToFile(responseTimes, echoesFileName1);

		//Save moving average to .txt file		
		saveLongArrayToFile(averages, echoesFileName2);

	}
	
	public void receiveImage(String packetInfo, int serverPort, int clientPort, int timeout)
	{
		
		byte[] theImage = new byte[80000];
		
		setSenderAndSendPacket(packetInfo, serverPort);
		
		//Receive the image bytes
		theImage = setReceiverAndReceiveImage(clientPort, timeout);
		
		//Save the image to .jpg file
		try {
			FileOutputStream outputStream = new FileOutputStream(imageFileName2);
			outputStream.write(theImage);
			outputStream.close();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void receiveAudio(String packetInfo, int serverPort, int clientPort, int timeout, int seconds)
	{
				
		byte[] encodedAudio = new byte[seconds*32*audioPacketLength];
		byte[] audioPlayer = new byte[seconds*32*2*audioPacketLength];
		int[] differences = new int[seconds*32*2*audioPacketLength];
		
		setSenderAndSendPacket(packetInfo, serverPort);
		
		//Receive the encoded audio bytes
		encodedAudio = setReceiverAndReceiveAudio(clientPort, timeout, seconds);
		
		//Decode the audio bytes
		audioPlayer = decodeAudio(encodedAudio);
		
		//Save audio sample values to .txt file
		saveByteArrayToFile(audioPlayer, audioFileName1);
		
		//Get the audio sample differences
		differences = getAudioSampleDifferences(encodedAudio);
		
		//Save audio sample differences to .txt file
		saveIntArrayToFile(differences, audioFileName2);
		
		//Setup Output Audio Line and play the audio
		playAudio(audioPlayer);
		
	}
	
	public void receiveAudioWithAQ(String packetInfo, int serverPort, int clientPort, int timeout, int seconds)
	{
				
		byte[] encodedAudio = new byte[seconds*32*audioPacketLength];
		byte[] header = new byte[seconds*32*audioPacketOverhead];
		
		byte[] audioPlayer = new byte[seconds*32*2*audioPacketLength];
		
		int[] differences = new int[seconds*32*2*audioPacketLength];
		int[] stepsArray = new int[header.length/audioPacketOverhead];
		int[] meansArray = new int[header.length/audioPacketOverhead];
		
		setSenderAndSendPacket(packetInfo, serverPort);
		
		ArrayList<byte[]> audioAndHeader = setReceiverAndReceiveAudioWithAQ(clientPort, timeout, seconds);
		
		//Receive the overhead of the audio bytes
		header = audioAndHeader.get(0);
		
		//Receive the encoded audio bytes
		encodedAudio = audioAndHeader.get(1);
		
		//Decode the audio bytes
		audioPlayer = decodeAudioWithAQ(header, encodedAudio);
		
		//Save audio sample values to .txt file
		saveByteArrayToFile(audioPlayer, audioFileName3);
		
		//Get the audio sample differences
		differences = getAudioWithAQSampleDifferences(header, encodedAudio);
		
		//Save audio sample differences to .txt file
		saveIntArrayToFile(differences, audioFileName4);
		
		ArrayList<int[]> stepAndMean = getStepAndMean(header);
		
		//Get the step of each packet of the audio bytes
		stepsArray = stepAndMean.get(0);
		
		//Save step values to .txt file
		saveIntArrayToFile(stepsArray, audioFileName5);
		
		//Get the mean of each packet of the audio bytes
		meansArray = stepAndMean.get(1);
		
		//Save mean values to .txt file
		saveIntArrayToFile(meansArray, audioFileName6);
		
		//Setup Output Audio Line and play the audio
		playAudio(audioPlayer);
		
	}
	
	public void receiveCopterTelemetry(String packetInfo, int serverPort, int clientPort, int timeout)
	{
		//MUST RUN SIMULTANEOUSLY WITH ithakicopter.jar TO PROPERLY RECEIVE DATA!!
		//ALSO ONLY WORKS WITH clientPort = 48038!
		
		int j = 0;
		
		String[] telemetry = new String[300];
		long startTime = System.currentTimeMillis();
		
		// 30000ms = 30 secs of movement
		long timeOutTime = 30000;                       						
		
		//For 30 secs, get telemetry data from copter
		while(System.currentTimeMillis() - startTime < timeOutTime) {	
			
			long time1 = System.currentTimeMillis();
			
			setSenderAndSendPacket(packetInfo, serverPort);
			telemetry[j] = setReceiverAndReceiveString(clientPort, timeout);

			System.out.println(telemetry[j]+"\n");
			
			j++;
			
			while(System.currentTimeMillis() - time1 < 100) {
				//Wait and Sample every 0.1 secs
			}
		
		}
		
		//Save the telemetry data to .txt file
		saveStringArrayToFile(telemetry, copterFileName1);
		
	}
	
	public void receiveOBDII(String packetInfo, int serverPort, int clientPort, int timeout, String filename)
	{
		
		int j = 0;
		String message = "";

		int[] parameterValues = new int[25000*2];
		long startTime = System.currentTimeMillis();
		
		// 241000ms = 4 minutes and 1 sec
		long timeOutTime = 241000;                       						
		
		//For ~4 minutes get OBD values from server and calculate parameters	
		while(System.currentTimeMillis() - startTime < timeOutTime) {	
		
			setSenderAndSendPacket(packetInfo, serverPort);
			message = setReceiverAndReceiveString(clientPort, timeout);
			System.out.println(message);
			
			if(message.length()>8) {
				
				//XX Value comes first
				parameterValues[j] = Integer.parseInt(message.substring(6,8), 16);
				j++;
				//YY Value comes second
				parameterValues[j] = Integer.parseInt(message.substring(9,11), 16);
				j++;
			}
			else {
				
				parameterValues[j] = Integer.parseInt(message.substring(6,8), 16);
				j++;

			}		
		
		}
		
		//Save OBD parameter values to .txt file		
		saveIntArrayToFile(parameterValues, filename);
		
	}
	
	///////////////////////////////////////////////
	
	////////////////Helpful Functions///////////////
	
	public long[] calculateMovingAverage(long[] responseTimes, int numberOfEchoes, long samplingTime)
	{
		
		long sum = 0;
		int k = 0;
		int counter = 0;
		
		long[] averagesR = new long[25000];
		
		//This part of code calculates the moving average of throughput.
		//Don't ask how. It just does :)		
		for(int i=0; i<numberOfEchoes; i++) {
			
			sum = sum + responseTimes[i];
			
			if(sum >= samplingTime) {
				int number = i+1-counter;
				averagesR[k] = number*32*8/(samplingTime/1000);
				System.out.println("Average: "+ averagesR[k]+"\n");
				k++;
				
				while(sum >= samplingTime) {
					sum = sum - responseTimes[counter];
					counter++;
				}
				
			}
			
		}
		
		return averagesR;
		
	}
	
	public void saveLongArrayToFile(long[] array, String filename)
	{
		
		try{    
			FileOutputStream fos = new FileOutputStream(filename);
		    DataOutputStream dos = new DataOutputStream(fos);
		    
		    for(int i=0; i<array.length; i++) {
		    	dos.writeUTF(String.valueOf((int)array[i]));
		    	dos.writeChars("\n");
		    }
		    
		    dos.close(); 
            System.out.println("\nSave successful.");    
        }
		catch(Exception x){
			System.out.println(x);
		}
		
	}
	
	public byte[] decodeAudio(byte[] encodedAudio)
	{
		
		int counter = 1;
		byte[] decodedAudio = new byte[encodedAudio.length*2];
		
		//Arbitrary...
		decodedAudio[0] = 0; 
		
		//Decode the audio file based on the DPCM encoding 
		for(int i=0; i<encodedAudio.length; i++) {
			
			byte b = encodedAudio[i]; 
			byte nibbleHigh = (byte)((b & 0xF0) >> 4);
			byte nibbleLow = (byte) (b & 0x0F);
			
			byte D1 = (byte)(nibbleHigh - 8);
			byte D2 = (byte)(nibbleLow - 8);
			
			decodedAudio[counter] = (byte)(D1 + decodedAudio[counter-1]);
			counter++;
			
			if(counter==decodedAudio.length) {
				break;
			}
			
			decodedAudio[counter] = (byte)(D2 + decodedAudio[counter-1]);
			counter++;
		}

		return decodedAudio;
		
	}
	
	public int[] getAudioSampleDifferences(byte[] encodedAudio)
	{
		
		int counter = 0;
		int[] differences = new int[encodedAudio.length*2];
			
		//Get the diffs based on the DPCM encoding 
		for(int i=0; i<encodedAudio.length; i++) {
			
			byte b = encodedAudio[i]; 
			byte nibbleHigh = (byte)((b & 0xF0) >> 4);
			byte nibbleLow = (byte) (b & 0x0F);
			
			byte D1 = (byte)(nibbleHigh - 8);
			byte D2 = (byte)(nibbleLow - 8);
			
			differences[counter] = D1;
			counter++;
			
			if(counter==differences.length) {
				break;
			}
			
			differences[counter] = D2;
			counter++;
		}
		
		return differences;
		
	}
	
	public byte[] decodeAudioWithAQ(byte[] header, byte[] encodedAudio)
	{
		
		int counter = 0;
		byte[] decodedAudio = new byte[encodedAudio.length*4];
		
		//Arbitrary...
		int previous = 0; 
		
		//Decode the audio file based on the AQ-DPCM encoding 
		for(int i=0, k=0; i<encodedAudio.length; i=i+audioPacketLength, k=k+audioPacketOverhead) {
			
			//For each packet find the step
			int step = ((header[k+3] & 0xFF) << 8) | (header[k+2] & 0xFF);
			
			//For each packet find the mean
			int mean = ((header[k+1] & 0xFF) << 8) | (header[k] & 0xFF);
			
			//For this packet, step, and mean, decode
			for(int j=0; j<audioPacketLength; j++) {
				
				byte b = encodedAudio[i+j]; 
				byte nibbleHigh = (byte)((b & 0xF0) >> 4);
				byte nibbleLow = (byte) (b & 0x0F);
				
				int D1 = ((nibbleHigh - 8)*step);
				int D2 = ((nibbleLow - 8)*step);
				
				int X1 = D1 + mean + previous;
				int X2 = D2 + mean + D1;
				previous = D2;
				
				decodedAudio[counter] = (byte)((X1) & 0xFF);
				counter++;
				
				decodedAudio[counter] = (byte)((X1 >>> 8) & 0xFF);
				counter++;
				
				if(counter==decodedAudio.length) {
					break;
				}
				
				decodedAudio[counter] = (byte)((X2) & 0xFF);
				counter++;
				
				decodedAudio[counter] = (byte)((X2 >>> 8) & 0xFF);
				counter++;
				
			}
			
		}

		return decodedAudio;
		
	}
	
	public int[] getAudioWithAQSampleDifferences(byte[] header, byte[] encodedAudio)
	{
		
		int counter = 0;
		int[] differences = new int[encodedAudio.length*2];
		
		//Decode the audio file based on the AQ-DPCM encoding 
		for(int i=0, k=0; i<encodedAudio.length; i=i+audioPacketLength, k=k+audioPacketOverhead) {
			
			//For each packet find the step
			int step = ((header[k+3] & 0xFF) << 8) | (header[k+2] & 0xFF);
			
			//For this packet, step, and mean, decode
			for(int j=0; j<audioPacketLength; j++) {
				
				byte b = encodedAudio[i+j]; 
				byte nibbleHigh = (byte)((b & 0xF0) >> 4);
				byte nibbleLow = (byte) (b & 0x0F);
				
				int D1 = ((nibbleHigh - 8)*step);
				int D2 = ((nibbleLow - 8)*step);
				
				differences[counter] = D1;
				counter++;
				
				if(counter==differences.length) {
					break;
				}
				
				differences[counter] = D2;
				counter++;
				
			}
			
		}

		return differences;
		
	}
	
	public ArrayList<int[]> getStepAndMean(byte[] header)
	{
		
		int[] stepsArray = new int[header.length/audioPacketOverhead];
		int[] meansArray = new int[header.length/audioPacketOverhead];
		
		for(int k=0; k<header.length; k=k+audioPacketOverhead) {
			
			//For each packet find the step
			stepsArray[k/audioPacketOverhead] = ((header[k+3] & 0xFF) << 8) | (header[k+2] & 0xFF);
			
			//For each packet find the mean
			meansArray[k/audioPacketOverhead] = ((header[k+1] & 0xFF) << 8) | (header[k] & 0xFF);
			
		}
		
		ArrayList<int[]> stepAndMean = new ArrayList<int[]>();
		stepAndMean.add(stepsArray);
		stepAndMean.add(meansArray);
		
		return stepAndMean;	
		
	}
	
	public void saveByteArrayToFile(byte[] array, String filename)
	{
		
		try{    
			FileOutputStream fos = new FileOutputStream(filename);
		    DataOutputStream dos = new DataOutputStream(fos);
		    
		    for(int i=0; i<array.length; i++) {
		    	dos.writeUTF(String.valueOf((int)array[i]));
		    	dos.writeChars("\n");
		    }
		    
		    dos.close(); 
            System.out.println("\nSave successful.");    
        }
		catch(Exception x){
			System.out.println(x);
		}
		
	}
	
	public void saveIntArrayToFile(int[] array, String filename)
	{
		
		try{    
			FileOutputStream fos = new FileOutputStream(filename);
		    DataOutputStream dos = new DataOutputStream(fos);
		    
		    for(int i=0; i<array.length; i++) {
		    	dos.writeUTF(String.valueOf(array[i]));
		    	dos.writeChars("\n");
		    }
		    
		    dos.close(); 
            System.out.println("\nSave successful.");    
        }
		catch(Exception x){
			System.out.println(x);
		}
		
	}
	
	public void playAudio(byte[] audioPlayer)
	{
		
		try {
			AudioFormat linearPCM = new AudioFormat(8000,16,1,true,false);
			SourceDataLine lineOut = AudioSystem.getSourceDataLine(linearPCM);
			lineOut.open(linearPCM,32000);
			lineOut.start();
			
			System.out.println("Playing audio...");
			lineOut.write(audioPlayer,0,audioPlayer.length);
			System.out.println("Audio ended.");
			
			//lineOut.stop();
			//lineOut.close();
		}
		catch(Exception x){
			System.out.println(x);
		}
		
	}
	
	public void saveStringArrayToFile(String[] array, String filename)
	{
		
		try{    
			FileOutputStream fos = new FileOutputStream(filename);
		    DataOutputStream dos = new DataOutputStream(fos);
		    
		    for(int i=0; i<array.length; i++) {
		    	dos.writeUTF(array[i]+"\n");
		    }
		    
		    dos.close(); 
            System.out.println("\nSave successful.");    
        }
		catch(Exception x){
			System.out.println(x);
		}
		
	}
	
	public void getHTTPResponse(String packetInfo, byte[] hostIP, int port) 
	{
		
		try {
			byte[] txbuffer = packetInfo.getBytes();
		
			InetAddress hostAddress = InetAddress.getByAddress(hostIP);
			
			Socket s = new Socket(hostAddress, port);
			
			OutputStream out = s.getOutputStream();
			
			out.write(txbuffer);
			
			InputStream in = s.getInputStream();
			
			System.out.println(convertInputStreamToString(in));
			
			s.close();
		}
		catch (Exception x) {
			System.out.println(x);
		}
		
	}
	
	private static String convertInputStreamToString(InputStream inputStream) 
			throws IOException {

	        ByteArrayOutputStream result = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = inputStream.read(buffer)) != -1) {
	            result.write(buffer, 0, length);
	        }

	        return result.toString(StandardCharsets.UTF_8.name());

			}
	
	///////////////////////////////////////////////
	
	///////////////Core Demo Function///////////////
	
	public void demo() 
	{
		
		//Request Codes (UDP) Definitions
		String echoesPacket = "E7358T00";
		String imagePacket = "M4598CAM=PTZ";
		String soundPacket = "A8009AQF320";
		String copterPacket = "Q9647";
		String OBDPacket = "V1078OBD=01 0D";
		
		//Request Codes (HTTP) Definitions
		String HTTPPacket = "GET /index.html HTTP/1.0\r\n\r\n";
		String copterHTTPPacket = "AUTO FLIGHTLEVEL=100 LMOTOR=180 RMOTOR=180 PILOT \r\n";
		String OBDHTTPPacket = "0x01 0x05\r";
		
		//Ports and Timeout Setup
		int serverPort = 38008;
		int clientPort = 48008;
		int timeout = 4000;
		
		//How many seconds to play the audio (should correspond to FXXX in soundPacket)
		int seconds = 10;
		
		//IP address to get HTTP response from Ithaki
		byte[] hostIP = { (byte)155,(byte)207,(byte)17,(byte)208 };
		
		//Get data from Server with each request
		receiveEchoes(echoesPacket,serverPort,clientPort,timeout);
		receiveImage(imagePacket,serverPort,clientPort,timeout);
		receiveAudio(soundPacket,serverPort,clientPort,timeout,seconds);
		receiveAudioWithAQ(soundPacket,serverPort,clientPort,timeout,seconds);
		getHTTPResponse(HTTPPacket, hostIP, serverPort);
		receiveCopterTelemetry(copterPacket,serverPort,clientPort,timeout);
		receiveOBDII(OBDPacket,serverPort,clientPort,timeout, OBDFileName5);
		
	}
	
	////////////////////////////////////////////////

}

