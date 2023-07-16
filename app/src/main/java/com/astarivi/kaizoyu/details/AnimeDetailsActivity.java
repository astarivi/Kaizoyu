package com.astarivi.kaizoyu.details;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;

import com.astarivi.kaizolib.common.util.StringPair;
import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizolib.kitsu.exception.NoResultsException;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.GenericModalBottomSheet;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.core.models.base.ImageSize;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.schedule.AnimeScheduleChecker;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeDao;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.ActivityAnimeDetailsBinding;
import com.astarivi.kaizoyu.details.gui.AnimeEpisodesFragment;
import com.astarivi.kaizoyu.details.gui.AnimeInfoFragment;
import com.astarivi.kaizoyu.details.gui.adapters.DetailsTabAdapter;
import com.astarivi.kaizoyu.gui.adapters.BackInterceptAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;
import com.astarivi.zparc.Zparc;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.util.Objects;


public class AnimeDetailsActivity extends AppCompatActivityTheme {
    private ActivityAnimeDetailsBinding binding;
    private AnimeBase anime;
    private ModelType.Anime animeType;
    private SeenAnime seenAnime;
    private Zparc zparc;

    // The bundle must contain the following keys to create this Details Activity:
    // "type" as ModelType.Anime Enum value String representation
    // "anime" as the Anime parcelable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAnimeDetailsBinding.inflate(getLayoutInflater());
        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        String action = getIntent().getAction();
        Integer kitsuId = null;

        // Deep link
        if (action != null && action.equals("android.intent.action.VIEW")) {
            try {
                kitsuId = Integer.parseInt(
                        Objects.requireNonNull(
                                Objects.requireNonNull(
                                        getIntent().getData()
                                ).getLastPathSegment()
                        )
                );
            } catch(NullPointerException | NumberFormatException e) {
                Logger.error("Deep link was invalid {}", getIntent().getData());
                finish();
                return;
            }
        // Bundle
        } else if (bundle != null) {
            String type = bundle.getString("type");

            if (type == null || type.equals("")) {
                finish();
                return;
            }

            try {
                animeType = ModelType.Anime.valueOf(type);
            } catch(IllegalArgumentException e) {
                Logger.error("Invalid anime type {} for this bundle", type);
                finish();
                return;
            }

            anime = Utils.getAnimeFromBundle(bundle, animeType);

            if (anime == null) {
                Logger.error("Anime type {} couldn't be decoded from bundle", animeType);
                finish();
                return;
            }
        // Not valid
        } else {
            Logger.error("No valid build data was passed to this activity");
            finish();
            return;
        }

        // Data is valid, continue

        TabLayout tabLayout = binding.informationTabLayout;

        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("index");
            TabLayout.Tab tab = tabLayout.getTabAt(index);

