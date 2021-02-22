import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

/*
* @author Pascal Polchow, s0564053
* @version 1.0
*/

public class PlusPuzzle {
	
	private int[] correctNumbers;
	private Random rand = new Random();
	private int maxBottomValue; 
	private int maxTopValue;
	private int[] fakeNumbers;
	private int[] mixedNumbers;
	
	public PlusPuzzle(int difficulty) {
		maxBottomValue = difficulty;
		generateCorrectNumbers();
		fakeNumbers = generateFakeNumbers(maxTopValue, correctNumbers[14]);
		mixedNumbers = mixNumbers(correctNumbers, fakeNumbers);
	}
	
	private void generateCorrectNumbers() {
		correctNumbers = new int[15];
		int[] bottomNumbers = new int[15];
		for (int i = 0; i < 5; i++) {
			bottomNumbers[i] = rand.nextInt(maxBottomValue-1)+1; //+1 damit der Wert immer > 0 ist
		}
		correctNumbers = calculateTopNumbers(bottomNumbers);
	}
	
	private int[] generateFakeNumbers(int size, int maxValue) {
		int[] fakeNumbers = new int[size];
		for (int i = 0; i < size; i++) {
			fakeNumbers[i] = rand.nextInt(maxValue-1)+1;
		}
		return fakeNumbers;
	}

	private int[] calculateTopNumbers (int[] bottomNumbers) {
		int[] calculatedNumbers = bottomNumbers;
		calculatedNumbers[5] = Math.addExact(bottomNumbers[0], bottomNumbers[1]);
		calculatedNumbers[6] = Math.addExact(bottomNumbers[1], bottomNumbers[2]);
		calculatedNumbers[7] = Math.addExact(bottomNumbers[2], bottomNumbers[3]);
		calculatedNumbers[8] = Math.addExact(calculatedNumbers[3], calculatedNumbers[4]);
		calculatedNumbers[9] = Math.addExact(calculatedNumbers[5], calculatedNumbers[6]);
		calculatedNumbers[10] = Math.addExact(calculatedNumbers[6], calculatedNumbers[7]);
		calculatedNumbers[11] = Math.addExact(calculatedNumbers[7], calculatedNumbers[8]);
		calculatedNumbers[12] = Math.addExact(calculatedNumbers[9], calculatedNumbers[10]);
		calculatedNumbers[13] = Math.addExact(calculatedNumbers[10], calculatedNumbers[11]);
		calculatedNumbers[14] = Math.addExact(calculatedNumbers[12], calculatedNumbers[13]);
		maxTopValue = calculatedNumbers[14];
		return calculatedNumbers;
	}
	
	private int[] mixNumbers(int[] correctNumbers, int[] fakeNumbers) {
		int[] mixedNumbers = new int[correctNumbers.length];
		List<Integer> numberList = new LinkedList<Integer>();
		for (int i = 0; i < mixedNumbers.length; i++) { 
			if(i < 5) {
				numberList.add(fakeNumbers[i]);
			}
			else {
				numberList.add(correctNumbers[i]);
			}
		}
		Collections.shuffle(numberList);
		for (int i = 0; i < numberList.size(); i++) {
			mixedNumbers[i] = numberList.get(i);
		}
		return mixedNumbers;
	}
	
	public int[] getCorrectNumbers() {
		return correctNumbers;
	}
	
	public int[] getFakeNumbers() {
		return fakeNumbers;
	}
	
	public int[] getMixedNumbers() {
		return mixedNumbers;
	}
	
	public boolean compareNumbers(int[] pyramidNumbers) {
		for (int i = 0; i < correctNumbers.length; i++) {
			if(correctNumbers[i] != pyramidNumbers[i]) {
				return false;
			}
		}
		return true;
	}
}
