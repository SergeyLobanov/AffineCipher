package lab2;

import java.util.*;

/**
 * Created by Сергей on 20.03.2016.
 */
public class Affine {
    private static final String alphabet = "абвгдежзийклмнопрстуфхцчшщьыэюя";
    private static final int mSqValue = alphabet.length()*alphabet.length();

    private static String [] freqRusBigrams = {"ст" , "но", "то", "на", "ен"};
    private static String [] freqCipherBigrams;
    private static int [] arrayA;

    public static int getNumberByBigram(String bigram) {
        return alphabet.indexOf(bigram.charAt(0))*31 + alphabet.indexOf(bigram.charAt(1));
    }

    public static String getBigramByNumber(int number) {
        return alphabet.charAt(number/31) + "" + alphabet.charAt(number%31);
    }

    public static String filtration(String unfiltered) {
        return unfiltered.replaceAll("[^а-яА-Я]+", "").replaceAll("ё", "е").replaceAll("ъ", "ь");
    }

    // greatest common divisor
    public static int gcd (int a, int b) {
        if (b != 0) {
            return gcd(b, a%b);
        }
        return a;
    }

    // a^(-1)*mod(m)
    public static int inverse (int a, int m) {
        int r0 = a;
        int r1 = m;
        int q = 0;
        int u0 = 1;
        int u1 = 0;
        do
        {
            int t = r0%r1;
            r0 = r1;
            r1 = t;
            t = u1;
            u1 = u0-q*u1;
            u0 = t;
            if (r1 != 0) q = r0/r1;
            else break;
        }
        while (r0%r1 != 0);
        return (u1+m) % m;
    }

    // ax = b*mod(m)
    public static int[] solveEquation(int a, int b, int m) {
        int r = gcd(a, m);
        int [] arrayTemp = new int[r+1];

        if (r == 1) {
            arrayTemp[0] = 1;
            arrayTemp[1] = (b * inverse(a,m) % m);
        }
        else {
            if (b % r != 0) {
                arrayTemp = new int[1];
                arrayTemp[0] = 0;
            }
            else {
                arrayTemp[0] = r;
                arrayTemp[1] = ((b/r)*inverse(a/r, m/r)) % m;
                for (int i = 2; i < r; i++) {
                    arrayTemp[i] = (arrayTemp[i-1] + m/r) % m;
                }
            }
        }

        return arrayTemp;
    }

    //dX*a = dY mod(m)  a = dX^(-1)*dY mod(m)
    public static void solvingSystem (int y1, int x1, int y2, int x2, int m) {
        int dY, dX;
        dY = (y1 - y2 + m)%(m);
        dX = (x1 - x2 + m)%(m);
        arrayA = solveEquation(dX, dY, m);
    }

    // directly for strings
    public static void solvingSystem (String y1, String x1, String y2, String x2, int m) {
        solvingSystem (getNumberByBigram(y1), getNumberByBigram(x1), getNumberByBigram(y2), getNumberByBigram(x2), m);
    }

    // affine transposition deciphering
    public static String deciphering(String ciphertext) {
        StringBuilder result = new StringBuilder();
        int [] cipherBigramNum = new int[ciphertext.length()/2];

        for (int i = 0; i < ciphertext.length()-1; i+=2) {
            cipherBigramNum[i / 2] = getNumberByBigram(ciphertext.charAt(i) + "" + ciphertext.charAt(i + 1));
        }

        getFirstNBigrams(ciphertext, 5);

        int tempA, tempB;
        String tempX1, tempX2, tempY1, tempY2;
        for (int x = 0; x < freqRusBigrams.length*freqRusBigrams.length - 1; x++) {
            if ((x / 5) == (x % 5)) continue;
            tempX1 = freqRusBigrams[x / 5];
            tempX2 = freqRusBigrams[x % 5];
            for (int y = 0; y < freqCipherBigrams.length*freqCipherBigrams.length; y++) {
                if ((y / 5) == (y % 5)) continue;
                tempY1 = freqCipherBigrams[y / 5];
                tempY2 = freqCipherBigrams[y % 5];

                // getting array of 'a' values
                solvingSystem(tempY1, tempX1, tempY2, tempX2, mSqValue);
                System.out.println("X1 " + tempX1 + " Y1 "+ tempY1 + " X2 "+ tempX2 + " Y2 " + tempY2 );

                for (int k = 1; k < arrayA.length; k++) {
                    tempA = arrayA[k];
                    tempB = ((getNumberByBigram(tempY1) - tempA*getNumberByBigram(tempX1))%mSqValue + mSqValue)%mSqValue;
                    System.out.println("A " + tempA + " B " + tempB);

                    for (int i = 0; i < cipherBigramNum.length; i++) {
                        result.append(getBigramByNumber(decipherBigram(cipherBigramNum[i], tempA, tempB, mSqValue)));
                    }
                    System.out.println(result);

                    if (isCorrectText(result.toString())) {
                        return result.toString();
                    }

                    else
                    result = new StringBuilder();
                }
            }
        }

        return null;
    }

