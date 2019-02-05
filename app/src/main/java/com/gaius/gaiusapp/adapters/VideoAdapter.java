package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.classes.Video;

import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private Context mCtx;
    private List<Video> videosList;
    private SharedPreferences prefs;

    public VideoAdapter(Context mCtx, List<Video> videosList) {
        this.mCtx = mCtx;
        this.videosList = videosList;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.video_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Video video = videosList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (video.getAvatar().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.avatarView);
        }
        else{
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(prefs.getString("base_url", null) + video.getAvatar())
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.avatarView);
        }

        Glide.with(mCtx)
                .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + video.getThumbnail(), mCtx))
                .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                .into(holder.videoView.thumbImageView);
        holder.videoView.setUp(prefs.getString("base_url", null) + video.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);

        holder.textViewUpdateTime.setText(video.getUploadedSince());
        holder.textViewTitle.setText(video.getTitle());
        holder.textViewDescription.setText(video.getDescription());
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        CardView videoCard;
        TextView textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView;
        JzvdStd videoView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            videoView = itemView.findViewById(R.id.videoView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            videoCard =  itemView.findViewById(R.id.imageViewCardView);
        }
    }
}
