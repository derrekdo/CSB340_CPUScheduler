import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Round Robin Scheduling algorithm.
 *
 * @author Jared Scarr
 */
public class RR implements ScheduleInterface {
    public List<Process> allProcesses = new ArrayList<>();
    private final Queue<Process> readyQ = new LinkedList<>();
    // priority queue smallest duration is the highest priority
    private final Queue<Process> ioQ = new PriorityQueue<>(new Comparator<Process>() {
        public int compare(Process a, Process b) {
            if (a.getCurrentDuration() >= b.getCurrentDuration()) {
                return 1;
            }
            if (a.getCurrentDuration() < b.getCurrentDuration()) {
                return -1;
            }
            return 0;
        }
    });
    private final Set<Process> processedList  = new LinkedHashSet<>();
    private final int timeQuantum;
    private int algorithmTotalTime = 0;
    private Process currentRunningProcess = null;
    private boolean displayMode = false;
    // file to write output to if desired
    FileWriter outFile = null;

    private int cpuTime = 0;
    private int cpuIdleTime = 0;

    /**
     * Default constructor.
     */
    public RR() {
        timeQuantum = 5;
    }

    /**
     * Constructor with List as param.
     * @param toProcessList - List of Processes.
     */
    public RR(List<Process> toProcessList) {
        allProcesses = toProcessList;
        readyQ.addAll(toProcessList);
        timeQuantum = 5;
    }

    /**
     * Constructor with quantum as parameter.
     * @param quantumLength - integer representing the quantum duration.
     */
    public RR(int quantumLength) {
        timeQuantum = quantumLength;
    }

    /**
     * Constructor with both time quantum and list passed.
     * @param quantumLength - int time quantum duration.
     * @param toProcessList - List of Processes.
     */
    public RR(int quantumLength, List<Process> toProcessList) {
        allProcesses = toProcessList;
        readyQ.addAll(toProcessList);
        timeQuantum = quantumLength;
    }

    /**
     * Process the schedule given the current state of the ready queue.
     * @return - list of completed processes in order of completion.
     */
    public List<Process> process() {
        while (processedList.size() != allProcesses.size()) {
            Process currProc = readyQ.poll();
            // Ready queue contains processes
            if (currProc != null) {
                currProc.setCurrentState(Process.State.RUNNING);
                // Run process for entire burst or quantum whichever is shortest
                int runDuration = Math.min(currProc.nextBurstDuration(), timeQuantum);
                int tick = 0;
                while (tick < runDuration) {
                    // Display logic
                    if (displayMode) {
                        currentRunningProcess = currProc;
                        displayState(true, false);
                    }
                    // end display logic
                    tick();
                    cpuTime += currProc.getCpuTime();
                    tick++;
                }
                // if it is still in running state it didn't complete it's burst then set to waiting
                // and move back into ready queue
                if (currProc.getCurrentState() == Process.State.RUNNING) {
                    currProc.setCurrentState(Process.State.WAITING);
                    readyQ.add(currProc);
                }
            } else { // Nothing in readyQ everything out for IO in ioQ or complete
                // nothing on readyQ so track cpu idle time
                cpuIdleTime++;
                tick();
            }
        }
        // last snapshot
        if (displayMode) {
            currentRunningProcess = null;
            displayState(true, false);
        }
        // end snapshot logic
        return processedList.stream().toList();
    }

    /**
     * Run updates on all processes, check their states after each tick,
     * and move/remove them from the appropriate lists and queues.
     */
    private void tick() {
        algorithmTotalTime++;
        for (Process proc : allProcesses) {
            proc.tick();
            if (proc.isFinished()) {
                proc.setCurrentState(Process.State.FINISHED);
                readyQ.remove(proc);
                ioQ.remove(proc);
                processedList.add(proc);
            }
            // only proc to IO queue if in the correct state AND isn't already there
            // otherwise it will have repeat processes in the ioQ
            if (proc.getCurrentState() == Process.State.IO && !ioQ.contains(proc)) {
                // The CPU burst has completed so send to IO
                readyQ.remove(proc);
                ioQ.add(proc);
            }

            // If the process has been set to waiting since the last tick then it can be moved
            // to the ready queue if it is not already there.
            if (proc.getCurrentState() == Process.State.WAITING && !readyQ.contains(proc)) {
                Process firstIoCompletedProcess = ioQ.poll();
                // if there is a process in IO the first one in the queue will be the first to be ready to move
                if (firstIoCompletedProcess != null) {
                    readyQ.add(firstIoCompletedProcess);
                }
            }
        }
    }

    /**
     * Add Process to queue.
     * @param proc - Process.
     */
    public void addProcess(Process proc) {
        readyQ.add(proc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getDisplayMode() {
        return displayMode;
    }

    /**
     * Set boolean variable for the display mode.
     * @param displayMode - boolean default true.
     */
    public void setDisplayMode(boolean displayMode) {
        this.displayMode = displayMode;
        if (displayMode){
            try {
                outFile = new FileWriter(new File("RR.txt"));
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    public int getTotalIdleCPUTime() {
        return cpuIdleTime;
    }

    /**
     * {@inheritDoc}
     */
    public int getTotalElapsedTime() {
        return algorithmTotalTime;
    }


    /**
     * {@inheritDoc}
     */
    public void displayState(boolean writeToFile, boolean writeToScreen) {
        StringBuilder sb = new StringBuilder();
        sb.append("Process burst reference:" + "\n");
        for (Process p : allProcesses) {
            sb.append(p.toString() + "\n");
        }
        sb.append("......................................................." + "\n");
        sb.append("Current Time: " + algorithmTotalTime + "\n");
        sb.append("\n");
        System.out.println("Next process on CPU: " +
                (currentRunningProcess == null ? "<none>" : currentRunningProcess.getName()) + ", duration: " +
                (currentRunningProcess == null ? "<none>" : currentRunningProcess.getCurrentDuration()));
        sb.append("......................................................." + "\n");
        sb.append("\n");
        sb.append("List of processes in the ready queue:" + "\n");
        sb.append("\n");
        sb.append("\t\tProcess\t\tBurst" + "\n");
        for (Process p : readyQ) {
            System.out.println("\t\t\t" + p.getName() + "\t\t" +
                    ((Integer)p.getCurrentDuration() == null ? "<none>" : p.getCurrentDuration()));
        }
        sb.append("\n");
        sb.append("......................................................." + "\n");
        sb.append("List of processes in I/O:" + "\n");
        sb.append("\n");
        sb.append("\t\tProcess\tRemaining I/O time" + "\n");
        for (Process p : ioQ) {
            sb.append("\t\t\t" + p.getName() + "\t\t" + p.getCurrentDuration() + "\n");
        }
        sb.append("......................................................." + "\n");
        sb.append("\n");
        sb.append("Finished processes: ");
        for (Process p : processedList) sb.append(p.getName() + " ");
        sb.append("\n");
        sb.append(":::::::::::::::::::::::::::::::::::::::::::::::::::::::" + "\n");
        sb.append("\n");
        sb.append("\n");
        if (writeToScreen) System.out.println(sb.toString());
        if (writeToFile) {
            try {
                outFile.write(sb.toString());
                outFile.flush();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
     }
}
