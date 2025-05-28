package com.astarivi.kaizolib.kitsuv2.private_api;

import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.model.KitsuListEntry;
import com.astarivi.kaizolib.kitsuv2.model.KitsuUser;
import com.astarivi.kaizolib.kitsuv2.model.RawResults;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KitsuPrivate extends Methods {
    @NotNull
    public static KitsuUser user() throws KitsuException, ParsingError {
        return KitsuUser.deserialize(
                executeGetWithCredentials(SELF_USER_ENDPOINT)
        );
    }

    @NotNull
    public static List<KitsuListEntry> list(@NotNull LibraryParams libraryParams) throws KitsuException, ParsingError {
        return KitsuListEntry.deserialize(
                executeGetWithCredentials(
                        libraryParams.buildURI()
                )
        );
    }

    @NotNull
    public static RawResults<KitsuListEntry> rawList(@NotNull LibraryParams libraryParams) throws KitsuException, ParsingError {
        return KitsuListEntry.deserializeRaw(
                executeGetWithCredentials(
                        libraryParams.buildURI()
                )
        );
    }
}
