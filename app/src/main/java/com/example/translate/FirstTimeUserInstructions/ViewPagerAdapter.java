package com.example.translate.FirstTimeUserInstructions;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.translate.R;

public class ViewPagerAdapter extends PagerAdapter {

    Context context;
    int slidePos = 1;

    MediaItem[] mediaItems = {
            new MediaItem(R.raw.opening_page, MediaItem.MediaType.VIDEO),
            new MediaItem(R.raw.second_page, MediaItem.MediaType.VIDEO),
            new MediaItem(R.raw.third_page, MediaItem.MediaType.VIDEO),
            new MediaItem(R.raw.fourth_page, MediaItem.MediaType.VIDEO),
            new MediaItem(R.raw.fifth_page, MediaItem.MediaType.VIDEO),
    };


    int[] headings = {
            R.string.heading_1,
            R.string.heading_2,
            R.string.heading_3,
            R.string.heading_4,
            R.string.heading_5
    };

    int[] descriptions = {
            R.string.description_1,
            R.string.description_2,
            R.string.description_3,
            R.string.description_4,
            R.string.description_5
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return mediaItems.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView slideImageView = view.findViewById(R.id.imageView);
        VideoView slideVideoView = view.findViewById(R.id.videoView);
        TextView slideHeading = view.findViewById(R.id.textTitle);
        TextView slideDescription = view.findViewById(R.id.textDescription);

        MediaItem item = mediaItems[position];

        if (item.getType() == MediaItem.MediaType.IMAGE) {
            slideImageView.setImageResource(item.getResourceId());
            slideImageView.setVisibility(View.VISIBLE);
            slideVideoView.setVisibility(View.GONE);
        } else if (item.getType() == MediaItem.MediaType.VIDEO) {
            slideImageView.setVisibility(View.GONE);
            slideVideoView.setVisibility(View.VISIBLE);


            // Set the video URI using the resource ID
            Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + item.getResourceId());
            slideVideoView.setVideoURI(videoUri);
            slideVideoView.setZOrderOnTop(true);

            slideVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    slideVideoView.start();
                }
            });

            slideVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    slideVideoView.postDelayed(null, 1000);
                    slideVideoView.start();
                }
            });
        }

        slideHeading.setText(headings[position]);
        slideDescription.setText(descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
