package ru.arnis.pobedascanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by arnis on 18/08/16.
 */
public class MyAdapter extends BaseAdapter {
    ArrayList<Post> posts;
    Context context;

    public MyAdapter(ArrayList<Post> posts, Context context) {
        this.posts=posts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static class ViewHolder {
        public final ImageView postImage;
        public final TextView postText;
        public final View shade;
        public final TextView postDate;
        public final Button buyTicket;

        public ViewHolder(ImageView postImage, TextView postText,View shade,TextView postDate,Button buyTicket) {
            this.postImage = postImage;
            this.postText = postText;
            this.shade =shade;
            this.postDate = postDate;
            this.buyTicket = buyTicket;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView postImage;
        TextView postText;
        View shade;
        TextView postDate;
        Button buyTicket;
        if (view==null){
            view = LayoutInflater.from(context).inflate(R.layout.tab,viewGroup,false);
            postText = (TextView) view.findViewById(R.id.postText);
            postImage = (ImageView)view.findViewById(R.id.postImage);
            shade = view.findViewById(R.id.pre_lolipop_shadow_tab);
            postDate = (TextView) view.findViewById(R.id.postDate);
            buyTicket = (Button)view.findViewById(R.id.buy_tickets);
            buyTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("http://www.pobeda.aero");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            view.setTag(new ViewHolder(postImage,postText,shade,postDate,buyTicket));

        } else{
            ViewHolder viewHolder = (ViewHolder)view.getTag();
            postImage = viewHolder.postImage;
            postText = viewHolder.postText;
            shade = viewHolder.shade;
            postDate = viewHolder.postDate;
            buyTicket = viewHolder.buyTicket;
        }


        if (Build.VERSION.SDK_INT<= Build.VERSION_CODES.LOLLIPOP&&shade.getVisibility()==View.GONE){
            shade.setVisibility(View.VISIBLE);
            buyTicket.setBackgroundColor(Color.WHITE);
        }

        postText.setText(posts.get(i).getText());
        postDate.setText(posts.get(i).getTimeStamp());
//        Log.d("happycache", "cache "+Integer.toString(ImageLoader.cachedPostImages.size()) + " i " + Integer.toString(i));

        if (ImageLoader.cachedPostImages.size()>i&&ImageLoader.cachedPostImages.get(i)!=null){
            postImage.setImageBitmap(ImageLoader.cachedPostImages.get(i));
        } else {
            postImage.setImageResource(R.drawable.offline_image);
            if (NetworkStateReceiver.connected)
                ImageLoader.requestPostponedImage(postImage, i);
        }


        return view;
    }
}
