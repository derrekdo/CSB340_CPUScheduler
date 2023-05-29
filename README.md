# CPUScheduler Project

For Professor Eric Lloyd North Seattle College

Operating Systems (CSB340)


## Purpose
- Implement scheduling algorithms.
- Simulate the algorithms running and show snapshots of each state.
- Analyze their performance.

## Setup

- Build with gradle

## Run Tests

- Right click on test module and select the run tests option.

## Display to console

Each algorithm has a toggle in the code to display output.
In an effort to reduce the amount of scrolling the engineer may choose
which of the algorithms to display results for on each run. Each algorithm
prints a snapshot which can be a lot of output, so it is recommended to run
one at a time.

To set the toggle look for the code in Main that looks like the below snippet:

## Snapshot logs

Each algorithm will have a file of snapshots saved to [data&charts/outputFiles](data&charts/outputFiles)
when run with display mode on.

```java
// toggle display mode
roundRobin.setDisplayMode(false);
```
Pass _true_ to the method to display data to the console.

## Report

Information on algorithm implementation and analysis is found in the
[Report.md](Report.md) file.