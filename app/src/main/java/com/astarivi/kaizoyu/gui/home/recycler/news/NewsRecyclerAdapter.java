package com.astarivi.kaizoyu.gui.home.recycler.news;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.rss.RssFetcher;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.ItemNewsBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.bumptech.glide.Glide;
import com.rometools.rome.feed.synd.SyndEntry;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.HomeNewsViewHolder>{
    private final ItemClickListener itemClickListener;
    private final List<SyndEntry> items = new ArrayList<>();

    public NewsRecyclerAdapter(ItemClickListener listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public HomeNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNewsBinding binding = ItemNewsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        binding.newsScrim.setBackgroundColor(
                Colors.getColorScrim(binding.getRoot(), R.attr.colorSurfaceVariant)
        );

        return new HomeNewsViewHolder(binding);
    }

    public void replaceData(List<SyndEntry> entries) {
        items.clear();
        items.addAll(entries);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull HomeNewsViewHolder holder, int position) {
        final SyndEntry entry = items.get(position);
        holder.setEntry(entry);

        holder.binding.newsTitle.setText(entry.getTitle());
        holder.binding.newsDescription.setText(
                entry.getDescription().getValue().replaceAll("\\s*</?[^>\\s]*>\\s*", "")
        );

        holder.fetchImage();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class HomeNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ItemNewsBinding binding;
        private SyndEntry entry;
        private Future<?> imageUrlFetcher;

        public HomeNewsViewHolder(@NonNull ItemNewsBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            this.binding = binding;
        }

        public void setEntry(SyndEntry entry) {
            this.entry = entry;
        }

        public void fetchImage() {
            if (entry == null) return;

            binding.newsBackground.setImageDrawable(null);

            if (imageUrlFetcher != null && !imageUrlFetcher.isDone()) {
                imageUrlFetcher.cancel(true);
            }

            imageUrlFetcher = Threading.submitTask(Threading.TASK.INSTANT, () -> {
                String url = RssFetcher.getThumbnailUrl(entry);

                if (url == null) return;

                binding.getRoot().post(() ->
                        Glide.with(binding.getRoot().getContext())
                                .load(url)
                                .centerCrop()
                                .into(binding.newsBackground)
                );
            });
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) {
                Logger.debug("Listener for items at home recycler was null.");
                return;
            }

            if (entry == null || entry.getLink() == null) {
                return;
            }

            itemClickListener.onItemClick(entry);
        }
    }

    public interface ItemClickListener {
        void onItemClick(SyndEntry article);
    }
}
