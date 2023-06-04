package com.astarivi.kaizoyu.core.adapters;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ImageSize;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.databinding.ItemAnimeBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;

import org.tinylog.Logger;


public class AnimeViewHolder<A extends Anime> extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final ItemAnimeBinding binding;
    protected final boolean canCheckFavorite;
    protected A anime;
    protected ItemClickListener<A> itemClickListener;
    private static int baseColor = Color.BLUE;
    private static int favoriteColor = Color.GREEN;

    public AnimeViewHolder(@NonNull ItemAnimeBinding binding, boolean canCheckFavorite) {
        super(binding.getRoot());
        this.binding = binding;

        binding.getRoot().setOnClickListener(this);
        this.canCheckFavorite = canCheckFavorite;

        if (baseColor == Color.BLUE)
            baseColor = MaterialColors.getColor(
                    binding.getRoot().getContext(),
                    R.attr.colorSecondaryContainer,
                    Color.BLUE
            );

        if (favoriteColor == Color.GREEN)
            favoriteColor = MaterialColors.getColor(
                    binding.getRoot().getContext(),
                    R.attr.colorPrimaryContainer,
                    Color.GREEN
            );
    }

    public void setItemClickListener(ItemClickListener<A> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener == null) {
            Logger.debug("Listener for items at recycler was null.");
            return;
        }

        itemClickListener.onItemClick(anime);
    }

    public void setAnime(A anime) {
        this.anime = anime;
    }

    // region Favorite implementation

    public void checkFavorite() {
        if (!canCheckFavorite) return;

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SeenAnime seenAnime = Data.getRepositories()
                    .getSeenAnimeRepository()
                    .getAnimeDao()
                    .getFromKitsuId(
                            Integer.parseInt(
                                    anime.getKitsuAnime().id
                            )
                    );

            if (seenAnime == null || !seenAnime.isFavorite()) {
                return;
            }

            Threading.runOnUiThread(this::markFavorite);
        });
    }

    public void markFavorite() {
        if (!canCheckFavorite) return;

        binding.getRoot().setCardBackgroundColor(favoriteColor);
        binding.episodeCardFavIc.setVisibility(View.VISIBLE);
    }

    public void removeFavorite() {
        binding.getRoot().setCardBackgroundColor(baseColor);
        binding.episodeCardFavIc.setVisibility(View.INVISIBLE);
    }

    public int getBaseColor() {
        return baseColor;
    }

    public int getFavoriteColor() {
        return favoriteColor;
    }

    // endregion

    // region Images implementation

    public void fetchImages() {
        // Would it even be possible for binding to be null?
        if (anime == null || binding == null) return;

        String coverUrl = anime.getImageUrlFromSize(ImageSize.TINY, true);
        String posterUrl = anime.getImageUrlFromSize(ImageSize.TINY, false);

        if (posterUrl == null) {
            // Nothing to display
            binding.imagePoster.setImageResource(R.drawable.ic_general_placeholder);
            binding.imageCover.setImageResource(R.drawable.ic_general_placeholder);
            return;
        }

        Glide.with(binding.getRoot().getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_general_placeholder)
                .into(binding.imagePoster);

        if (coverUrl == null) {
            binding.imageCover.setImageResource(R.drawable.ic_general_placeholder);
            return;
        }

        Glide.with(binding.getRoot().getContext())
                .load(coverUrl)
                // Fixes pixel level imperfections. This could be profiled to see if it's worth it.
                .centerCrop()
                .placeholder(R.drawable.ic_general_placeholder)
                .into(binding.imageCover);
    }

    public void unloadImages() {
        binding.imageCover.setImageDrawable(null);
        binding.imagePoster.setImageDrawable(null);

        Glide.with(binding.getRoot().getContext())
                .clear(binding.imageCover);

        Glide.with(binding.getRoot().getContext())
                .clear(binding.imagePoster);
    }

    // endregion

    public interface ItemClickListener<B> {
        void onItemClick(B anime);
    }
}
