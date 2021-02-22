
public class TestPuzzle {

	public static void main (String[] args) {
		PlusPuzzle puzzle = new PlusPuzzle(5);
		int[] correctNumbers = puzzle.getCorrectNumbers();
		System.out.println("solved Numbers:");
		System.out.println("     " + correctNumbers[14]);
		System.out.println("   " + correctNumbers[12] +" "+ correctNumbers[13]);
		System.out.println("  " +correctNumbers[9] +" "+ correctNumbers[10] +" "+ correctNumbers[11]);
		System.out.println(" " +correctNumbers[5] +" "+ correctNumbers[6] +"  "+ correctNumbers[7]+" " + correctNumbers[8]);
		System.out.println(correctNumbers[0] +" "+ correctNumbers[1] +" "+ correctNumbers[2]+"  " + correctNumbers[3] +" " + correctNumbers[4]);
		System.out.println("mixed with fake numbers");
		int[] mixedNumbers = puzzle.getMixedNumbers();
		System.out.println(mixedNumbers[0]+" "+mixedNumbers[1]+" "+mixedNumbers[2]+" "+
							mixedNumbers[3]+" "+mixedNumbers[4]+" "+mixedNumbers[5]+" "+
							mixedNumbers[6]+" "+mixedNumbers[7]+" "+mixedNumbers[8]+" "+
							mixedNumbers[9]+" "+mixedNumbers[10]+" "+mixedNumbers[11]+" "+
							mixedNumbers[12]+" "+mixedNumbers[13]+" "+mixedNumbers[14]+" "+
							mixedNumbers[15]+" "+mixedNumbers[16]+" "+mixedNumbers[17]+" "+
							mixedNumbers[18]+" "+mixedNumbers[19]);
	}

}
