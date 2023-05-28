import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MLQTest {
    private final TestUtil testUtil = new TestUtil();

    @Test
    void testRuns() {
        int totalRT = 0, totalWT = 0, totalTT = 0;
        double size;

        Process[] testData = testUtil.getDefaultTestData();
        MLQ mlq = new MLQ(Arrays.stream(testData,0,4).toList(), Arrays.stream(testData,4,8).toList(), 4);
        mlq.setDisplayMode(true);
        List<Process> result = mlq.process();
        String[] expected = {"P1", "P3", "P2", "P4", "P8", "P6", "P7", "P5"};
        ArrayList<String> actual = new ArrayList<>(result.size());
        size = Double.valueOf(result.size());
        for (Process proc : result) {
            totalRT += proc.getResponseTime();
            totalWT += proc.getWaitingTime();
            totalTT += proc.getTurnaroundTime();
            System.out.print(proc.getName() + " CPU: " + proc.getCpuTime() + " IO: " + proc.getIoTime() +
                    " TTr: " + proc.getTurnaroundTime() + " Tr: " + proc.getResponseTime() + " Tw: ");
            System.out.println(proc.getWaitingTime());
            actual.add(proc.getName());
        }
        System.out.println();
        System.out.printf("Tw (avg): %.1f TTr (avg): %.1f Tr (avg): %.1f%n", totalWT / size, totalTT / size, totalRT / size);
        System.out.print("Total Time: " + mlq.timer + " Idle Time: " + mlq.idleCPUTime + " CPU Util: ");
        System.out.printf("%.1f", mlq.getCPUUtilization() * 100);
        System.out.println("%");
        assertArrayEquals(expected, actual.toArray());
    }
}