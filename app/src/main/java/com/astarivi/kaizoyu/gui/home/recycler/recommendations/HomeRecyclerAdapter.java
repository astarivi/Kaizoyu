package com.astarivi.kaizoyu.gui.home.recycler.recommendations;

import static com.astarivi.kaizoyu.utils.MathUtils.lerp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.databinding.FragmentHomeAnimeBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;


public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.HomeAnimeViewHolder> {
    @Setter
    private Consumer<RemoteAnime> itemClickListener;
    private final List<RemoteAnime> items = new ArrayList<>();

    @NonNull
    @Override
    public HomeAnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentHomeAnimeBinding binding = FragmentHomeAnimeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new HomeAnimeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAnimeViewHolder holder, int position) {
        final RemoteAnime anime = getEpisodeFromPosition(position);
        holder.setAnime(anime);
        holder.binding.posterHomeTitle.setText(anime.getPreferredTitle());
        holder.fetchImages();

        holder.binding.getRoot().setOnMaskChangedListener(maskRect -> {
            holder.binding.posterHomeTitle.setTranslationX(maskRect.left);
            holder.binding.posterHomeTitle.setAlpha(lerp(1F, 0F, 0F, 200F, maskRect.left));

            float posterAlpha = lerp(1F, 0F, 40F, 100F, maskRect.left);
            holder.toggleCoverVisibility(posterAlpha > 0.1F);

            holder.binding.popupPoster.setTranslationX(maskRect.left);
            holder.binding.popupPoster.setAlpha(posterAlpha);
        });
    }

    @Override
    public void onViewRecycled(@NonNull HomeAnimeViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.get(holder.binding.getRoot().getContext()).clearMemory();
    }

    public void clear() {
        int totalSize = getItemCount();
        items.clear();
        notifyItemRangeChanged(0, totalSize);
    }

    public void replaceData(List<RemoteAnime> anime) {
        items.clear();
        items.addAll(anime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private RemoteAnime getEpisodeFromPosition(int position) {
        return items.get(position);
    }

    public class HomeAnimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final FragmentHomeAnimeBinding binding;
        private RemoteAnime anime;
        private boolean isCoverVisible = false;

        public HomeAnimeViewHolder(@NonNull FragmentHomeAnimeBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            this.binding = binding;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) {
                Logger.debug("Listener for items at home recycler was null.");
                return;
            }

            itemClickListener.accept(this.anime);
        }

        public void setAnime(RemoteAnime anime) {
            this.anime = anime;
            isCoverVisible = false;
        }

        public void toggleCoverVisibility(boolean shouldBeVisible) {
            if (shouldBeVisible) {
                if (isCoverVisible) return;
                isCoverVisible = true;
                Glide.with(binding.getRoot().getContext())
                        .load(anime.getImageURL(AnimeBasicInfo.ImageType.POSTER, AnimeBasicInfo.ImageSize.TINY))
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_general_placeholder)
                        .into(binding.popupPoster);
                return;
            }

            if (!isCoverVisible) return;
            isCoverVisible = false;
            binding.popupPoster.setImageDrawable(null);
            Glide.with(binding.getRoot().getContext())
                    .clear(binding.popupPoster);
        }

        public void fetchImages() {
            if (this.anime == null) return;

            String imageUrl;
            if (Data.isDeviceLowSpec()) {
                imageUrl = anime.getImageURL(AnimeBasicInfo.ImageType.COVER, AnimeBasicInfo.ImageSize.TINY);
            } else {
                imageUrl = anime.getImageURLorFallback(AnimeBasicInfo.ImageType.COVER, AnimeBasicInfo.ImageSize.MEDIUM);
            }

            if (imageUrl == null) {
                binding.animeCover.setImageResource(R.drawable.ic_general_placeholder);
                return;
            }

            Glide.get(binding.getRoot().getContext())
                            .setMemoryCategory(MemoryCategory.LOW);

            Glide.with(binding.getRoot().getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_general_placeholder)
                    .into(binding.animeCover);
        }
    }
}
