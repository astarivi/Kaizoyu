package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.astarivi.kaizolib.ann.ANN;
import com.astarivi.kaizolib.ann.model.ANNItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


public class RssTest {
    @Test
    @DisplayName("Rss ANN feed")
    void annFeed() throws Exception {
        List<ANNItem> items = ANN.getANNFeed();
        assertNotNull(items);
        assertTrue(items.size() > 1);
    }
}
