package com.astarivi.kaizoyu.core.adapters;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;
import com.astarivi.kaizoyu.databinding.ItemAnimeBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.color.MaterialColors;

import org.tinylog.Logger;

import lombok.Setter;


public class AnimeViewHolder<A extends AnimeBasicInfo> extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final ItemAnimeBinding binding;
    protected final boolean canCheckFavorite;
    @Setter
    protected A anime;
    @Setter
    protected Consumer<A> itemClickListener;
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

    @Override
    public void onClick(View v) {
        if (itemClickListener == null) {
            Logger.debug("Listener for items at recycler was null.");
            return;
        }

        itemClickListener.accept(anime);
    }

    // region Favorite implementation
    public void checkFavorite() {
        if (!canCheckFavorite) return;

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SavedAnime savedAnime = SavedShowRepo.getAnimeDao().getByKitsuId(anime.getKitsuId());

            if (savedAnime == null || savedAnime.list == AnimeBasicInfo.LocalList.NOT_TRACKED.getValue()) {
                return;
            }

            Threading.runOnUiThread(() -> {
                //noinspection ConstantValue
                if (!canCheckFavorite) return;

                binding.getRoot().setCardBackgroundColor(favoriteColor);
                binding.episodeCardFavIc.setVisibility(View.VISIBLE);
            });
        });
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

        String coverUrl = anime.getOptimizedImageURLFor(AnimeBasicInfo.ImageType.COVER);
        String posterUrl = anime.getOptimizedImageURLFor(AnimeBasicInfo.ImageType.POSTER);

        if (posterUrl != null) {
            Glide.with(binding.getRoot().getContext())
                    .load(posterUrl)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (coverUrl != null || !(resource instanceof BitmapDrawable)) return false;

                            Palette.from(
                                    // Welcome to casting hell, again
                                    ((BitmapDrawable) resource).getBitmap()
                            ).generate(palette -> {
                                if (palette == null) return;

                                binding.getRoot().post(() -> {
                                    try {
                                        binding.imageCover.setImageDrawable(null);
                                        binding.imageCover.setBackgroundColor(palette.getMutedColor(
                                                Color.parseColor("#9240aa")
                                        ));
                                    } catch(Exception ignored) {
                                    }
                                });
                            });
                            return false;
                        }
                    })
                    .placeholder(R.drawable.ic_general_placeholder)
                    .into(binding.imagePoster);
        } else {
            binding.imagePoster.setImageResource(R.drawable.ic_general_placeholder);
        }

        if (coverUrl != null) {
            Glide.with(binding.getRoot().getContext())
                    .load(coverUrl)
                    // Fixes pixel level imperfections. This could be profiled to see if it's worth it.
                    // Profiled 2023-8-6, it's absolutely worth it.
                    .centerCrop()
                    .placeholder(R.drawable.ic_general_placeholder)
                    .into(binding.imageCover);
        } else {
            binding.imageCover.setImageResource(R.drawable.ic_general_placeholder);
        }
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
}