            if (tab != null) {
                tab.select();
            }
        }

        getWindow().setStatusBarColor(
                Colors.getSemiTransparentStatusBar(
                        binding.getRoot(),
                        R.attr.colorSurface
                )
        );

        binding.internalToolbar.setBackground(
                Colors.fadeSurfaceFromStatusBar(
                        binding.getRoot(),
                        R.attr.colorSurface,
                        GradientDrawable.Orientation.TOP_BOTTOM
                )
        );

        binding.cancelButton.setOnClickListener(v -> finish());
        setLoadingScreen();

        // If this var isn't null, we are dealing with a deep link
        Integer finalKitsuId = kitsuId;
        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            final int currentShowId = finalKitsuId != null ? finalKitsuId : Integer.parseInt(anime.getKitsuAnime().id);

            SeasonalAnime seasonalAnime = AnimeScheduleChecker.getSeasonalAnime(
                    currentShowId
            );

            // Seasonal anime
            if (seasonalAnime != null) {
                anime = seasonalAnime;
                animeType = ModelType.Anime.SEASONAL;
                binding.getRoot().post(this::initializeFavorite);
                return;
            }

            // Local anime or deep link
            if (animeType == ModelType.Anime.LOCAL || finalKitsuId != null) {
                Kitsu kitsu = new Kitsu(
                        Data.getUserHttpClient()
                );

                KitsuAnime ktAnime = null;
                try {
                    ktAnime = kitsu.getAnimeById(
                            currentShowId
                    );
                } catch (NetworkConnectionException | NoResultsException e) {
                    if (finalKitsuId != null) {
                        Logger.error("No internet connection to initialize this deep link");
                        binding.getRoot().post(this::finish);
                        return;
                    }
                } catch (NoResponseException | ParsingException e) {
                    if (finalKitsuId != null) {
                        Logger.error("Failed to initialize this deep link, response couldn't be decoded");
                        Logger.error(e);
                        binding.getRoot().post(this::finish);
                        return;
                    }
                    Logger.error("Weird exception after trying to initialize a locally saved anime.");
                    Logger.error(e);
                    Logger.error("This incident has been reported to analytics.");
                    AnalyticsClient.onError("offline_anime_fetch", "Offline anime weird error", e);
                }

                if (ktAnime != null) {
                    anime = new Anime(ktAnime);
                    animeType = ModelType.Anime.BASE;
                }
            }

            binding.getRoot().post(this::initializeFavorite);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("index", binding.informationTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (zparc != null) zparc.stopAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (zparc != null) zparc.startAnimation();
    }

    public void triggerFavoriteRefresh() {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            int animeKtId = Integer.parseInt(anime.getKitsuAnime().id);
            SeenAnimeDao seenAnimeDao = Data.getRepositories().getSeenAnimeRepository().getAnimeDao();
            seenAnime = seenAnimeDao.getFromKitsuId(animeKtId);

            if (seenAnime != null && seenAnime.isFavorite())
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        });
    }

    private void initializeFavorite() {
        if (seenAnime != null) {
            if (seenAnime.isFavorite())
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite_active);
            continueInitialization();
            return;
        }

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            int animeKtId = Integer.parseInt(anime.getKitsuAnime().id);

            SeenAnimeDao seenAnimeDao = Data.getRepositories().getSeenAnimeRepository().getAnimeDao();
            seenAnime = seenAnimeDao.getFromKitsuId(animeKtId);

            if (seenAnime != null && seenAnime.isFavorite()) {
                binding.getRoot().post(() ->
                    binding.favoriteButton.setImageResource(R.drawable.ic_favorite_active)
                );
            }

            binding.getRoot().post(this::continueInitialization);
        });
    }

    private void continueInitialization() {
        binding.posterImage.setVisibility(View.VISIBLE);

        TabLayout tabLayout = binding.informationTabLayout;

        String coverUrl;
        String posterUrl;

        if (Utils.isDeviceLowSpec(this)) {
            coverUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.SMALL, true);
            posterUrl = anime.getImageUrlFromSize(ImageSize.TINY, false);
        } else {
            coverUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.ORIGINAL, true);
            posterUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.MEDIUM, false);
        }

        if (coverUrl != null)
            Glide.with(this)
                    .load(coverUrl)
                    .centerCrop()
                    .into(binding.coverImage);


        if (posterUrl != null)
            Glide.with(this)
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_general_placeholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (coverUrl != null || !(resource instanceof BitmapDrawable)) return false;

                            Palette.from(
                                // Welcome to casting hell
                                ((BitmapDrawable) resource).getBitmap()
                            ).generate(palette -> {
                                if (isFinishing() || isDestroyed() || palette == null) return;

                                binding.coverAnimation.post(() -> binding.coverAnimation.setVisibility(View.VISIBLE));

                                zparc = new Zparc.Builder(AnimeDetailsActivity.this)
                                        .setView(binding.coverAnimation)
                                        .setDuration(4000)
                                        .setAnimColors(
                                                palette.getDominantColor(
                                                        Color.parseColor("#363d80")
                                                ),
                                                palette.getMutedColor(
                                                        Color.parseColor("#9240aa")
                                                )
                                        )
                                        .build();

                                // Activity visible, start the animation
                                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                    zparc.startAnimation();
                                }
                            });

                            return false;
                        }
                    })
                    .into(binding.posterImage);

        configureTabAdapter(tabLayout);

        binding.animeTitle.setText(anime.getDisplayTitle());
        binding.animeTitle.setOnLongClickListener(v ->
            Utils.copyToClipboard(this, "Anime title", anime.getDisplayTitle())
        );

        binding.posterImage.setOnClickListener(v ->
            Threading.submitTask(Threading.TASK.INSTANT, () -> {

                File downloadedFile;
                try {
                    downloadedFile = DetailsUtils.downloadImage(this, (Anime) anime, false);
                } catch (Exception ignored) {
                    return;
                }

                binding.getRoot().post(() -> {
                    try {
                        startActivity(
                                new Intent(Intent.ACTION_VIEW)
                                        .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        .setDataAndType(
                                                FileProvider.getUriForFile(
                                                        AnimeDetailsActivity.this,
                                                        getString(R.string.provider_authority),
                                                        downloadedFile
                                                ),
                                                "image/*"
                                        )
                        );
                    } catch (Exception ignored) {
                    }
                });
            })
        );

        binding.coverImage.setOnClickListener(v ->
                Threading.submitTask(Threading.TASK.INSTANT, () -> {

                    File downloadedFile;
                    try {
                        downloadedFile = DetailsUtils.downloadImage(this, (Anime) anime, true);
                    } catch (Exception ignored) {
                        return;
                    }

                    binding.getRoot().post(() -> {
                        try {
                            startActivity(
                                    new Intent(Intent.ACTION_VIEW)
                                            .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .setDataAndType(
                                                    FileProvider.getUriForFile(
                                                            AnimeDetailsActivity.this,
                                                            getString(R.string.provider_authority),
                                                            downloadedFile
                                                    ),
                                                    "image/*"
                                            )
                            );
                        } catch (Exception ignored) {
                        }
                    });
                })
        );

        binding.collapsingBarChild.setTitle(
            anime.getDisplayTitle()
        );

        binding.issueTouchArea.setOnClickListener(v ->
            Toast.makeText(this, R.string.d_issue_unavailable, Toast.LENGTH_SHORT).show()
        );

        binding.shareTouchArea.setOnClickListener(v -> {
            GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                    getString(R.string.d_share_title),
                    new StringPair[]{
                            new StringPair(
                                    getString(R.string.d_share_kitsu),
                                    getString(R.string.d_share_kitsu_desc)
                            ),
                            new StringPair(
                                    getString(R.string.d_share_app),
                                    getString(R.string.d_share_app_desc)
                            )
                    },
                    index -> {
                        if (index == 0) {
                            new ShareCompat.IntentBuilder(this)
                                    .setType("text/plain")
                                    .setChooserTitle(R.string.d_share_kitsu)
                                    .setText(String.format("https://kitsu.io/anime/%s", anime.getKitsuAnime().id))
                                    .startChooser();
                        } else {
                            new ShareCompat.IntentBuilder(this)
                                    .setType("text/plain")
                                    .setChooserTitle(R.string.d_share_app)
                                    .setText(String.format("https://kaizoyu.ovh/app/show/%s", anime.getKitsuAnime().id))
                                    .startChooser();
                        }
                    }
            );

            modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
        });

        final String rating = anime.getKitsuAnime().attributes.averageRating;

        if (rating != null) {
            binding.animeRating.setText(rating);
        } else {
            binding.animeRating.setVisibility(View.GONE);
        }

        final String subType = anime.getKitsuAnime().attributes.subtype;

        if (subType != null) {
            binding.animeSubtype.setText(
                    Translation.getSubTypeTranslation(
                            anime.getKitsuAnime().attributes.subtype,
                            this
                    )
            );
        } else {
            binding.animeSubtype.setVisibility(View.GONE);
        }

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        // Favorite Button

        binding.favoriteTouchArea.setOnClickListener(v -> {
            binding.favoriteTouchArea.setEnabled(false);
            Threading.submitTask(Threading.TASK.DATABASE, () -> {
                SeenAnimeDao seenAnimeDao = Data.getRepositories().getSeenAnimeRepository().getAnimeDao();
                seenAnime = seenAnimeDao.getFromKitsuId(
                        Integer.parseInt(anime.getKitsuAnime().id)
                );

                if (seenAnime == null) {
                    seenAnime = new SeenAnime(
                            anime.toEmbeddedDatabaseObject(),
                            System.currentTimeMillis()
                    );

                    seenAnime.id = (int) seenAnimeDao.insert(
                            seenAnime
                    );
                }

                if (seenAnime.isFavorite()) {
                    Data.getRepositories()
                            .getFavoriteAnimeRepository()
                            .deleteFromRelated(seenAnime);
                    binding.getRoot().post(() ->
                            binding.favoriteButton.setImageResource(R.drawable.ic_favorite));
                } else {
                    Data.getRepositories()
                            .getFavoriteAnimeRepository()
                            .createFromRelated(seenAnime, System.currentTimeMillis());
                    binding.getRoot().post(() ->
                            binding.favoriteButton.setImageResource(R.drawable.ic_favorite_active));
                }
                binding.getRoot().post(() ->
                        binding.favoriteTouchArea.setEnabled(true)
                );
            });
        });

        binding.loadingScreen.setVisibility(View.GONE);
    }

    private void setLoadingScreen() {
        binding.posterImage.setVisibility(View.INVISIBLE);
        binding.loadingScreen.setVisibility(View.VISIBLE);
    }

    private void configureTabAdapter(TabLayout tabLayout){
        ViewPager2 viewPager = binding.informationViewPager;

        Bundle bundle = new Bundle();
        bundle.putParcelable("anime", ((Anime) anime));

        DetailsTabAdapter detailsTabAdapter = new DetailsTabAdapter(this, bundle);
        viewPager.setAdapter(detailsTabAdapter);

        final String[] tabTitles = new String[]{
                getString(R.string.activity_details_tab1),
                getString(R.string.activity_details_tab2)
        };

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int currentTabPosition = viewPager.getCurrentItem();
                int reselectedTabPosition = tab.getPosition();

                if (currentTabPosition != reselectedTabPosition) return;

                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + reselectedTabPosition);

                if (fragment instanceof AnimeEpisodesFragment) {
                    ((AnimeEpisodesFragment) fragment).scrollTop();
                    return;
                }

                if (fragment instanceof AnimeInfoFragment) {
                    ((AnimeInfoFragment) fragment).scrollTop();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        int position = binding.informationViewPager.getCurrentItem();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);

        if (fragment == null) {
            super.onBackPressed();
            return;
        }

        if (fragment instanceof BackInterceptAdapter && ((BackInterceptAdapter) fragment).shouldFragmentInterceptBack()) {
            return;
        }

        super.onBackPressed();
    }

    public void setCurrentFragment(int index) {
        binding.informationViewPager.setCurrentItem(index);
    }

    public void changePagerInteractivity(boolean value) {
        binding.informationViewPager.setUserInputEnabled(value);
    }
}