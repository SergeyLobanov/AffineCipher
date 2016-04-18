package lab2;

import java.io.IOException;

import static lab1.Entropy.createTextFile;
import static lab1.Entropy.readFile;
import static lab2.Affine.*;

/**
 * Created by Сергей on 20.03.2016.
 */

public class AffineTest {

    public static void main(String[] args) throws IOException{
        //String cipherText = filtration(readFile("src/lab2/ciphertext/13.txt"));
        String cipherText = filtration(readFile("src/lab2/ciphertext/04.txt"));


        String res = deciphering(cipherText);
        //printFirstBigrams();
        System.out.println(res);

        //createTextFile("src/lab2/plaintext/plain13.txt", res);
        //createTextFile("src/lab2/plaintext/plain04.txt", res);
    }
}


