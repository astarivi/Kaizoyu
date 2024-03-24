package com.astarivi.kaizoyu.core.storage.database.data.embedded;

import com.astarivi.kaizolib.anilist.model.AniListAnime;


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

    public AniListAnime toAniListAnime() {
        AniListAnime aniListAnime = AniListAnime.withDefaults(kitsuId);

        aniListAnime.subtype = subtype;
        aniListAnime.description = synopsis;
        aniListAnime.title.english = titleEn;
        aniListAnime.title.japanese = titleJp;
        aniListAnime.title.romaji = titleEnJp;

        // The naming scheme may be confusing, as Kitsu was used here before
        aniListAnime.bannerImage = coverImgLink;
        aniListAnime.coverImage.medium = posterImageLink;

        return aniListAnime;
    }
}
