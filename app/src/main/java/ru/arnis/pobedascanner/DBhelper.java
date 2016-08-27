package ru.arnis.pobedascanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by arnis on 19/08/16.
 */
public class DBhelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "posts.db";
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_IMAGE_URL = "image";
    public static final String COLUMN_DATE = "date";

    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_POSTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TEXT + " TEXT," +
                COLUMN_IMAGE_URL + " TEXT," +
                COLUMN_DATE + " TEXT" + ");";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_POSTS);
        onCreate(sqLiteDatabase);

    }

    public void clearTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_POSTS);
        onCreate(db);
    }

//    public void addPost(Post post){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COLUMN_TEXT,post.getText());
//        contentValues.put(COLUMN_IMAGE_URL,post.getImageURL());
//        SQLiteDatabase db = getWritableDatabase();
////        db.beginTransaction();
//        db.insert(TABLE_POSTS,null,contentValues);
////        db.setTransactionSuccessful();
////        db.endTransaction();
//        db.close();
//    }

    public void addPosts(ArrayList<Post> posts){
        Collections.reverse(posts);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Post post:posts) {
            contentValues.clear();
            contentValues.put(COLUMN_TEXT, post.getText());
            contentValues.put(COLUMN_IMAGE_URL, post.getImageURL());
            contentValues.put(COLUMN_DATE, post.getTimeStamp());
            db.insert(TABLE_POSTS, null, contentValues);
        }
        Collections.reverse(posts);
        db.close();
    }

    public boolean checkIFempty(){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_POSTS,null,null,null,null,null,null);
        cursor.moveToFirst();
        database.close();
        return cursor.getCount() == 0;
    }

    public ArrayList<Post> getPosts(){
        ArrayList<Post> posts = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_POSTS,new String[]{COLUMN_TEXT,COLUMN_IMAGE_URL,COLUMN_DATE},null,null,null,null,null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String text = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT));
            String url = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
            String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
            if (!cursor.isLast())
                cursor.moveToNext();
            Post post = new Post(text,url,date);
            posts.add(post);
        }
        Collections.reverse(posts);
        database.close();
        return posts;
    }
}
