import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RRTest {
    private final TestUtil testUtil = new TestUtil();

    @Test
    void testRuns() {
        Process[] testData = testUtil.getDefaultTestData();
        RR rr = new RR(Arrays.stream(testData).toList());
        rr.setDisplayMode(true);
        List<Process> result = rr.process();
        String[] expected = {"P1", "P6", "P8", "P2", "P5", "P4", "P7", "P3"};
        ArrayList<String> actual = new ArrayList<>(8);
        for (Process proc : result) {
            actual.add(proc.getName());
        }
        assertArrayEquals(expected, actual.toArray());
    }
}