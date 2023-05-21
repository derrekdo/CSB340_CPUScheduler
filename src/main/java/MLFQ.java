import java.util.*;

public class MLFQ implements ScheduleInterface{
    //holds data for which queue a process belongs to
    private TreeMap<Process, Integer> currentPriority;
    //Ready queue for each algorithm
    private Queue<Process> highPriorityRQ;
    private Queue<Process> medPriorityRQ;
    private Queue<Process> lowPriorityRQ;
    //list to hold each queue
    private List<Queue<Process>> readyQueues = new ArrayList<>();
    //List of process in I/O
    private List<Process> inIO = new LinkedList<>();
    //List of process that have completed running
    private List<Process> completed;
    //List of all process
    private List<Process> allProcesses;
    //Time quantum for both the high and med priority queue
    private int highPriorityTQ;
    private int medPriorityTQ;
    //Number of processes
    private int size;
    //Data and results regarding the process runtime
    private int currentTime;
    private double cpuTime = 0;
    private double totWaitTime;
    private double totTurnaroundTime;
    private double totResponseTime;
    //process currently running
    private Process processOnCpu;
    //determines if data is displayed
    boolean displayMode;

    public MLFQ(List<Process> processes, int highPriorityTQ, int medPriorityTQ){
        highPriorityRQ = new LinkedList<>(processes);
        medPriorityRQ = new LinkedList<>();
        lowPriorityRQ = new LinkedList<>();
        readyQueues.add(highPriorityRQ); readyQueues.add(medPriorityRQ); readyQueues.add(lowPriorityRQ);
        this.highPriorityTQ = highPriorityTQ;
        this.medPriorityTQ = medPriorityTQ;
        allProcesses = new LinkedList<>(processes);
        size = processes.size();
        completed = new ArrayList<>(size);
        currentTime = 0;
        displayMode = false;
        currentPriority = new TreeMap<>();

        for (Process p : allProcesses) {
            currentPriority.put(p, 0);
        }
    }

    /**
     * Runs all process in the cpu
     * @return a list of all process in order they finished
     */
    public List<Process> process(){
        int burstDuration = 0;
        //Runs each process until all bursts are complete
        while (completed.size() != 8) {
            //if the high priority queue is not empty, run the first process in it
            if (!highPriorityRQ.isEmpty()) {
                processOnCpu = highPriorityRQ.poll();
            //if high priority queue is empty, run the first process in the next queue
            } else if (!medPriorityRQ.isEmpty()) {
                processOnCpu = medPriorityRQ.poll();
            //if the previous queues are empty, run the first process in the lowest priority queue
            } else if (!lowPriorityRQ.isEmpty()) {
                processOnCpu = lowPriorityRQ.poll();
            } else {
                //if all queues are empty, no process is running
                processOnCpu = null;
            }

            //if there is a process running, find which queue it is in
            //if it is either the high or medium priority queue, cpu time will be determined by lower value
            //between the processes burst duration and time quantum of the queue
            if (processOnCpu != null) {
                int priority = currentPriority.get(processOnCpu);
                switch (priority) {
                    case 0 : burstDuration = Math.min(processOnCpu.getCurrentDuration(), highPriorityTQ);
                             break;
                    case 1 : burstDuration = Math.min(processOnCpu.getCurrentDuration(), medPriorityTQ);
                             break;
                    case 2 : burstDuration = processOnCpu.getCurrentDuration();
                             break;
                }
                //sets process state to running
                processOnCpu.setCurrentState(Process.State.RUNNING);
            } else {
                //if there is no process running, the process with lowest remaining I/O time enters the ready queue
                burstDuration = inIO.get(0).nextBurstDuration();

                for (Process current : inIO) {
                    burstDuration = Math.min(burstDuration, current.nextBurstDuration());
                }
            }
            if (displayMode) {
                displayState(false);
            }
            tickProcess(burstDuration);
        }
        if (displayMode) {
            displayState(false);
        }
        return completed;
    }

    /**
     * Depending on the current state of the process, increment its wait time or cpu time, or decrements its I/O time
     * @param burstDuration
     */
    private void tickProcess(int burstDuration) {
        currentTime += burstDuration;
        int priority = 0;
        //ticks each process for the current burst duration
        while (burstDuration > 0) {
            //iterates throgh each process and uses the tick method on it
            for (Process current : allProcesses) {
                current.tick();
                //find which queue the process belongs
                //checks if the process is finished before doing so to prevent error
                if(current.getCurrentState() != Process.State.FINISHED){
                    priority = currentPriority.get(current);
                }

                //if the process is finsihed add to list of completed processes and record its data
                if (current.getCurrentState() == Process.State.FINISHED && !completed.contains(current)) {
                    completed.add(current);
                    totWaitTime += current.getWaitingTime();
                    totTurnaroundTime += current.getTurnaroundTime();
                    totResponseTime += current.getResponseTime();
                    cpuTime += current.getCpuTime();
                //if process enters a wait state or is already waiting, add it to the queue
                } else if (current.getCurrentState() == Process.State.WAITING
                        && !readyQueues.get(priority).contains(current)) {
                    readyQueues.get(priority).add(current);
                    inIO.remove(current);
                //if process enters I/O add it to the list of processes in I/O
                } else if (current.getCurrentState() == Process.State.IO && !inIO.contains(current)) {
                    inIO.add(current);
                    readyQueues.get(priority).remove(current);
                }
            }
            burstDuration--;
        }

        //If the process does not complete its cpu burst within the time quantum, preempt it and add to next lower queue
        if (processOnCpu != null && processOnCpu.getCurrentState() == Process.State.RUNNING) {
            priority = currentPriority.get(processOnCpu) + 1;
            processOnCpu.setCurrentState(Process.State.WAITING);
            currentPriority.put(processOnCpu, priority);
            readyQueues.get(priority).add(processOnCpu);
        }
    }

