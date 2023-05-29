import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Prints algorithm results to the console.
 * Each algorithm's display mode defaults to false.
 * To show individual algorithm results use method: setDisplayMode(true)
 */
public class Main {

    public static void main(String[] args) {
        // display mode set to true but defaults to false
        // can toggle these settings to decide what you want to display
        // this is also why it is not in a loop
        TestUtil util = new TestUtil();
        RR roundRobin = new RR(Arrays.stream(util.getDefaultTestData()).toList());
        // toggle display mode
        roundRobin.setDisplayMode(true);
        if (roundRobin.getDisplayMode()) {
            displayResults(roundRobin);
        }

        SJF shortyJobFirst = new SJF(util.getDefaultTestData());
        // toggle display mode
        shortyJobFirst.setDisplayMode(true);
        if (shortyJobFirst.getDisplayMode()) {
            displayResults(shortyJobFirst);
        }

        FCFS firstComeFirstServed = new FCFS(Arrays.stream(util.getDefaultTestData()).toList());
        // toggle display mode
        firstComeFirstServed.setDisplayMode(true);
        if (firstComeFirstServed.getDisplayMode()) {
            displayResults(firstComeFirstServed);
        }

        Priority priority = new Priority(Arrays.stream(util.getDefaultTestData()).toList());
        // toggle display mode
        priority.setDisplayMode(true);
        if (priority.getDisplayMode()) {
            displayResults(priority);
        }

        MLQ mlq = new MLQ(Arrays.stream(util.getDefaultTestData(),0,4)
                .toList(), Arrays.stream(util.getDefaultTestData(),4,8)
                .toList(), 4);
        // toggle display mode
        mlq.setDisplayMode(true);
        if (mlq.getDisplayMode()) {
            displayResults(mlq);
        }

        MLFQ mlfq = new MLFQ(Arrays.asList(util.getDefaultTestData()), 5, 10);
        // toggle display mode
        mlfq.setDisplayMode(true);
        if (mlfq.getDisplayMode()) {
            displayResults(mlfq);
        }
    }

    /**
     * Print averages to console.
     * @param algo - class that implements ScheduleInterface
     */
    public static void displayResults(ScheduleInterface algo) {
        int totalRT = 0, totalWT = 0, totalTT = 0, idleTime = 0, totalTime = 0;
        double size;

        List<Process> result = algo.process();
        ArrayList<String> actual = new ArrayList<>(result.size());
        size = Double.valueOf(result.size());

        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        System.out.println();
        System.out.println(algo.getClass().getName());
        System.out.println();

        for (Process proc : result) {
            totalRT += proc.getResponseTime();
            totalWT += proc.getWaitingTime();
            totalTT += proc.getTurnaroundTime();
            System.out.print(proc.getName() + " CPU: " + proc.getCpuTime() + " IO: " + proc.getIoTime() +
                    " TTr: " + proc.getTurnaroundTime() + " Tr: " + proc.getResponseTime() + " Tw: ");
            System.out.println(proc.getWaitingTime());
            actual.add(proc.getName());
        }
        idleTime = algo.getTotalIdleCPUTime();
        totalTime = algo.getTotalElapsedTime();
        System.out.println();
        System.out.printf("Tw (avg): %.1f TTr (avg): %.1f Tr (avg): %.1f%n", totalWT / size, totalTT / size, totalRT / size);
        System.out.print("Total Time: " + totalTime + " Idle Time: " + idleTime + " CPU Util: ");
        System.out.printf("%.1f", ((1.0 * totalTime - idleTime) / totalTime) * 100);
        System.out.println("%");
    }
}
