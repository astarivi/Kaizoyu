package com.astarivi.kaizolib.subsplease.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsPleaseResult {
    public String tz;
    public SubsPleaseSchedule schedule;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubsPleaseSchedule {
        public List<SubsPleaseAnime> Monday;
        public List<SubsPleaseAnime> Tuesday;
        public List<SubsPleaseAnime> Wednesday;
        public List<SubsPleaseAnime> Thursday;
        public List<SubsPleaseAnime> Friday;
        public List<SubsPleaseAnime> Saturday;
        public List<SubsPleaseAnime> Sunday;

        @JsonIgnore
        public @Nullable List<SubsPleaseAnime> getDay(@NotNull DayOfWeek dow) {
            switch(dow) {
                case MONDAY:
                    return this.Monday;
                case TUESDAY:
                    return this.Tuesday;
                case WEDNESDAY:
                    return this.Wednesday;
                case THURSDAY:
                    return this.Thursday;
                case FRIDAY:
                    return this.Friday;
                case SATURDAY:
                    return this.Saturday;
                case SUNDAY:
                default:
                    return this.Sunday;
            }
        }
    }
}
