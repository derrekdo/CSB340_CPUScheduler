import java.util.List;

/**
 * Interface for scheduling algorithms.
 *
 * @author Jared Scarr, Mike Murphy
 */
public interface ScheduleInterface {
    /**
     * Execute the schedule.
     * @return - list of completed processes.
     */
    public List<Process> process();

    /**
     * Display the snapshots of each process state.
     * @param waitBetweenPages - boolean to wait for command line input or not.
     */
    public void displayState(boolean waitBetweenPages);

    /**
     * Return the current display mode.
     * @return - boolean.
     */
    public boolean getDisplayMode();
    /**
     * Set display mode.
     * @param displayMode - true to show data else false.
     */
    public void setDisplayMode(boolean displayMode);

    /**
     * Get the total number of clock ticks elapsed during the algorithm's run.
     * @return
     */
    public int getTotalElapsedTime();

    /**
     * Get the total number of clock ticks when the CPU was idle during the algorithm's run.
     * @return
     */
    public int getTotalIdleCPUTime();
}
