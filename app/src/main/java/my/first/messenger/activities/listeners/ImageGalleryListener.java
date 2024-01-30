package my.first.messenger.activities.listeners;

public interface ImageGalleryListener {
    void onImageGalleryClick(String url, int position);
    void deleteImage(String name);

}