package crystal.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Utility {
	public static Scanner getFileScanner(String fileName){
		File f = new File(fileName);
		Scanner input = null;
		try {
			input = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(input.hasNextLine()){
			String line = input.nextLine();
			System.out.println(line);
		}
		
		return input;
		
	}
}
