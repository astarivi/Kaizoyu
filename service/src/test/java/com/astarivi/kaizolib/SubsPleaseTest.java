package com.astarivi.kaizolib;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.subsplease.SubsPlease;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseAnime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;


public class SubsPleaseTest {
    private static UserHttpClient userHttpClient;
    private static SubsPlease subsPlease;

    @BeforeAll
    static void setup() {
        userHttpClient = UserHttpClient.getInstance();
        subsPlease = new SubsPlease(userHttpClient);
    }
    @Test
    @DisplayName("SubsPlease DOW Schedule")
    void testSchedule() {
        TreeMap<DayOfWeek, List<SubsPleaseAnime>> result = subsPlease.getAiringAnime(TimeZone.getDefault());
        assertNotNull(result);
    }

    @Test
    @DisplayName("SubsPlease Today Schedule")
    void testTodaySchedule() {
        List<SubsPleaseAnime> result = subsPlease.getAnimeAiringToday(TimeZone.getDefault());
        assertNotNull(result);
    }

    @AfterAll
    static void finish() {
        userHttpClient.close();
        subsPlease = null;
    }
}