    @Override
    public void displayState(boolean waitBetweenPages) {
        System.out.println("\n\nCurrent Time: " + currentTime);
        System.out.print("Next Process on CPU: ");
        if (processOnCpu != null && processOnCpu.getCurrentState() == Process.State.RUNNING) {
            System.out.println(processOnCpu.getName() + " \nBurst Time: " + processOnCpu.getCurrentDuration());
            if (currentPriority.get(processOnCpu) == 0) {
                System.out.println("Time Quantum: " + highPriorityTQ);
            } else if (currentPriority.get(processOnCpu) == 1) {
                System.out.println("Time Quantum: " + medPriorityTQ);
            }
        } else {
            System.out.println("IDLE");
        }

        System.out.println("..................................................");
        System.out.println("\nList of processes in the ready queue:\n");
        System.out.println("\t\tProcess\t\tBurst\t\tQueue");
        int index = 0;
        int counter = 0;
        while (index < readyQueues.size()) {
            if (readyQueues.get(index).isEmpty()) {
                counter++;
            }
            if (counter == 3){
                System.out.println("\t\t[empty]");
            } else {
                for (Process p : readyQueues.get(index)) {
                    System.out.print("\t\t\t" + p.getName() + "\t\t\t" + p.getCurrentDuration());
                    System.out.println("\t\t\tQ" + currentPriority.get(p));
                }
            }
            index++;
        }

        System.out.println("..................................................");
        System.out.println("\nList of processes in I/O:\n");
        System.out.println("\t\tProcess\t\tRemaining I/O time");

        if (inIO.isEmpty()) {
            System.out.println("\t\t[empty]");
        } else {
            for (Process p : inIO) {
                System.out.println("\t\t\t" + p.getName() + "\t\t\t" + p.getCurrentDuration());
            }
        }

        System.out.println("..................................................");
        if (!completed.isEmpty()) {
            System.out.print("\nCompleted: ");
            for (Process p : completed) {
                System.out.print(p.getName() + " ");
            }
            System.out.println();
            System.out.println("..................................................");
        }
        System.out.println("..................................................");

        if (completed.size() == size) {
            Queue<Integer> waitTimes = new LinkedList<>();
            Queue<Integer> turnAroundTimes = new LinkedList<>();
            Queue<Integer> responseTimes = new LinkedList<>();

            System.out.println("\n\n");
            System.out.println("FINISHED\n");
            System.out.println("Total Time:\t\t\t" + currentTime);
            System.out.printf("CPU Utilization:\t%.4f", (cpuTime / currentTime) * 100 );
            System.out.println("%");
            String[] timeType = {"Waiting Times", "Turnaround Times", "Response Times"};

            for (int i = 0; i < timeType.length; i++) {
                System.out.print("\n" + timeType[i] + "\t");
                if (i != 1) {
                    System.out.print("\t");
                }

                for (Process process : allProcesses) {
                    System.out.print(process.getName() + "\t");
                    if (i == 0) {
                        waitTimes.add(process.getWaitingTime());
                        turnAroundTimes.add(process.getTurnaroundTime());
                        responseTimes.add(process.getResponseTime());
                    }
                }
                System.out.print("\n\t\t\t\t\t");
                if (i == 0) {
                    while (!waitTimes.isEmpty()) {
                        System.out.print(waitTimes.remove() + "\t");
                    }
                    System.out.printf("\nAverage Wait:\t\t%.2f %n", (totWaitTime / size));
                } else if (i == 1) {
                    while (!turnAroundTimes.isEmpty()) {
                        System.out.print(turnAroundTimes.remove() + "\t");
                    }
                    System.out.printf("\nAverage Turnaround: %.3f %n", (totTurnaroundTime / size));
                } else {
                    while (!responseTimes.isEmpty()) {
                        System.out.print(responseTimes.remove() + "\t");
                    }
                    System.out.printf("\nAverage Response:\t%.3f %n%n", (totResponseTime / size));
                }
            }
        }
        if (waitBetweenPages) {
            System.out.println("Press Enter to continue . . .");
            try {
                System.in.read();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public void setDisplayMode(boolean displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public int getTotalElapsedTime() {
        return currentTime;
    }

    @Override
    public int getTotalIdleCPUTime() {
        return currentTime - (int)cpuTime;
    }
}
