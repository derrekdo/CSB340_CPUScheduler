import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * First Come, First Served scheduling class.
 *
 * @author Derrek Do
 */
public class FCFS implements ScheduleInterface{
    //Ready queue for each process
    private Queue<Process> inReadyQueue;
    //List of processes in IO
    private List<Process> inIO = new LinkedList<>();
    //list process that have completed all bursts
    private List<Process> completed;
    //List of all processes
    private List<Process> processes;
    //number of processes
    private int size;
    //Data and results regarding process runtimes
    private int currentTime;
    private double cpuTime = 0;
    private double totWaitTime;
    private double totTurnaroundTime;
    private double totResponseTime;
    //current process running
    private Process processOnCpu;
    boolean displayMode;
    // file to write output to if desired
    FileWriter outFile = null;

    public FCFS(List<Process> readyQueue) {
        inReadyQueue = new LinkedList<>(readyQueue);
        processes = new LinkedList<>(readyQueue);
        size = inReadyQueue.size();
        completed = new ArrayList<>(size);
        currentTime = 0;
        displayMode = false;
    }

    public List<Process> process() {
        int burstDuration = 0;
        //Runs each processes until they complete all bursts
        while (completed.size() != size) {
            //removes the first process from queue
            processOnCpu = inReadyQueue.poll();
            //checks if a process was removed
            if (processOnCpu != null) {
                //record its burst duration and set state to RUNNING
                burstDuration = processOnCpu.nextBurstDuration();
                processOnCpu.setCurrentState(Process.State.RUNNING);
            } else {
                //If there were no process to run, wait for process in I/O
                if (processOnCpu == null) {
                    burstDuration = inIO.get(0).nextBurstDuration();
                }
                //Finds the process with the lowest current I/O time
                for (Process process : inIO) {
                    burstDuration = Math.min(burstDuration, process.getCurrentDuration());
                }
            }
            //determines if the data will be shown
            if (displayMode) {
                displayState(true, false);
            }
            //Ticks each process base on the current CPU or I/O burst
            //updates run time,wait time, and I/O time based on each processes current state
            tickProcess(burstDuration);
        }
        if (displayMode) {
            displayState(true, false);
        }
        return completed;
    }

    public void tickProcess(int burstDuration) {
        //adds current burst duration to total time
        currentTime += burstDuration;
        //loop runs until the burst finishes
        while (burstDuration > 0) {
            //iterates through each process
            for (Process current : processes) {
                //runs the tick method on the current process
                current.tick();
                //if the current running process completes is CPU burst, send to I/O
                if (current.getCurrentState() == Process.State.IO && !inIO.contains(current)) {
                    inIO.add(current);
                    inReadyQueue.remove(current);
                    //If any process I/O burst finishes, send to ready queue
                } else if (current.getCurrentState() == Process.State.WAITING && !inReadyQueue.contains(current)) {
                    inReadyQueue.add(current);
                    inIO.remove(current);
                    //if the process finishes all bursts, add to list of completed processes, and record all data
                } else if (current.getCurrentState() == Process.State.FINISHED && !completed.contains(current)) {
                    completed.add(current);
                    totWaitTime += current.getWaitingTime();
                    totTurnaroundTime += current.getTurnaroundTime();
                    totResponseTime += current.getResponseTime();
                    cpuTime += current.getCpuTime();
                }
            }
            burstDuration--;
        }

    }

    /**
     * {@inheritDoc}
     */
    public void displayState(boolean writeToFile, boolean writeToScreen) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nCurrent Time: " + currentTime + "\n");
        sb.append("Next Process on CPU: ");
        if (processOnCpu != null && processOnCpu.getCurrentState() == Process.State.RUNNING) {
            sb.append(processOnCpu.getName() + " \nBurst Time: " + processOnCpu.getCurrentDuration() + "\n");
        } else {
            sb.append("IDLE" + "\n");
        }

        sb.append(".................................................." + "\n");
        sb.append("\nList of processes in the ready queue:\n" + "\n");
        sb.append("\t\tProcess\t\tBurst" + "\n");
        if (inReadyQueue.isEmpty()) {
            sb.append("\t\t[empty]" + "\n");
        } else {
            for (Process p : inReadyQueue) {
                sb.append("\t\t\t" + p.getName() + "\t\t\t" + p.getCurrentDuration() + "\n");
            }
            sb.append("\n");
        }
        sb.append(".................................................." + "\n");
        sb.append("\nList of processes in I/O:\n" + "\n");
        sb.append("\t\tProcess\t\tRemaining I/O time" + "\n");
        if (inIO.isEmpty()) {
            sb.append("\t\t[empty]" + "\n");
        } else {
            for (Process p : inIO) {
                sb.append("\t\t\t" + p.getName() + "\t\t\t" + p.getCurrentDuration() + "\n");
            }
        }
        sb.append(".................................................." + "\n");
        if (!completed.isEmpty()) {
            sb.append("\nCompleted: ");
            for (Process p : completed) {
                sb.append(p.getName() + " ");
            }
            sb.append("\n");
            sb.append(".................................................." + "\n");
        }
        sb.append(".................................................." + "\n");

        if (completed.size() == size) {
            Queue<Integer> waitTimes = new LinkedList<>();
            Queue<Integer> turnAroundTimes = new LinkedList<>();
            Queue<Integer> responseTimes = new LinkedList<>();

            sb.append("\n\n" + "\n");
            sb.append("FINISHED\n" + "\n");
            sb.append("Total Time:\t\t\t" + currentTime + "\n");
            sb.append(String.format("CPU Utilization:\t%.4f", (cpuTime / currentTime) * 100 ));
            sb.append("%" + "\n");
            String[] timeType = {"Waiting Times", "Turnaround Times", "Response Times"};

            for (int i = 0; i < timeType.length; i++) {
                sb.append("\n" + timeType[i] + "\t");
                if (i != 1) {
                    sb.append("\t");
                }

                for (Process process : processes) {
                    sb.append(process.getName() + "\t");
                    if (i == 0) {
                        waitTimes.add(process.getWaitingTime());
                        turnAroundTimes.add(process.getTurnaroundTime());
                        responseTimes.add(process.getResponseTime());
                    }
                }
                sb.append("\n\t\t\t\t\t");
                if (i == 0) {
                    while (!waitTimes.isEmpty()) {
                        sb.append(waitTimes.remove() + "\t");
                    }
                    sb.append(String.format("\nAverage Wait:\t\t%.2f %n", (totWaitTime / size)));
                } else if (i == 1) {
                    while (!turnAroundTimes.isEmpty()) {
                        sb.append(turnAroundTimes.remove() + "\t");
                    }
                    sb.append(String.format("\nAverage Turnaround: %.3f %n", (totTurnaroundTime / size)));
                } else {
                    while (!responseTimes.isEmpty()) {
                        sb.append(responseTimes.remove() + "\t");
                    }
                    sb.append(String.format("\nAverage Response:\t%.3f %n%n", (totResponseTime / size)));
                }
            }
        }
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
     * {@inheritDoc}
     */
    @Override
    public void setDisplayMode(boolean displayMode) {
        this.displayMode = displayMode;
        if (displayMode){
            try {
                outFile = new FileWriter(new File("data&charts/outputFiles/FCFS.txt"));
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalElapsedTime() {
        return currentTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalIdleCPUTime() {
        return currentTime - (int)cpuTime;
    }
}
