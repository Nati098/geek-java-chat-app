import org.junit.jupiter.api.Test;
import server.JavaAppHw6;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class getAllAfterLast4Test {

    @Test
    public void testGetAllAfterLast4_1() {
        int[] init = new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7 };
        int[] res = new int[]{1, 7 };

        assertArrayEquals(res, JavaAppHw6.getAllAfterLast4(init));
    }

    @Test
    public void testGetAllAfterLast4_2() {
        int[] init = new int[]{1, 2, 1, 7};

        assertThrows(RuntimeException.class, () -> JavaAppHw6.getAllAfterLast4(init));
    }

}
