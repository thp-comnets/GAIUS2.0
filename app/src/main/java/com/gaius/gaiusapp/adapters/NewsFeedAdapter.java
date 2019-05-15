package com.gaius.gaiusapp.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Priority;
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.BrowseWebFragment;
import com.gaius.gaiusapp.CreatePageActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAMLActivity;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.interfaces.OnAdapterInteractionListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.TopCropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.event.OnSlideClickListener;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.newsFeedViewHolder> {
    private Context mCtx;
    private List<NewsFeed> newsFeedList;
    private SharedPreferences prefs;
    private float scale;
    private Integer requestType;
    OnAdapterInteractionListener mAdapterListener;

    String audioUrl;
    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // gets audio duration

    private final Handler handler = new Handler();

    /**
     * help to toggle between play and pause.
     */
    private boolean playPause = false;
    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */
    private boolean intialStage = true;
    int tempPosition;
    private int playingPosition;
    private Handler uiUpdateHandler;
    private static final int MSG_UPDATE_SEEK_BAR = 1845;
    newsFeedViewHolder playingHolder;

    private SeekBarUpdater seekBarUpdater;
    private int seeked_progess;


    public NewsFeedAdapter(Context mCtx, List<NewsFeed> newsFeedList, int requestType, OnAdapterInteractionListener mListener) {
        this.mCtx = mCtx;
        this.newsFeedList = newsFeedList;
        this.requestType = requestType;
        this.mAdapterListener = mListener;
        this.playingPosition = -1;
        seekBarUpdater = new SeekBarUpdater();

//        uiUpdateHandler = new Handler(this);
//        mediaPlayer = new MediaPlayer(); // commented out for testing by thulasi
//       mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // commented out for testing by thulasi
    }

    @Override
    public newsFeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.newsfeed_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        scale = mCtx.getResources().getDisplayMetrics().density;
        return new newsFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final newsFeedViewHolder holder, final int position) {
        final NewsFeed newsfeed = newsFeedList.get(position);


        if (position == playingPosition) {
            playingHolder = holder;
            updatePlayingView();
        } else {
            updateNonPlayingView(holder);
        }


        if (newsfeed.getShowAvatar() == true) {
            if (newsfeed.getAvatar().contains("None")) {
                holder.avatarView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_avatar));
            }
            else{
                GlideApp.with(mCtx)
                        .load(prefs.getString("base_url", null) + newsfeed.getAvatar())
                        .avatar()
                        .into(holder.avatarView);
            }
        }
        else {
            holder.avatarView.setVisibility(View.GONE);
            holder.textViewName.setVisibility(View.GONE);
        }

        holder.avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarPopup((ImageView) v);
            }
        });

        String fidelity = prefs.getString("fidelity_level", "high");

        if (newsfeed.getType().equals("page") || newsfeed.getType().equals("channel")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.GONE);
            holder.audioView.setVisibility(View.GONE);

            holder.imageView.setTag(R.id.imageView, position); //we need the key here, otherwise Glide will complain
            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this page on GAIUS");
                }
            });

            GlideApp.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
                    .priority(Priority.LOW)
                    .content()
//                    .transition(withCrossFade())
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsFeed n = newsFeedList.get((Integer) v.getTag(R.id.imageView));
                    Bundle bundle = new Bundle();

                    // we distinguish here between simple pages and channels
                    if (n.getType().equals("channel")) {
                        // this is a channel, inflate the browse fragme
                        Fragment fragment = new BrowseWebFragment();
                        bundle.putString("userID", n.getUserID());
                        fragment.setArguments(bundle);

                        ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commit();
                    } else {
                        // this is a simple page, render it
                        Intent i = new Intent(mCtx, RenderMAMLActivity.class);
                        bundle.putSerializable("BASEURL", prefs.getString("base_url", null));
                        bundle.putSerializable("URL", n.getUrl());
                        bundle.putString("title", n.getTitle());
                        i.putExtras(bundle);
                        mCtx.startActivity(i);
                    }
                }
            });
        }
        else if (newsfeed.getType().equals("video")) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.GONE);
            holder.audioView.setVisibility(View.GONE);

            GlideApp.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
                    .priority(Priority.LOW)
                    .content()
                    .into(holder.videoView.thumbImageView);

            holder.videoView.setUp(prefs.getString("base_url", null) + newsfeed.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this video on GAIUS");
                }
            });
        }else if(newsfeed.getType().equals("audio")){

            if (position == playingPosition) {
                playingHolder = holder;
                updatePlayingView();
            } else {
                updateNonPlayingView(holder);
            }

            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.GONE);
            holder.audioView.setVisibility(View.VISIBLE);

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this audio on GAIUS");
                }
            });

        }
        else if (newsfeed.getType().equals("image") || newsfeed.getType().equals("ad")) {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.VISIBLE);
            holder.audioView.setVisibility(View.GONE);
            holder.setIsRecyclable(false);

