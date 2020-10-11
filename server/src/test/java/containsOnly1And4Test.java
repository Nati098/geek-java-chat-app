import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.JavaAppHw6;

import java.util.stream.Stream;

public class containsOnly1And4Test {
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsOnly1And4Test(boolean res, int[] init) {
        Assertions.assertEquals(res, JavaAppHw6.containsOnly1And4(init));
    }

    public static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.arguments(true, new int[]{ 1, 1, 1, 4, 4, 1, 4, 4}),
                Arguments.arguments(false, new int[]{4, 4, 4, 4}),
                Arguments.arguments(false, new int[]{ 1, 1, 1}),
                Arguments.arguments(false, new int[]{1, 1, 1, 4, 4, 3, 4, 4})
        );
    }
}
