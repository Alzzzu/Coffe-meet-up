package my.first.messenger.activities.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import my.first.messenger.R;
import my.first.messenger.activities.listeners.ChatMessageListener;
import my.first.messenger.activities.listeners.ImageGalleryListener;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.models.Image;
import my.first.messenger.activities.models.User;
import my.first.messenger.databinding.ItemContainerDisplayImageBinding;
import my.first.messenger.databinding.ItemContainerImageBinding;
import my.first.messenger.databinding.ItemContainerReceivedMessageBinding;
import my.first.messenger.databinding.ItemContainerSentMessageBinding;
import my.first.messenger.databinding.ItemContainerUserBinding;

public class ImageDisplayAdapter extends RecyclerView.Adapter<ImageDisplayAdapter.ViewHolder> {
    private ArrayList<Image> images;
  //  private ImageGalleryListener imageGalleryListener;
    private Context context;

    public ImageDisplayAdapter(ArrayList<Image> images, Context context) {
        //this.imageGalleryListener = imageGalleryListener;
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






