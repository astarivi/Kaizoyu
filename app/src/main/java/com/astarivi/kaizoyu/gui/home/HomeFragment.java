package com.astarivi.kaizoyu.gui.home;

import android.content.Intent;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.astarivi.kaizolib.kitsu.KitsuSearchParams;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.databinding.FragmentHomeItemBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.fullsearch.FullSearchActivity;
import com.astarivi.kaizoyu.gui.home.recycler.HomeRecyclerAdapter;
import com.astarivi.kaizoyu.search.SearchActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Utils;


public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private HomeRecyclerAdapter.ItemClickListener listener;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean showAdvancedSearch = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("advanced_search", false);

        binding.mainSearchBar.getMenu().clear();

        if (!showAdvancedSearch) return;

        binding.mainSearchBar.inflateMenu(R.menu.search_bar_menu);
        binding.mainSearchBar.setOnMenuItemClickListener(item -> {
            // There's only one item, so no need to check. If another item is added, change this.
            if (getActivity() == null) return false;
            Intent intent = new Intent(requireActivity(), FullSearchActivity.class);
            intent.putExtra("openSearch", true);
            startActivity(intent);
            return true;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        TextView appTitle = binding.mainAppTitle;

        final Shader textShader = Utils.getBrandingTextShader(appTitle.getTextSize());

        appTitle.getPaint().setShader(textShader);

        binding.mainSearchBar.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(requireActivity(), SearchActivity.class);
            intent.putExtra("openSearch", true);
            startActivity(intent);
        });

        listener = anime -> {
            Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.BASE.name());
            startActivity(intent);
        };

        initializeAll();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }

    private void initializeAll() {
        inflateItemBinding(
                getResources().getString(R.string.popular_anime),
                new KitsuSearchParams().
                        setLimit(
                                15
                        ).
                        setCustomParameter(
                                "sort",
                                "popularityRank"
                        )
        );

        inflateItemBinding(
                getResources().getString(R.string.home_beloved),
                new KitsuSearchParams().
                        setLimit(
                                15
                        ).
                        setCustomParameter(
                                "sort",
                                "popularityRank"
                        ).
                        setCustomParameter(
                                "sort",
                                "-favoritesCount"
                        )
        );

        inflateItemBinding(
                getResources().getString(R.string.home_seinen),
                new KitsuSearchParams().
                        setLimit(
                                15
                        ).
                        setCustomParameter(
                                "filter[categories]",
                                "seinen"
                        ).
                        setCustomParameter(
                                "sort",
                                "popularityRank"
                        ).
                        setCustomParameter(
                                "sort",
                                "-favoritesCount"
                        )
        );
    }

    private void inflateItemBinding(String title, KitsuSearchParams params) {
        FragmentHomeItemBinding homeItemBinding = FragmentHomeItemBinding.inflate(
                getLayoutInflater(),
                binding.itemsLayout,
                false
        );

        homeItemBinding.homeItemsTitle.setText(title);

        viewModel.initializeItem(
                homeItemBinding,
                binding,
                params,
                listener
        );
    }
}