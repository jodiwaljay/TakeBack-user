package com.elegance.feedback;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Created by Jay on 9/30/2015.
 */
public class sql_logo {

    private final Context ourContext;
    private static final String DATABASE_NAME = "BACKEND_DB";
    private static final String DATABASE_TABLE = "BACKEND_TABLE";
    private static final int DATABASE_VERSION = 1;

    private dbHelper ourHelper;
    private SQLiteDatabase ourDatabase;
    public sql_logo(Context c){
        ourContext = c;
    }

    public sql_logo open() throws SQLException{
        ourHelper = new dbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }

    private static class dbHelper extends SQLiteOpenHelper{

        public dbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, IMAGE BLOB );"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
    public long createEntry(Bitmap bitmap ){

        //checkDefaultImage();
        Log.d("Entry","createEntry is started");
        ContentValues cv = new ContentValues();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitMapData = stream.toByteArray();
        cv.put("IMAGE", bitMapData);
        return ourDatabase.update(DATABASE_TABLE, cv, null, null);

    }

    public Bitmap getData (){
            //checkDefaultImage();
            byte[] img = null;
            String[] col={"IMAGE"};
                Cursor c = ourDatabase.query(DATABASE_TABLE, col, null, null, null, null, null);



            if(c!=null){
                c.moveToFirst();
                do{
                    img=c.getBlob(c.getColumnIndex("IMAGE"));
                }while(c.moveToNext());
            }
            c.close();

            return BitmapFactory.decodeByteArray(img, 0, img.length);


    }

    public void checkDefaultImage(Context ctx){
        Cursor mCursor = ourDatabase.rawQuery("SELECT * FROM " + DATABASE_TABLE, null);

        if (!mCursor.moveToFirst())
        {
            ContentValues cv = new ContentValues();
            Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.icon_feedback);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100, stream);
            byte[] bitMapData = stream.toByteArray();
            cv.put("IMAGE",bitMapData);
            ourDatabase.insert(DATABASE_TABLE,null,cv);

        }
        mCursor.close();
    }


}
