package com.astarivi.kaizolib.kitsuv2.model;

import java.util.List;

public record RawResults<T>(List<T> anime, long count) {
}