    // checking by frequently bigrams matching criterion
    public static boolean isCorrectText(String candidate) {
        Map<Integer, Double> sortedMap = sortMapsByValue(countFrequency(candidate));
        double sum = 0;
        int tempBigramNum;
        for (String key : freqRusBigrams) {
            tempBigramNum = getNumberByBigram(key);
            sum += sortedMap.get(tempBigramNum);
        }
        //check result
        System.out.println("sum " + sum + "\n");

        return sum > 0.055;
    }

    // transformation from bigram Y to X
    private static int decipherBigram(int cipherBigram, int a, int b, int m) {
        return inverse(a, m)*(cipherBigram - b + m)%(m);
    }

    // text analysis
    private static HashMap<Integer, Double> countFrequency(String text) {
        HashMap<Integer, Double> freqMap = new HashMap<>();
        String ciphertext = filtration(text);
        int [] freqArray = new int[mSqValue];
        int n = ciphertext.length();
        int ind1, ind2;

        for (int i = 0; i < n-1; i++) {
            ind1 = alphabet.indexOf(ciphertext.charAt(i));
            ind2 = alphabet.indexOf(ciphertext.charAt(i+1));
            freqArray[ind1*alphabet.length() + ind2]++;
        }

        for (int i = 0; i < mSqValue; i++) {
            freqMap.put(i, (double)freqArray[i]/n);
        }

        return freqMap;
    }

    private static <Integer, Double extends Comparable<? super java.lang.Double>> HashMap<java.lang.Integer, java.lang.Double>
    sortMapsByValue(HashMap<java.lang.Integer, java.lang.Double> freqMap) {

        List<Map.Entry<java.lang.Integer, java.lang.Double>> list =
                new LinkedList<>(freqMap.entrySet());

        Collections.sort( list, (o1, o2) -> (o2.getValue()).compareTo( o1.getValue() ));

        Map<java.lang.Integer, java.lang.Double> result = new LinkedHashMap<>();
        for (Map.Entry<java.lang.Integer, java.lang.Double> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return (HashMap<java.lang.Integer, java.lang.Double>)result;
    }

    //getting bigrams from our cipher
    public static void getFirstNBigrams(String ciphertext, int N) {
        freqCipherBigrams = new String[N];
        Map<Integer, Double> sortedMap = sortMapsByValue(countFrequency(ciphertext));
        int i = 0;
        for(Integer value : sortedMap.keySet()) {
            if (i == N) break;
            freqCipherBigrams[i] = getBigramByNumber(value);
            i++;
        }
    }

    //optional
    public static void printSortedFreq(String ciphertext) {
        Map<Integer, Double> sortedMap = sortMapsByValue(countFrequency(ciphertext));
        for(Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
            // to print with bigrams
            //System.out.println(getBigramByNumber(entry.getKey()) + "\t" + entry.getValue());
        }
        System.out.println();
    }
    //printing our bigrams
    public static void printFirstBigrams() {
        if (freqCipherBigrams == null) {
            System.out.println("You need to set first N bigrams from ciphertext first");
            return;
        }
        for (String bigram : freqCipherBigrams) {
            System.out.println(bigram);
        }
    }
}