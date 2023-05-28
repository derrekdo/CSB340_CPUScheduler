import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SJFTest {
    private final TestUtil testUtil = new TestUtil();

    @Test
    void testRuns() {
        int totalRT = 0, totalWT = 0, totalTT = 0;
        double size;

        Process[] testData = testUtil.getDefaultTestData();
        SJF sjf = new SJF(testData);
        sjf.setDisplayMode(true);
        List<Process> result = sjf.process();
        String[] expected = {"P1", "P6", "P8", "P7", "P2", "P4", "P5", "P3"};
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
        System.out.print("Total Time: " + sjf.timer + " Idle Time: " + sjf.idleCPUTime + " CPU Util: ");
        System.out.printf("%.1f", sjf.getCPUUtilization() * 100);
        System.out.println("%");
        assertArrayEquals(expected, actual.toArray());
    }
}