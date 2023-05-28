import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Priority scheduling class.
 *
 * @author Jared Scarr
 */

public class Priority implements ScheduleInterface {
    public List<Process> allProcesses = new ArrayList<>();
    private PriorityQueue<Process> readyQ = this.getPriorityQueue(false);
    private final RR rr = new RR();
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
    private int algorithmTotalTime = 0;
    private boolean displayMode = false;
    // file to write output to if desired
    FileWriter outFile = null;
    private Process currentRunningProcess = null;

    private int cpuTime = 0;
    private int cpuIdleTime = 0;

    /**
     * Default constructor.
     */
    public Priority() {}

    /**
     * Constructor with option for ascending or descending priority.
     * @param desc - boolean flag.
     */
    public Priority(boolean desc) {
        readyQ = getPriorityQueue(desc);
    }

    /**
     * Constructor with list of Processes passed as a List.
     * @param procList - List of Process classes.
     */
    public Priority(List<Process> procList) {
        allProcesses.addAll(procList);
        readyQ.addAll(procList);
    }
    /**
     * Constructor with boolean flag and list of processes.
     * @param desc - boolean flag for asc or desc.
     * @param procList - List of Process classes.
     */
    public Priority(boolean desc, List<Process> procList) {
        allProcesses.addAll(procList);
        readyQ.addAll(procList);
    }

    /**
     * Add a process to the PriorityQueue.
     * @param proc - Process class.
     */
    public void addProcess(Process proc) {
        allProcesses.add(proc);
        readyQ.add(proc);
    }

    /**
     * Non-preemptive processing of the schedule.
     * @return - List of processes in the order that they were processed.
     */
    public List<Process> process() {
        while (processedList.size() != allProcesses.size()) {
            Process currProc = readyQ.poll();
            // Ready queue contains processes
            if (currProc != null) {
                currProc.setCurrentState(Process.State.RUNNING);
                int runDuration = currProc.getCurrentDuration();
                int tick = 0;
                while (tick < runDuration) {
                    // display logic
                    if (displayMode) {
                        currentRunningProcess = currProc;
                        displayState(true, false);
                    }
                    // end display logic
                    tick();
                    cpuTime += currProc.getCpuTime();
                    tick++;
                }
            } else {
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

            if (proc.getCurrentState() == Process.State.WAITING && !readyQ.contains(proc)) {
                // Add back to ready after IO burst completes
                Process firstIoCompletedProcess = ioQ.poll();
                // if there is a process in IO the first one in the queue will be the first to be ready to move
                if (firstIoCompletedProcess != null) {
                    readyQ.add(firstIoCompletedProcess);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                ((currentRunningProcess == null) ? "<none>" : currentRunningProcess.getName()) + ", duration: " +
                ((currentRunningProcess == null) ? "<none>" : currentRunningProcess.getCurrentDuration()));
        sb.append("......................................................." + "\n");
        sb.append("\n");
        sb.append("List of processes in the ready queue:" + "\n");
        sb.append("\n");
        sb.append("\t\tProcess\t\tBurst" + "\n");
        for (Process p : readyQ) {
            sb.append("\t\t\t" + p.getName() + "\t\t" + p.getCurrentDuration() + "\n");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDisplayMode() {
        return displayMode;
    }
    /**
     * Set display mode.
     *
     * @param displayMode - true to show data else false.
     */
    @Override
    public void setDisplayMode(boolean displayMode) {
        this.displayMode = displayMode;
        if (displayMode){
            try {
                outFile = new FileWriter(new File("Priority.txt"));
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Get the total number of clock ticks elapsed during the algorithm's run.
     *
     * @return
     */
    @Override
    public int getTotalElapsedTime() {
        return algorithmTotalTime;
    }

    /**
     * Get the total number of clock ticks when the CPU was idle during the algorithm's run.
     *
     * @return
     */
    @Override
    public int getTotalIdleCPUTime() {
        return cpuIdleTime;
    }

    /**
     * Return a PriorityQueue with ascending priority (default) or if desc flag
     * is true return a PriorityQueue with descending priority.
     * @param desc - boolean flag true for descending.
     * @return - PriorityQueue.
     */
    private PriorityQueue<Process> getPriorityQueue(boolean desc) {
        if (desc) {
            return new PriorityQueue<>(new Comparator<Process>() {
                public int compare(Process a, Process b) {
                    if (a.compareTo(b) > 0) {
                        return 1;
                    }
                    if (a.compareTo(b) < 0) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
        return new PriorityQueue<>();
    }
}
