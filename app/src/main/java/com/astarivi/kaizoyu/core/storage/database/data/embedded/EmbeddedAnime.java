package com.astarivi.kaizoyu.core.storage.database.data.embedded;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;


public class EmbeddedAnime {
    public int kitsuId;
    public String subtype;
    public String titleJp;
    public String titleEn;
    public String titleEnJp;
    public String synopsis;
    public String coverImgLink;
    public String posterImageLink;

    public EmbeddedAnime(int kitsuId, String subtype, String titleJp, String titleEn, String titleEnJp,
                         String synopsis, String coverImgLink, String posterImageLink) {
        this.kitsuId = kitsuId;
        this.subtype = subtype;
        this.titleJp = titleJp;
        this.titleEn = titleEn;
        this.titleEnJp = titleEnJp;
        this.synopsis = synopsis;
        this.coverImgLink = coverImgLink;
        this.posterImageLink = posterImageLink;
    }

    public KitsuAnime toKitsuAnime() {
        return new KitsuAnime.KitsuAnimeBuilder(
                Integer.toString(kitsuId)
        ).setSubtype(
                subtype
        ).setSynopsis(
                synopsis
        ).setTitles(
                titleJp,
                titleEn,
                titleEnJp
        ).setCoverImage(
                coverImgLink
        ).setPosterImage(
                posterImageLink
        ).build(
        );
    }
}
