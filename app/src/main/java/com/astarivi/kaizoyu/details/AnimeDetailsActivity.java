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

import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizolib.kitsu.exception.NoResultsException;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.modal.GenericModalBottomSheet;
import com.astarivi.kaizoyu.core.adapters.modal.ModalOption;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.ImageSize;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.schedule.AnimeScheduleChecker;
import com.astarivi.kaizoyu.core.search.AssistedResultSearcher;
import com.astarivi.kaizoyu.core.search.SearchEnhancer;
import com.astarivi.kaizoyu.core.storage.database.repositories.AnimeStorageRepository;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.util.Objects;


public class AnimeDetailsActivity extends AppCompatActivityTheme {
    private ActivityAnimeDetailsBinding binding;
    private Anime anime;
    private ModelType.Anime animeType;
    private ModelType.LocalAnime localType = null;
    private SearchEnhancer searchEnhancer = null;
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

            if (animeType == ModelType.Anime.LOCAL) {
                localType = ((LocalAnime) anime).getLocalAnimeType();
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
            localType = Data.getRepositories().getAnimeStorageRepository().getLocalType(anime);

            if (localType != null && localType != ModelType.LocalAnime.SEEN) {
                binding.getRoot().post(() ->
                    binding.favoriteButton.setImageResource(R.drawable.ic_details_added)
                );
            }
        });
    }

    private void initializeFavorite() {
        if (localType != null && localType != ModelType.LocalAnime.SEEN) {
            binding.favoriteButton.setImageResource(R.drawable.ic_details_added);
            getSearchEnhancer();
            return;
        }

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            localType = Data.getRepositories().getAnimeStorageRepository().getLocalType(anime);

            if (localType != null && localType != ModelType.LocalAnime.SEEN) {
                binding.getRoot().post(() ->
                    binding.favoriteButton.setImageResource(R.drawable.ic_details_added)
                );
            }

            getSearchEnhancer();
        });
    }

    private void getSearchEnhancer() {
        binding.getRoot().post(() ->
                binding.loadingHint.setText(R.string.d_search_enhancer)
        );

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            Logger.info("Reaching KaizoSearch for search enhancement...");

            searchEnhancer = AssistedResultSearcher.getSearchEnhancer(
                    Integer.parseInt(anime.getKitsuAnime().id)
            );

            Logger.info("Got search enhancer response {}", searchEnhancer);

            binding.getRoot().post(this::continueInitialization);
        });
    }

    private void continueInitialization() {
        if (isDestroyed() || isFinishing()) return;

        binding.posterImage.setVisibility(View.VISIBLE);

        TabLayout tabLayout = binding.informationTabLayout;

        String coverUrl;
        String posterUrl;

        if (Data.isDeviceLowSpec()) {
            coverUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.SMALL, true);
            posterUrl = anime.getImageUrlFromSize(ImageSize.TINY, false);
        } else {
            coverUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.ORIGINAL, true);
            posterUrl = anime.getImageUrlFromSizeWithFallback(ImageSize.ORIGINAL, false);
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
                    downloadedFile = DetailsUtils.downloadImage(this, anime, false);
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
                        downloadedFile = DetailsUtils.downloadImage(this, anime, true);
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
                    new ModalOption[]{
                            new ModalOption(
                                    getString(R.string.d_share_kitsu),
                                    getString(R.string.d_share_kitsu_desc)
                            ),
                            new ModalOption(
                                    getString(R.string.d_share_app),
                                    getString(R.string.d_share_app_desc)
                            )
                    },
                    (index, highlight) -> {
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

            GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                    getString(R.string.d_save_title),
                    new ModalOption[]{
                            new ModalOption(
                                    getString(R.string.d_pending),
                                    getString(R.string.d_pending_desc),
                                    localType != null && localType == ModelType.LocalAnime.PENDING
                            ),
                            new ModalOption(
                                    getString(R.string.d_favorite_list),
                                    getString(R.string.d_favorite_desc),
                                    localType != null && localType == ModelType.LocalAnime.FAVORITE
                            ),
                            new ModalOption(
                                    getString(R.string.d_watched_list),
                                    getString(R.string.d_watched_desc),
                                    localType != null && localType == ModelType.LocalAnime.WATCHED
                            )
                    },
                    (index, highlight) -> {
                        if (highlight) {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.d_remove_list)
                                    .setMessage(R.string.d_remove_desc)
                                    .setNegativeButton(R.string.d_remove_negative, null)
                                    .setPositiveButton(R.string.d_remove_positive, (dialog, which) ->
                                        Data.getRepositories().getAnimeStorageRepository().asyncDelete(anime, new AnimeStorageRepository.Callback() {
                                            @Override
                                            public void onFinished() {
                                                Toast.makeText(
                                                        AnimeDetailsActivity.this,
                                                        R.string.d_remove_toast_p,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                localType = null;
                                                binding.favoriteButton.setImageResource(R.drawable.ic_details_add);
                                                // TODO Maybe go back if we're in offline mode
                                                binding.favoriteTouchArea.setEnabled(true);
                                            }

                                            @Override
                                            public void onFailure() {
                                                Toast.makeText(
                                                        AnimeDetailsActivity.this,
                                                        R.string.d_remove_toast_n,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                binding.favoriteTouchArea.setEnabled(true);
                                            }
                                        })
                                    )
                                    .show();
                            return;
                        }

                        ModelType.LocalAnime newLocalType;

                        switch(index){
                            case 0:
                                newLocalType = ModelType.LocalAnime.PENDING;
                                break;
                            case 1:
                                newLocalType = ModelType.LocalAnime.FAVORITE;
                                break;
                            case 2:
                            default:
                                newLocalType = ModelType.LocalAnime.WATCHED;
                        }

                        Data.getRepositories().getAnimeStorageRepository().asyncCreateOrUpdate(anime, newLocalType, new AnimeStorageRepository.Callback() {
                            @Override
                            public void onFinished() {
                                Toast.makeText(
                                        AnimeDetailsActivity.this,
                                        R.string.d_general_toast_p,
                                        Toast.LENGTH_SHORT
                                ).show();
                                localType = newLocalType;
                                binding.favoriteButton.setImageResource(R.drawable.ic_details_added);
                                binding.favoriteTouchArea.setEnabled(true);
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(
                                        AnimeDetailsActivity.this,
                                        R.string.d_general_toast_n,
                                        Toast.LENGTH_SHORT
                                ).show();
                                binding.favoriteTouchArea.setEnabled(true);
                            }
                        });
                    }
            );

            modalDialog.setCancelListener(() -> binding.favoriteTouchArea.setEnabled(true));

            modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
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
        bundle.putParcelable("anime", anime);
        bundle.putParcelable("search_enhancer", searchEnhancer);

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