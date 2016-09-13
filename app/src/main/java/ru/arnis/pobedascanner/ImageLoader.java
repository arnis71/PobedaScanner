package ru.arnis.pobedascanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;

import ru.arnis.pobedascanner.other.Post;

/**
 * Created by arnis on 18/08/16.
 */
public class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    public static ArrayList<Bitmap> cachedPostImages = new ArrayList<>();

    public ImageLoader(String url) {
        this.url = url;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
//            Log.d("happy", "DOWNLOADING IMAGE");
            if (url==null)
                return null;
            URL urlConnection = new URL(url);
            return BitmapFactory.decodeStream(urlConnection.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result==null){
            cachedPostImages.add(null);
            return;
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(result, MainActivity.width-40,(MainActivity.width-40)/2,false);
        cachedPostImages.add(bitmap);
    }

    public static void cacheImages(ArrayList<Post> posts){
            for (Post post:posts)
                new ImageLoader(post.getImageURL()).execute();
    }

    public static void requestPostponedImage(final ImageView postImage, final int index) {
        new Thread(new Runnable() {
            int waitLimit = 10;
            @Override
            public void run() {
                while (cachedPostImages.size()<=index&&waitLimit>0) {
//                    Log.d("happy", "run: " + Integer.toString(cachedPostImages.size()));
                    try {
                        Thread.sleep(1000);
                        waitLimit--;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (waitLimit!=0&&cachedPostImages.get(index)!=null)
                    postImage.post(new Runnable() {
                        @Override
                        public void run() {
                            postImage.setImageBitmap(cachedPostImages.get(index));
                        }
                    });
            }
        }).start();
    }
}