//            HashMap<String,String> url_maps = new HashMap<String, String>();
            holder.multiImageViewBitmaps = newsfeed.getImagesGallery();
            holder.slider.setAdapter(new SliderAdapter() {
                @Override
                public int getItemCount() {
                    return holder.multiImageViewBitmaps.size();
                }

                @Override
                public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
                    imageSlideViewHolder.bindImageSlide(holder.multiImageViewBitmaps.get(position));
                    imageSlideViewHolder.imageView.setMaxHeight((int) (220 * scale));
                }
            });

            holder.slider.setSelectedSlide(0);
//            holder.slider.setInterval(2000);
            holder.slider.setTag(position);
            holder.slider.setOnSlideClickListener(new OnSlideClickListener() {
                @Override
                public void onSlideClick(int position) {
                    Bundle bundle = new Bundle();
                    Intent i = new Intent(mCtx, AlbumViewActivity.class);
                    bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
                    NewsFeed n = newsFeedList.get((Integer) holder.slider.getTag());
                    bundle.putString("name", n.getName());
                    bundle.putString("description", n.getDescription());
                    bundle.putString("uploadtime", n.getUpdateTime());
                    Log.d("thp", "load album " + n.getAvatar());
                    bundle.putString("avatar", n.getAvatar());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
            });

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this image album on GAIUS");
                }
            });
        }



        holder.textViewName.setText(newsfeed.getName());
        holder.textViewUpdateTime.setText(newsfeed.getUpdateTime());
        holder.textViewTitle.setText(newsfeed.getTitle());

        holder.textViewDescription.setText(newsfeed.getDescription());

        holder.textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterListener.onAdapterInteraction((Integer) v.getTag());
            }
        });

        if (newsfeed.getLiked().equals("true")) {
            holder.likeButton.setImageResource(R.drawable.icon_liked);
        }

        holder.textViewName.setTag(position); //set the tag to handle clicks properly

        holder.likeButton.setTag(position);
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("yasir", "clicked on liked ");

                NewsFeed n = newsFeedList.get((Integer) v.getTag());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

                String url = prefs.getString("base_url", null) + "like.py?url=" + n.getUrl() + "&token=" + prefs.getString("token", "null");
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.contains("Success") && response.contains("true")) {
//                                    mLikes.setText((Integer.parseInt(mLikes.getText().toString()) + 1) + "");
                                    holder.likeButton.setImageResource(R.drawable.icon_liked);
                                }
                                else if (response.contains("Success") && response.contains("false")) {
//                                    mLikes.setText((Integer.parseInt(mLikes.getText().toString()) - 1) + "");
                                    holder.likeButton.setImageResource(R.drawable.icon_like);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Yasir","Error "+error);
                            }
                        });

                Volley.newRequestQueue(mCtx).add(request);
            }
        });

        // this is only shown if we are in My Content
        if (requestType.equals(Constants.REQUEST_TYPE_MYOWN)) {
            holder.textViewStatus.setVisibility(View.VISIBLE);
            holder.editButton.setTag(position);
            holder.deleteButton.setTag(position);

            switch (newsfeed.getPublished()) {
                case "0":
                    holder.textViewStatus.setText("Saved");
                    holder.textViewStatus.setTextColor(mCtx.getResources().getColor(R.color.blue_500));
                    break;
                case "1":
                    holder.textViewStatus.setText("Published");
                    holder.textViewStatus.setTextColor(mCtx.getResources().getColor(R.color.green_500));
                    break;
                case "-1":
                    holder.textViewStatus.setText("Pending approval");
                    holder.textViewStatus.setTextColor(mCtx.getResources().getColor(R.color.orange_500));
                    break;
            }

            holder.shareButton.setVisibility(View.GONE);

            holder.editDeleteLayout.setVisibility(View.VISIBLE);

            //don't show description for ads since its the adCampaign
            if (newsfeed.getType().equals("ad")) {
                holder.textViewDescription.setVisibility(View.GONE);
                holder.adStatsLayout.setVisibility(View.VISIBLE);
                holder.textViewAdViewed.setText(newsfeed.getViewed());
                holder.textViewAdLiked.setText(newsfeed.getLiked());
            }

            //only pages can be edited right now
            if (newsfeed.getType().equals("page")) {
                holder.editButton.setVisibility(View.VISIBLE);
                holder.editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsFeed n = newsFeedList.get((Integer) v.getTag());

                        Intent intent = new Intent(mCtx, CreatePageActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PAGE_URL", n.getUrl());
                        bundle.putSerializable("EDIT_MODE", true);
                        bundle.putSerializable("PAGE_NAME", n.getTitle());
                        bundle.putSerializable("PAGE_DESCRIPTION", n.getDescription());
                        intent.putExtras(bundle);

                        mCtx.startActivity(intent);
                    }
                });
            }

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final NewsFeed n = newsFeedList.get((Integer) v.getTag());

                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                    builder.setTitle("Delete Content");
                    builder.setMessage("Do you want to delete this "+n.getType()+"?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            String URL;

                            // Do nothing but close the dialog
                            dialog.dismiss();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                            String token = prefs.getString("token", "null");

                            if (n.getType().equals("ad")) {
                                //use the description as adCampaign
                                URL = prefs.getString("base_url", null) + "deleteContent.py?token=" + token + "&" + n.getType() + "=" + n.getDescription();
                            } else {
                                URL = prefs.getString("base_url", null) + "deleteContent.py?token=" + token + "&" + n.getType() + "=" + n.getUrl();
                            }

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.d("yasir", response);

                                            if (response.contains("Success")) {
                                                newsFeedList.remove(n);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d("Yasir","Error "+error);
                                        }
                                    });
                            Log.d("Yasir","added request "+stringRequest);

                            Volley.newRequestQueue(mCtx).add(stringRequest);
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    public void shareItem (View v, String shareSub) {
        String shareBody;
        NewsFeed n = newsFeedList.get((Integer) v.getTag());
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        if (n.getType().equals("channel")) {
            shareBody = "http://gaiusnetworks.com/channel/"+n.getUserID();
        }
        else {
            shareBody = "http://gaiusnetworks.com/"+n.getUrl().replace("./","");
        }

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mCtx.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    public   void stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }



    private class SeekBarUpdater implements Runnable {
        @Override
        public void run() {
            if (null != mediaPlayer && playingHolder.getAdapterPosition() == playingPosition) {
                playingHolder.seekBarProgress.setMax(mediaPlayer.getDuration());
                playingHolder.seekBarProgress.setProgress(mediaPlayer.getCurrentPosition());
                playingHolder.seekBarProgress.postDelayed(this, 100);
            } else {
                playingHolder.seekBarProgress.removeCallbacks(seekBarUpdater);
            }

           /* if (null != playingHolder) {
                playingHolder.seekBarProgress.setProgress(mediaPlayer.getCurrentPosition());
                playingHolder.seekBarProgress.postDelayed(this, 100);
            }*/
        }
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    public void updateItemFriendsStatus(Integer position, Integer friendStatus) {
        newsFeedList.get(position).setFriendStatus(friendStatus);
    }

    private void showAvatarPopup(ImageView avatarView) {

        float scale = mCtx.getResources().getDisplayMetrics().density;

        Dialog builder = new Dialog(mCtx);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView imageView = new ImageView(mCtx);
        imageView.setImageDrawable(avatarView.getDrawable());

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                (int) (300 * scale),
                (int) (300 * scale));
        builder.addContentView(imageView, relativeParams);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams wmlp = builder.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP;
        wmlp.y = (int) (150 * scale);

        builder.show();
    }

    public NewsFeed getItem(int pos) {
        return newsFeedList.get(pos);
    }





