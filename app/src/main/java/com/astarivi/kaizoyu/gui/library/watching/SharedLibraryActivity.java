package com.astarivi.kaizoyu.gui.library.watching;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.ActivitySharedLibraryBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.library.watching.adapter.SharedLibraryRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Utils;

import org.tinylog.Logger;


public class SharedLibraryActivity extends AppCompatActivityTheme {
    private ActivitySharedLibraryBinding binding;
    private SharedLibraryViewModel viewModel;
    private SharedLibraryRecyclerAdapter adapter;
    private ModelType.LocalAnime localAnimeType;

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

        if (type == null || type.equals("")) {
            finish();
            return;
        }

        try {
            localAnimeType = ModelType.LocalAnime.valueOf(type);
        } catch(IllegalArgumentException e) {
            Logger.error("Invalid anime local type {}", type);
            finish();
            return;
        }

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        @StringRes int title;

        switch (localAnimeType) {
            case PENDING:
                title = R.string.d_pending;
                break;
            case WATCHED:
                title = R.string.d_watched_list;
                break;
            case FAVORITE:
            default:
                title = R.string.d_favorite_list;
        }

        binding.internalToolbar.setTitle(title);

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

        adapter = new SharedLibraryRecyclerAdapter(anime -> {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.LOCAL.name());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        viewModel.getAnimeList().observe(this, localAnime -> {
            if (localAnime == null) {
                binding.emptyLibraryPopup.setVisibility(View.VISIBLE);
                binding.loadingBar.setVisibility(View.GONE);
                binding.libraryContents.setVisibility(View.INVISIBLE);
                return;
            }

            manager.scrollToPosition(0);
            binding.loadingBar.setVisibility(View.GONE);
            binding.libraryContents.setVisibility(View.VISIBLE);

            adapter.replaceData(localAnime);
            adapter.notifyDataSetChanged();
        });

        viewModel.fetchFavorites(binding, localAnimeType);
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
        adapter.clear();
    }

    private void checkForRefresh() {
        final Data.TemporarySwitches switches = Data.getTemporarySwitches();

        if (switches.isPendingFavoritesRefresh()) {
            switches.setPendingFavoritesRefresh(false);

            viewModel.fetchFavorites(binding, localAnimeType);
        }
    }
}