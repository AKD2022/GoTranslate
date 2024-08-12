package com.example.translate.FirstTimeUserInstructions;

public class MediaItem {
    public enum MediaType { IMAGE, VIDEO }

    private MediaType type;
    private final int resourceId; // Can be either image or video resource ID

    public MediaItem(int resourceId, MediaType type) {
        this.type = type;
        this.resourceId = resourceId;
    }

    public MediaType getType() {
        return type;
    }

    public int getResourceId() {
        return resourceId;
    }
}