//    @Override
//    public void onSliderClick(BaseSliderView slider) {
//        Bundle bundle = new Bundle();
////        bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
//        Intent i = new Intent(mCtx, RenderPhotoActivity.class);
//        i.putExtras(bundle);
//
//        mCtx.startActivity(i);
//    }

    public class newsFeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout editDeleteLayout, adStatsLayout;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription, textViewStatus, textViewAdLiked, textViewAdViewed;
        ImageView avatarView, likeButton, shareButton, editButton, deleteButton;
        TopCropImageView imageView;
        JzvdStd videoView;
        Slider slider;
        LinearLayout audioView;
        FloatingActionButton buttonPlayPause;
        SeekBar seekBarProgress;



        ArrayList<String> multiImageViewBitmaps;

        public newsFeedViewHolder(View itemView) {
            super(itemView);

            editDeleteLayout = itemView.findViewById(R.id.editDeleteLayout);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            audioView = itemView.findViewById(R.id.audioView);
            buttonPlayPause = itemView.findViewById(R.id.ButtonTestPlayPause);
            seekBarProgress = itemView.findViewById(R.id.SeekBarTestPlay);
            seekBarProgress.setMax(99);
//            mDemoSlider = itemView.findViewById(R.id.slider);
            slider = itemView.findViewById(R.id.slider);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewStatus =  itemView.findViewById(R.id.testViewStatus);
            likeButton = itemView.findViewById(R.id.like);
            shareButton = itemView.findViewById(R.id.share);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
            adStatsLayout = itemView.findViewById(R.id.adStats);
            textViewAdLiked = itemView.findViewById(R.id.textViewAdLiked);
            textViewAdViewed = itemView.findViewById(R.id.textViewAdViewed);

            buttonPlayPause.setOnClickListener(this);
            seekBarProgress.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if(getAdapterPosition() == playingPosition){

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }else{

                playingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder);
                    }
                    mediaPlayer.release();
                }
                playingHolder = this;

                if(newsFeedList.get(playingPosition).getType().equals("audio")){
                    Uri myUri = Uri.parse(prefs.getString("base_url", null)+newsFeedList.get(playingPosition).getUrl());
                    startMediaPlayer(myUri);
                }

            }

            updatePlayingView();
        }


    }


