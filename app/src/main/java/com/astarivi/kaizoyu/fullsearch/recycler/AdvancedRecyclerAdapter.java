package com.astarivi.kaizoyu.fullsearch.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.databinding.ItemAdvancedBinding;

import java.util.ArrayList;
import java.util.List;


public class AdvancedRecyclerAdapter extends RecyclerView.Adapter<AdvancedRecyclerAdapter.AdvancedResultViewHolder> {
    private final ArrayList<Result> items = new ArrayList<>();
    private final ItemClickListener itemClickListener;

    public AdvancedRecyclerAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AdvancedResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdvancedBinding binding = ItemAdvancedBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        final AdvancedResultViewHolder viewHolder = new AdvancedResultViewHolder(binding);
        viewHolder.setItemClickListener(itemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdvancedResultViewHolder holder, int position) {
        final Result result = getItemFromPosition(position);

        holder.setResult(result);
        holder.binding.title.setText(result.getCleanedFilename());
        holder.binding.botName.setText(result.getBotName());
        holder.binding.videoQuality.setText(result.getQuality().toString());
        holder.binding.videoExtension.setText(result.getFileExtension());
        holder.binding.videoSize.setText(result.getNiblResult().size);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private Result getItemFromPosition(int position) {
        return items.get(position);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void replaceData(List<Result> results) {
        items.clear();
        items.addAll(results);
    }

    public static class AdvancedResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ItemAdvancedBinding binding;
        private ItemClickListener itemClickListener;
        private Result result;

        public AdvancedResultViewHolder(@NonNull ItemAdvancedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener == null) return;

            itemClickListener.onItemClick(result);
        }

        public void setResult(Result result) {
            this.result = result;
        }
    }

    public interface ItemClickListener {
        void onItemClick(Result result);
    }
}
