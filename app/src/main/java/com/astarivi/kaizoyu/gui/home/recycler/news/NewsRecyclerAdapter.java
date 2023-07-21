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
import com.bumptech.glide.Glide;
import com.rometools.rome.feed.synd.SyndEntry;

import org.tinylog.Logger;

import java.util.List;


public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.HomeNewsViewHolder>{
    private ItemClickListener itemClickListener;
    private List<SyndEntry> items;

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
    }

    @Override
    public void onBindViewHolder(@NonNull HomeNewsViewHolder holder, int position) {
        final SyndEntry entry = items.get(position);
        holder.setEntry(entry);

        holder.binding.newsTitle.setText(entry.getTitle());
        holder.binding.newsDescription.setText(entry.getDescription().getValue());

        holder.fetchImage();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class HomeNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ItemNewsBinding binding;
        private SyndEntry entry;

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

            String url = RssFetcher.getThumbnailUrl(entry);

            if (url == null) return;

            binding.newsBackground.setImageDrawable(null);

            Glide.with(binding.getRoot().getContext())
                    .load(url)
                    .centerCrop()
                    .into(binding.newsBackground);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) {
                Logger.debug("Listener for items at home recycler was null.");
                return;
            }

            itemClickListener.onItemClick(entry);
        }
    }

    public interface ItemClickListener {
        void onItemClick(SyndEntry anime);
    }
}
