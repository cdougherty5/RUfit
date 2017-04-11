package edu.rowanuniversity.rufit.rufitObjects;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Repressents a table of the user's target goals and progress made on those goals;
 * Created by catherine on 3/29/2017.
 */

public class Goal {

    private int runsPerWeekTarget;
    private int milesPerWeekTarget;
    private int daysUntilRace;
    private int runsPerWeekActual;
    private int milesPerWeekActual;
    private String dateOfRace;


    public Goal(int dpwValue, int mpwValue, String durValue) {
        runsPerWeekTarget = dpwValue;
        milesPerWeekTarget = mpwValue;
        dateOfRace = durValue;
        milesPerWeekActual = 0;
        runsPerWeekActual = 0;
    }

    public Goal() {
    }

    public void setMilesPerWeekActual(int milesPerWeekActual) { this.milesPerWeekActual = milesPerWeekActual;}

    public int getMilesPerWeekActual() {return milesPerWeekActual;}

    public int getRunsPerWeekActual() {return runsPerWeekActual;}

    public void setRunsPerWeekActual(int runsPerWeekActual) {this.runsPerWeekActual = runsPerWeekActual;}

    public int getDaysUntilRace() {
        calculateDays();
        return daysUntilRace;
    }

    public void setDaysUntilRace(String date) {
        dateOfRace = date;
        calculateDays();
    }

    public String getDateOfRace() { return dateOfRace;}

    public int getMilesPerWeekTarget() { return milesPerWeekTarget; }

    public void setMilesPerWeekTarget(int milesPerWeek) {
        this.milesPerWeekTarget = milesPerWeek;
    }

    public int getRunsPerWeekTarget() {return runsPerWeekTarget;    }

    public void setRunsPerWeekTarget(int runsPerWeek) {
        this.runsPerWeekTarget = runsPerWeek;
    }

    /**
     * Given today's date and date of event, calculates days remaining.
     */
    private void calculateDays () {
        if((dateOfRace != null) && !(dateOfRace.isEmpty())) {
            LocalDate date1 = LocalDate.parse(dateOfRace);
            LocalDate date2 = LocalDate.now();
            Days d = Days.daysBetween(date2, date1);
            daysUntilRace = d.getDays();
        } else {
            daysUntilRace = -1;
        }
    }
}