/*
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        releaseMediaPlayer();
    }*/

    // method to update audio seekerbar
    private void primarySeekBarProgressUpdater(final NewsFeedAdapter.newsFeedViewHolder holder,final int position) {
        holder.seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater(holder,position);
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }

    // method to update audio seekerbar
    private void primarySeekBarProgressUpdater1(final NewsFeedAdapter.newsFeedViewHolder holder) {
        holder.seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater1(holder);
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }

    private void updatePlayingView() {
        playingHolder.seekBarProgress.setMax(mediaPlayer.getDuration());
         playingHolder.seekBarProgress.setProgress(mediaPlayer.getCurrentPosition());

//        playingHolder.seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100));
        playingHolder.seekBarProgress.setEnabled(true);

        if (mediaPlayer.isPlaying()) {
            playingHolder.seekBarProgress.postDelayed(seekBarUpdater, 100);
            playingHolder.buttonPlayPause.setImageResource(R.drawable.ic_media_pause);
        } else {
            playingHolder.seekBarProgress.removeCallbacks(seekBarUpdater);
            playingHolder.buttonPlayPause.setImageResource(R.drawable.ic_media_play);
        }

    }

    private void updateNonPlayingView(newsFeedViewHolder holder) {
       /* if (holder == playingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }*/
        holder.seekBarProgress.removeCallbacks(seekBarUpdater);
        holder.seekBarProgress.setEnabled(false);
        holder.seekBarProgress.setProgress(0);
        holder.buttonPlayPause.setImageResource(R.drawable.ic_media_play);
    }

    private void startMediaPlayer(Uri audioResId) {
              mediaPlayer = MediaPlayer.create(mCtx, audioResId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
        mediaPlayer.start();

        if(mediaPlayer != null)
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
//                    playingHolder.seekBarProgress.setSecondaryProgress(i);

                }
            });

        playingHolder.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seeked_progess = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seeked_progess);
                }
            }
        });

    }

    private void releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder);
        }
        mediaPlayer.release();
        mediaPlayer = null;
        playingPosition = -1;
    }
    @Override
    public void onViewRecycled(newsFeedViewHolder holder) {
        super.onViewRecycled(holder);
        if (playingPosition == holder.getAdapterPosition()) {
            // view holder displaying playing audio cell is being recycled
            // change its state to non-playing
            updateNonPlayingView(playingHolder);
            playingHolder = null;
        }
    }
}