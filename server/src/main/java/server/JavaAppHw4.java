package server;

public class JavaAppHw4 {

    private static int num = 5;
    private static volatile int signal = 1;  // A ~ 1, B ~ 2, C ~ 3
    private static Object o = new Object();

    public static void main(String[] args) {

        // Thread A
        new Thread(() -> {
            try {
                for (int i=0; i < num; i++) {
                    synchronized (o) {
                        while (signal != 1) {
                            o.wait();
                        }

                        System.out.print("A");
                        signal = 2;
                        o.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread B
        new Thread(() -> {
            try {
                for (int i=0; i < num; i++) {
                    synchronized (o) {
                        while (signal != 2) {
                            o.wait();
                        }

                        System.out.print("B");
                        signal = 3;
                        o.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread C
        new Thread(() -> {
            try {
                for (int i=0; i < num; i++) {
                    synchronized (o) {
                        while (signal != 3) {
                            o.wait();
                        }

                        System.out.print("C");
                        signal = 1;
                        o.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

}
