package my.first.messenger.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import my.first.messenger.activities.listeners.ImageGalleryListener;
import my.first.messenger.activities.models.Image;
import my.first.messenger.databinding.ItemContainerImageBinding;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<Image> images;
    private ImageGalleryListener imageGalleryListener;
    private Context context;

    public ImageAdapter(ArrayList<Image> images, ImageGalleryListener imageGalleryListener, Context context) {
        this.imageGalleryListener = imageGalleryListener;
        this.images = images;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerImageBinding itemContainerImageBinding = ItemContainerImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(itemContainerImageBinding);
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
        ItemContainerImageBinding binding;


        ViewHolder(ItemContainerImageBinding itemContainerImageBinding) {
            super(itemContainerImageBinding.getRoot());
            binding = itemContainerImageBinding;

        }
        void setImage(Image image, int position){
            Glide.with(context).load(image.uri).into(binding.listItemImage);
            binding.progressImage.setVisibility(View.GONE);
            binding.getRoot().setOnClickListener(v->{
                binding.delete.setVisibility(View.GONE);
                imageGalleryListener.onImageGalleryClick(image.name,position);
            });
            if(!image.name.equals("0")){
            binding.getRoot().setOnLongClickListener(v->{
                binding.delete.setVisibility(View.VISIBLE);
                binding.delete.setOnClickListener(u -> {
                        deleteAt(image.name, position);
                        imageGalleryListener.deleteImage(image.name);
                });

        return true;});
            }
    }
    }
    private void deleteAt(String name, int position){
        images.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, images.size());
    }

}



