package com.astarivi.kaizoyu.gui.home.recycler.recommendations;

import static com.astarivi.kaizoyu.utils.MathUtils.lerp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ImageSize;
import com.astarivi.kaizoyu.databinding.FragmentHomeAnimeBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;


public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.HomeAnimeViewHolder> {
    private ItemClickListener itemClickListener;
    private final List<Anime> items = new ArrayList<>();

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
        final Anime anime = getEpisodeFromPosition(position);
        holder.setAnime(anime);
        holder.binding.posterHomeTitle.setText(anime.getDisplayTitle());
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

    public void replaceData(List<Anime> anime) {
        items.clear();
        items.addAll(anime);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private Anime getEpisodeFromPosition(int position) {
        return items.get(position);
    }

    public class HomeAnimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final FragmentHomeAnimeBinding binding;
        private Anime anime;
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

            itemClickListener.onItemClick(this.anime);
        }

        public void setAnime(Anime anime) {
            this.anime = anime;
            isCoverVisible = false;
        }

        public void toggleCoverVisibility(boolean shouldBeVisible) {
            if (shouldBeVisible) {
                if (isCoverVisible) return;
                isCoverVisible = true;
                Glide.with(binding.getRoot().getContext())
                        .load(anime.getImageUrlFromSize(ImageSize.TINY, false))
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

            Glide.get(binding.getRoot().getContext())
                            .setMemoryCategory(MemoryCategory.LOW);

            Glide.with(binding.getRoot().getContext())
                    .load(anime.getImageUrlFromSize(ImageSize.TINY, true))
                    .centerCrop()
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_general_placeholder)
                    .into(binding.animeCover);
        }
    }

    public interface ItemClickListener {
        void onItemClick(Anime anime);
    }
}
