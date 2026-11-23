package ido.dotan.physicspendelumido;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelperDB extends SQLiteOpenHelper {

    public static final String DB_FILE="Info.db";

    // Users table
    public static final String Users_TABLE="UsersList";
    public static final String USER_CODE="uCode";
    public static final String USER_PASSWORD="uPassword";

    // Videos table
    public static final String Videos_TABLE="VideosList";
    public static final String VIDEO_ID="_id";
    public static final String VIDEO_TITLE="vTitle";
    public static final String VIDEO_URL="vUrl";

    // Calculations table
    public static final String Calculations_TABLE = "CalculationsList";
    public static final String CALC_ID = "_id";
    public static final String CALC_USER_CODE = "uCode";
    public static final String CALC_L = "length";
    public static final String CALC_G = "gravity";
    public static final String CALC_T = "period";
    public static final String CALC_DATE = "calcDate";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface OnDbOperationCompleteListener {
        void onDbOperationComplete(boolean success);
    }

    public interface OnVideosLoadedListener {
        void onVideosLoaded(List<VideoItem> videos);
    }

    public interface OnCalculationsLoadedListener {
        void onCalculationsLoaded(List<CalculationItem> calculations);
    }

    // Helper classes
    public static class VideoItem {
        public int id;
        public String title;
        public String url;
        public VideoItem(int id, String title, String url) { this.id = id; this.title = title; this.url = url; }
    }

    public static class CalculationItem {
        public int id;
        public String userCode;
        public double l, g, t;
        public String date;
        public CalculationItem(int id, String userCode, double l, double g, double t, String date) {
            this.id = id; this.userCode = userCode; this.l = l; this.g = g; this.t = t; this.date = date;
        }
    }

    public HelperDB(@Nullable Context context) {
        super(context, DB_FILE, null, 5); // Incremented version
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+Users_TABLE+"("+USER_CODE+" TEXT UNIQUE, "+USER_PASSWORD+" TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+Videos_TABLE+"("+VIDEO_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+VIDEO_TITLE+" TEXT, "+VIDEO_URL+" TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+Calculations_TABLE+"("+CALC_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CALC_USER_CODE+" TEXT, "+CALC_L+" REAL, "+CALC_G+" REAL, "+CALC_T+" REAL, "+CALC_DATE+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Users_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Videos_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Calculations_TABLE);
        onCreate(db);
    }

    // --- Calculation Methods ---

    public void addCalculation(String userCode, double length, double gravity, double period, String date, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CALC_USER_CODE, userCode);
            values.put(CALC_L, length);
            values.put(CALC_G, gravity);
            values.put(CALC_T, period);
            values.put(CALC_DATE, date);
            long id = db.insert(Calculations_TABLE, null, values);
            handler.post(() -> listener.onDbOperationComplete(id != -1));
        });
    }

    public void getUserCalculations(String userCode, OnCalculationsLoadedListener listener) {
        executorService.execute(() -> {
            List<CalculationItem> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String selection = CALC_USER_CODE + " = ?";
            String[] selectionArgs = { userCode };
            Cursor cursor = db.query(Calculations_TABLE, null, selection, selectionArgs, null, null, CALC_ID + " DESC");
            if (cursor.moveToFirst()) {
                do {
                    list.add(new CalculationItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow(CALC_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(CALC_USER_CODE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(CALC_L)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(CALC_G)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(CALC_T)),
                            cursor.getString(cursor.getColumnIndexOrThrow(CALC_DATE))
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
            handler.post(() -> listener.onCalculationsLoaded(list));
        });
    }

    public void deleteCalculation(int id, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            int deleted = db.delete(Calculations_TABLE, CALC_ID + "=?", new String[]{String.valueOf(id)});
            handler.post(() -> listener.onDbOperationComplete(deleted > 0));
        });
    }

    // --- Video and User Methods ---
    public void addVideo(String title, String url, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(VIDEO_TITLE, title);
            values.put(VIDEO_URL, url);
            long id = db.insert(Videos_TABLE, null, values);
            handler.post(() -> listener.onDbOperationComplete(id != -1));
        });
    }

    public void getAllVideos(OnVideosLoadedListener listener) {
        executorService.execute(() -> {
            List<VideoItem> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Videos_TABLE, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(new VideoItem(cursor.getInt(cursor.getColumnIndexOrThrow(VIDEO_ID)), cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_URL))));
                } while (cursor.moveToNext());
            }
            cursor.close();
            handler.post(() -> listener.onVideosLoaded(list));
        });
    }

    public void deleteVideo(int id, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            int deleted = db.delete(Videos_TABLE, VIDEO_ID + "=?", new String[]{String.valueOf(id)});
            handler.post(() -> listener.onDbOperationComplete(deleted > 0));
        });
    }

    public void isCodeExists(String code, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Users_TABLE, null, USER_CODE + " = ?", new String[]{code}, null, null, null);
            int count = cursor.getCount(); cursor.close();
            handler.post(() -> listener.onDbOperationComplete(count > 0));
        });
    }

    public void addUser(String code, String password, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USER_CODE, code); values.put(USER_PASSWORD, password);
            long id = db.insert(Users_TABLE, null, values);
            handler.post(() -> listener.onDbOperationComplete(id != -1));
        });
    }

    public void checkUser(String code, String password, OnDbOperationCompleteListener listener) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Users_TABLE, null, USER_CODE + " = ? AND " + USER_PASSWORD + " = ?", new String[]{code, password}, null, null, null);
            int count = cursor.getCount(); cursor.close();
            handler.post(() -> listener.onDbOperationComplete(count > 0));
        });
    }
}
