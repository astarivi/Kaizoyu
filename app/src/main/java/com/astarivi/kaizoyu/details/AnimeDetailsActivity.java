package com.astarivi.kaizoyu.details;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;

import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.modal.GenericModalBottomSheet;
import com.astarivi.kaizoyu.core.adapters.modal.ModalOption;
import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.core.models.anime.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.schedule.AssistedScheduleFetcher;
import com.astarivi.kaizoyu.core.search.AssistedResultSearcher;
import com.astarivi.kaizoyu.core.search.SearchEnhancer;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
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
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;


public class AnimeDetailsActivity extends AppCompatActivityTheme {
    private ActivityAnimeDetailsBinding binding;
    private AnimeBasicInfo anime;
    private AnimeBasicInfo.LocalList localList;
    private SearchEnhancer searchEnhancer = null;
    private Zparc zparc;

    // The bundle must contain the following keys to create this Details Activity:
    // "type" as ModelType.Anime Enum value String representation
    // "anime" as the Anime parcelable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // region GUI init
        binding = ActivityAnimeDetailsBinding.inflate(getLayoutInflater());
        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);
        setContentView(binding.getRoot());

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

        // endregion

        Bundle bundle = getIntent().getExtras();
        String action = getIntent().getAction();
        Long kitsuId = null;

        // Deep link
        if (action != null && action.equals("android.intent.action.VIEW")) {
            try {
                kitsuId = Long.parseLong(
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

            if (type == null || type.isEmpty()) {
                Logger.error("Missing type in init bundle");
                finish();
                return;
            }

            AnimeBasicInfo.AnimeType animeType;

            try {
                animeType = AnimeBasicInfo.AnimeType.valueOf(type);
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

        // If this var isn't null, we are dealing with a deep link
        Long finalKitsuId = kitsuId;
        Threading.instant(() -> {
            // Try to promote a LocalAnime model to a RemoteAnime.
            if (anime.getType() == AnimeBasicInfo.AnimeType.LOCAL) {
                LocalAnime la = (LocalAnime) anime;
                localList = la.getLocalList();

                try {
                    anime = AnimeMapper.remoteFromLocal(la);
                } catch(Exception e) {
                    Logger.error("Tried to fetch LocalAnime, error {}", e);
                }
            }

            // Fetch the deep link.
            if (finalKitsuId != null) {
                try {
                    anime = AnimeMapper.remoteFromKitsu(KitsuPublic.get(finalKitsuId));
                } catch (KitsuException | ParsingError e) {
                    Logger.error("No internet connection to initialize this deep link, or an error occurred");
                    Logger.error(e);
                    binding.getRoot().post(this::finish);
                    return;
                }

                // Check if this deep link is saved in internal DB.
                // TODO: Change this to a common mechanism
                localList = SavedShowRepo.getLocalListFrom(anime.getKitsuId());
            }

            // Try to promote to SeasonalAnime
            SeasonalAnime seasonalAnime = null;

            try {
                seasonalAnime = AssistedScheduleFetcher.getSingle(
                        anime
                );
            } catch (IOException e) {
                Toast.makeText(
                        this,
                        R.string.network_connection_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
//            catch (AniListException e) {
//                Toast.makeText(
//                        this,
//                        R.string.parsing_error,
//                        Toast.LENGTH_SHORT
//                ).show();
//            }

            // Seasonal anime
            if (seasonalAnime != null) {
                anime = seasonalAnime;
                binding.getRoot().post(this::initializeFavorite);
                return;
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
        Threading.database(() -> {
            localList = SavedShowRepo.getLocalListFrom(anime.getKitsuId());

            if (localList != null && localList != AnimeBasicInfo.LocalList.NOT_TRACKED) {
                binding.getRoot().post(() ->
                    binding.favoriteButton.setImageResource(R.drawable.ic_details_added)
                );
            }
        });
    }

    private void initializeFavorite() {
        if (localList != null && localList != AnimeBasicInfo.LocalList.NOT_TRACKED) {
            binding.favoriteButton.setImageResource(R.drawable.ic_details_added);
            getSearchEnhancer();
            return;
        }

        Threading.database(() -> {
            localList = SavedShowRepo.getLocalListFrom(anime.getKitsuId());

            if (localList != null && localList != AnimeBasicInfo.LocalList.NOT_TRACKED) {
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

        Threading.instant(() -> {
            Logger.info("Reaching kaizoyu.ddns.net for search enhancements...");

            searchEnhancer = null;
            searchEnhancer = AssistedResultSearcher.getSearchEnhancer(
                    anime.getKitsuId()
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
            coverUrl = anime.getImageURLorFallback(
                    AnimeBasicInfo.ImageType.COVER,
                    AnimeBasicInfo.ImageSize.SMALL
            );
            posterUrl = anime.getImageURL(
                    AnimeBasicInfo.ImageType.POSTER,
                    AnimeBasicInfo.ImageSize.TINY
            );
        } else {
            coverUrl = anime.getImageURLorFallback(
                    AnimeBasicInfo.ImageType.COVER,
                    AnimeBasicInfo.ImageSize.ORIGINAL
            );
            posterUrl = anime.getImageURLorFallback(
                    AnimeBasicInfo.ImageType.POSTER,
                    AnimeBasicInfo.ImageSize.ORIGINAL
            );
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
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            if (coverUrl != null || !(resource instanceof BitmapDrawable))
                                return false;

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

        binding.animeTitle.setText(anime.getPreferredTitle());
        binding.animeTitle.setOnLongClickListener(v ->
            Utils.copyToClipboard(this, "Anime title", anime.getPreferredTitle())
        );

        binding.posterImage.setOnClickListener(v ->
            Threading.submitTask(Threading.TASK.INSTANT, () -> {

                File downloadedFile;
                try {
                    downloadedFile = DetailsUtils.downloadImage(this, anime, AnimeBasicInfo.ImageType.POSTER);
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
                        downloadedFile = DetailsUtils.downloadImage(this, anime, AnimeBasicInfo.ImageType.COVER);
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
            anime.getPreferredTitle()
        );

        binding.shareTouchArea.setOnClickListener(v -> {
            GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                    getString(R.string.d_share_title),
                    new ModalOption[]{
                            new ModalOption(
                                    getString(R.string.d_share_kitsu),
                                    getString(R.string.d_share_kitsu_desc)
                            ),
//                            new ModalOption(
//                                    getString(R.string.d_share_app),
//                                    getString(R.string.d_share_app_desc)
//                            )
                    },
                    (index, highlight) -> {
                        if (index == 0) {
                            new ShareCompat.IntentBuilder(this)
                                    .setType("text/plain")
                                    .setChooserTitle(R.string.d_share_kitsu)
                                    .setText(String.format(Locale.UK, "https://kitsu.io/anime/%d", anime.getKitsuId()))
                                    .startChooser();
                        } else {
//                            new ShareCompat.IntentBuilder(this)
//                                    .setType("text/plain")
//                                    .setChooserTitle(R.string.d_share_app)
//                                    .setText(String.format(Locale.UK, "https://kaizoyu.ovh/app/show/%d", anime.getKitsuId()))
//                                    .startChooser();
                        }
                    }
            );

            modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
        });

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        final String subType = anime.getSubtype();

        if (subType != null) {
            binding.animeSubtype.setText(
                    Translation.getSubTypeTranslation(
                            subType,
                            this
                    )
            );
        } else {
            binding.animeSubtype.setVisibility(View.GONE);
        }

        if (anime instanceof RemoteAnime remoteAnime) {
            final String rating = remoteAnime.getInternal().attributes.averageRating;

            if (rating != null && !rating.isBlank()) {
                binding.animeRating.setText(rating);
            } else {
                binding.animeRating.setVisibility(View.GONE);
            }
        } else {
            binding.animeRating.setVisibility(View.GONE);
        }

        // Issue report
        binding.issueTouchArea.setOnClickListener(v -> {
            GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                    getString(R.string.d_issue_title),
                    new ModalOption[]{
                            new ModalOption(
                                    getString(R.string.d_issue_join_title),
                                    getString(R.string.d_issue_join_description),
                                    false
                            )
                    },
                    (index, highlight) ->
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/8YvvxbfqSG")))
            );

            modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
        });

        // Favorite Button
        binding.favoriteTouchArea.setOnClickListener(v -> {
            binding.favoriteTouchArea.setEnabled(false);

            GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                    getString(R.string.d_save_title),
                    new ModalOption[]{
                            new ModalOption(
                                    getString(R.string.d_pending),
                                    getString(R.string.d_pending_desc),
                                    localList == AnimeBasicInfo.LocalList.WATCH_LATER
                            ),
                            new ModalOption(
                                    getString(R.string.d_favorite_list),
                                    getString(R.string.d_favorite_desc),
                                    localList == AnimeBasicInfo.LocalList.WATCHING
                            ),
                            new ModalOption(
                                    getString(R.string.d_watched_list),
                                    getString(R.string.d_watched_desc),
                                    localList == AnimeBasicInfo.LocalList.FINISHED
                            )
                    },
                    (index, highlight) -> {
                        if (highlight) {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.d_remove_list)
                                    .setMessage(R.string.d_remove_desc)
                                    .setNegativeButton(R.string.d_remove_negative, null)
                                    .setPositiveButton(R.string.d_remove_positive, (dialog, which) ->
                                            SavedShowRepo.deleteAsync(anime, (result) -> {
                                                Toast.makeText(
                                                        AnimeDetailsActivity.this,
                                                        result ? R.string.d_remove_toast_p : R.string.d_remove_toast_n,
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                if (result){
                                                    localList = null;
                                                    binding.favoriteButton.setImageResource(R.drawable.ic_details_add);
                                                }

                                                binding.favoriteTouchArea.setEnabled(true);
                                            })
                                    )
                                    .show();
                            return;
                        }

                        AnimeBasicInfo.LocalList newLocalType = AnimeBasicInfo.LocalList.fromValue(index);

                        SavedShowRepo.createOrUpdateAsync(anime, newLocalType, (result) -> {
                            Toast.makeText(
                                    AnimeDetailsActivity.this,
                                    result ? R.string.d_general_toast_p : R.string.d_general_toast_n,
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (result) {
                                localList = newLocalType;
                                binding.favoriteButton.setImageResource(R.drawable.ic_details_added);
                            }

                            binding.favoriteTouchArea.setEnabled(true);
                        });
                    }
            );

            modalDialog.setCancelListener(() -> binding.favoriteTouchArea.setEnabled(true));

            modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int position = binding.informationViewPager.getCurrentItem();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);

                if (fragment instanceof BackInterceptAdapter) {
                    if (((BackInterceptAdapter) fragment).shouldFragmentInterceptBack()) {
                        return;
                    }
                }

                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
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
        bundle.putString("type", anime.getType().name());
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

    public void setCurrentFragment(int index) {
        binding.informationViewPager.setCurrentItem(index);
    }

    public void changePagerInteractivity(boolean value) {
        binding.informationViewPager.setUserInputEnabled(value);
    }
}