package parser;

public class RomanConverter {
    /*
     * Converts given string representation of a roman number, to integer of same value.
     */
    public int convertToInt(String romanNumber) {
        if (romanNumber.isEmpty()) return 0;
        if (romanNumber.startsWith("X")) return 10 + convertToInt(romanNumber.substring(1));
        if (romanNumber.startsWith("IX")) return 9 + convertToInt(romanNumber.substring(2));
        if (romanNumber.startsWith("V")) return 5 + convertToInt(romanNumber.substring(1));
        if (romanNumber.startsWith("IV")) return 4 + convertToInt(romanNumber.substring(2));
        if (romanNumber.startsWith("I")) return 1 + convertToInt(romanNumber.substring(1));
        throw new IllegalArgumentException("Invalid roman text representation");
    }
}
