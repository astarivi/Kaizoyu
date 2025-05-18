package com.astarivi.kaizoyu.gui.library.watching;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.ActivitySharedLibraryBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.library.watching.adapter.SharedLibraryPagingAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Utils;

import org.tinylog.Logger;

import java.util.Locale;


public class SharedLibraryActivity extends AppCompatActivityTheme {
    private ActivitySharedLibraryBinding binding;
    private SharedLibraryViewModel viewModel;
    private SharedLibraryPagingAdapter adapter;
    private AnimeBasicInfo.LocalList localAnimeType;

    public SharedLibraryActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySharedLibraryBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        viewModel = new ViewModelProvider(this).get(SharedLibraryViewModel.class);

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            finish();
            return;
        }

        final String type = bundle.getString("local_type");

        if (type == null || type.isEmpty()) {
            finish();
            return;
        }

        try {
            localAnimeType = AnimeBasicInfo.LocalList.valueOf(type);
        } catch(IllegalArgumentException e) {
            Logger.error("Invalid anime local type {}", type);
            finish();
            return;
        }

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        @StringRes int title = switch (localAnimeType) {
            case WATCH_LATER -> R.string.d_pending;
            case FINISHED -> R.string.d_watched_list;
            default -> R.string.d_favorite_list;
        };

        binding.internalToolbar.setTitle(String.format(Locale.US, "Kaizoyu: %s", getString(title)));

        WindowCompatUtils.setWindowFullScreen(getWindow());

        binding.statusBarScrim.setBackgroundColor(
                Colors.getSemiTransparentStatusBar(
                        binding.getRoot(),
                        R.attr.colorSurfaceVariant
                )
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.statusBarScrim,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = insets.top;
                    v.setLayoutParams(params);

                    return windowInsets;
                }
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.libraryContents,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    v.setPadding(
                            0,
                            (int) Utils.convertDpToPixel(4, this),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(4, this)
                    );

                    return windowInsets;
                }
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.internalToolbar,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    v.setPadding(0, insets.top, 0, 0);

                    return windowInsets;
                }
        );

        // RecyclerView
        RecyclerView recyclerView = binding.libraryContents;
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        adapter = new SharedLibraryPagingAdapter(anime -> {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", anime);
            intent.putExtra("type", anime.getType().name());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        adapter.addLoadStateListener(loadState -> {
            LoadState refresh = loadState.getRefresh();

            if (refresh instanceof LoadState.Loading) {
                binding.libraryContents.setVisibility(View.INVISIBLE);
                binding.emptyLibraryPopup.setVisibility(View.GONE);
                binding.loadingBar.setVisibility(View.VISIBLE);
            } else if (refresh instanceof LoadState.Error) {
                binding.libraryContents.setVisibility(View.INVISIBLE);
                binding.emptyLibraryPopup.setVisibility(View.GONE);
                binding.loadingBar.setVisibility(View.GONE);

                Utils.makeToastRegardless(
                        SharedLibraryActivity.this,
                        R.string.db_error,
                        Toast.LENGTH_LONG
                );
            } else if (refresh instanceof LoadState.NotLoading) {
                if (adapter.getItemCount() == 0) {
                    binding.libraryContents.setVisibility(View.INVISIBLE);
                    binding.emptyLibraryPopup.setVisibility(View.VISIBLE);
                    binding.loadingBar.setVisibility(View.GONE);
                } else {
                    binding.libraryContents.setVisibility(View.VISIBLE);
                    binding.emptyLibraryPopup.setVisibility(View.GONE);
                    binding.loadingBar.setVisibility(View.GONE);
                }
            }

            return kotlin.Unit.INSTANCE;
        });

        viewModel.getResults().observe(this, localAnime ->
                adapter.submitData(getLifecycle(), localAnime));

        viewModel.fetchFavorites(localAnimeType);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel == null || adapter == null || binding == null) return;

        checkForRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void checkForRefresh() {
        final Data.TemporarySwitches switches = Data.getTemporarySwitches();

        if (switches.isPendingFavoritesRefresh()) {
            switches.setPendingFavoritesRefresh(false);

            viewModel.fetchFavorites(localAnimeType);
        }
    }
}