import java.util.Random;

/*
* @author Pascal Polchow, s0564053
* @version 1.0
*/

public class SchlossKnack {

	private int[] secretNumber;
	private int[] enteredNumber;
	private String[] comparisonAsSymbols;
	private Random rand = new Random();
	
	public SchlossKnack(int maximumValue) {
		secretNumber = new int[4];
		enteredNumber = new int[4];
		comparisonAsSymbols = new String[4];
		generateSecretNumber(maximumValue);
	}
	
	public void generateSecretNumber(int range){ 
		for (int i = 0; i < secretNumber.length; i++) {
			secretNumber[i] = rand.nextInt(range);
		}
	}
	
	public String printSecretNumber() {
		int numbers[] = secretNumber;
		String printCode = "Top Secret: "+numbers[0]+" ";
		for(int i = 1; i < numbers.length; i++) {
			printCode += numbers[i] + "  ";
		}
		return printCode;
	}
	
	public String[] getComparisonAsSymbols() { //gibt das Ergebnis als Symbole für die indicatorLabels zurück
		return comparisonAsSymbols;
		
	}
		
	public void enterNumber(int[] digits) {
		  enteredNumber[0] = digits[0];
		  enteredNumber[1] = digits[1];
		  enteredNumber[2] = digits[2];
		  enteredNumber[3] = digits[3];
	}
	
	public boolean compareNumbers() {
		compareSingleDigits(secretNumber,enteredNumber);
		if(isCorrectNumber()) {
			return true;
		}
		return false;
	}
	
	private void compareSingleDigits(int[] first, int[] second) {
		for(int i = 0; i < second.length; i++) {
			for(int j = 0; j < first.length; j++) {
				if(second[i] == first[i]) {
					comparisonAsSymbols[i] = ":-)"; 
				}
				else if(second[i] == first[0] || second[i] == first[1] || second[i] == first[2] || second[i] == first[3]) {
					comparisonAsSymbols[i] ="<-->";
				}
				else { 
					comparisonAsSymbols[i]="X"; 
					} 
			}
		}
	}
	
	private boolean isCorrectNumber() {
		int[] secret = secretNumber;
		int[] entered = enteredNumber;
		int check = 0;
		for(int i = 0; i < secret.length;i++) {
			if(secret[i] == entered[i]) {
					check++;	
			}
		}
		if(check != secret.length) {
			return false;
		}
		return true;
	}
}
