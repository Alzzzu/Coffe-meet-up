package my.first.messenger.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import my.first.messenger.activities.models.Image;
import my.first.messenger.databinding.ItemContainerDisplayImageBinding;

public class ImageDisplayAdapter extends RecyclerView.Adapter<ImageDisplayAdapter.ViewHolder> {
    private ArrayList<Image> images;
    private Context context;

    public ImageDisplayAdapter(ArrayList<Image> images, Context context) {
        this.images = images;
        this.context=context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerDisplayImageBinding itemContainerDisplayImageBinding = ItemContainerDisplayImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(itemContainerDisplayImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setImage(images.get(position), position);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerDisplayImageBinding binding;


        ViewHolder(ItemContainerDisplayImageBinding itemContainerDisplayImageBinding) {
            super(itemContainerDisplayImageBinding.getRoot());
            binding = itemContainerDisplayImageBinding;

        }
        void setImage(Image image, int position){
            Glide.with(context).load(image.uri).into(binding.listItemImage);
            binding.progressImage.setVisibility(View.GONE);

            }
        }
    }






