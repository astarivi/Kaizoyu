package com.astarivi.kaizoyu.details;

import android.content.Context;

import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizoyu.core.adapters.HttpFileDownloader;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ImageSize;

import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;


public class DetailsUtils {
    @ThreadedOnly
    public static File downloadImage(Context context, Anime anime, boolean isCover) throws NetworkConnectionException, IOException {
        String url = anime.getImageUrlFromSizeWithFallback(ImageSize.ORIGINAL, isCover);

        if (url == null) {
            throw new IllegalArgumentException("Argument 'anime' contains no URL for this type");
        }

        final String[] filename = url.split("/");

        HttpFileDownloader downloader = new HttpFileDownloader(
                url,
                new File(context.getCacheDir(), filename[filename.length-1])
        );

        try {
            return downloader.download();
        } catch (IOException | NetworkConnectionException e) {
            Logger.error("Error downloading external thumbnail");
            Logger.error(e);
            throw e;
        }
    }
}
