package server;

import java.util.Arrays;


public class JavaAppHw6 {

    public static void main(String[] args) {
//        System.out.println(containsOnly1And4(new int[]{ 1, 1, 1, 4, 4, 1, 4, 4}));
//        System.out.println(containsOnly1And4(new int[]{ }));
//        System.out.println(containsOnly1And4(new int[]{ 1, 1, 1, 1}));
//        System.out.println(containsOnly1And4(new int[]{ 1, 1, 1, 4, 4, 3, 4, 4}));
    }

    public static int[] getAllAfterLast4(int[] init) {

        for (int i=init.length-1; i >= 0; i--) {
            if (init[i] == 4) {
                return Arrays.copyOfRange(init, i+1, init.length);
            }
        }

        throw new RuntimeException("There is no 4");
    }

    public static boolean containsOnly1And4(int[] init) {
        boolean contains1 = false;
        boolean contains4 = false;

        for (int i : init) {
            if (i != 1 && i != 4) {
                return false;
            }

            if (i == 1) {
                contains1 = true;
            }

            if (i == 4) {
                contains4 = true;
            }

        }

        return contains1 && contains4;
    }
}
