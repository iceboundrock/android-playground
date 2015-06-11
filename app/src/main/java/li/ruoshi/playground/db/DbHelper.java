package li.ruoshi.playground.db;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by ruoshili on 1/19/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static abstract class UpgradeExecutor {
        public final int fromVersion;
        public final int toVersion;

        protected UpgradeExecutor(int from, int to) {
            this.fromVersion = from;
            this.toVersion = to;
        }

        public boolean shouldExecute(int from, int to) {
            final boolean ret = from <= fromVersion && to >= toVersion;
            Log.d(TAG, String.format("%s upgrade from %d to %d", ret ? "should" : "should not", fromVersion, toVersion));
            return ret;
        }

        protected abstract void onUpgrade(SQLiteDatabase db);

        public void upgrade(SQLiteDatabase db) {
            Log.d(TAG, String.format("upgrade %s from %d to %d", DATABASE_NAME, fromVersion, toVersion));
            onUpgrade(db);
        }
    }

    private static final String TAG = DbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tutor_db";
    public static final String QUESTIONS_TABLE_NAME = "questions";
    public static final String COMMENTS_TABLE_NAME = "comments";
    public static final String QUESTIONS_PIC_TABLE_NAME = "questions_pic";
    public static final String COMMENTS_PIC_TABLE_NAME = "comments_pic";
    public static final String USERS_TABLE_NAME = "users";

    private static final String TABLE_QUESTIONS_CREATE =
            "CREATE TABLE " + QUESTIONS_TABLE_NAME + " (" +
                    "id INTEGER not NULL,\n" +
                    "authorId INTEGER not NULL,\n" +
                    "grade TEXT NULL,\n" +
                    "subject TEXT NULL,\n" +
                    "desc TEXT NULL,\n" +
                    "createdDate TEXT NULL,\n" +
                    "thumbnailPath TEXT NULL,\n" +
                    "boardId TEXT NULL,\n" +
                    "status TEXT NULL,\n" +
                    "lastModifiedDate TEXT NULL,\n" +
                    "lastStatusOperatorId INTEGER NULL,\n" +
                    "appUserId INTEGER not NULL," +
                    "PRIMARY KEY (id, authorId));\n";

    private static final String TABLE_COMMENTS_CREATE =
            "CREATE TABLE " + COMMENTS_TABLE_NAME + " (" +
                    "id INTEGER not NULL,\n" +
                    "authorId INTEGER not NULL,\n" +
                    "questionId INTEGER not NULL,\n" +
                    "questionAuthorId INTEGER not NULL,\n" +
                    "createdDate TEXT NULL,\n" +
                    "PRIMARY KEY (id, authorId));\n";

    private static final String TABLE_QUESTIONS_PIC_CREATE =
            "CREATE TABLE " + QUESTIONS_PIC_TABLE_NAME + " (" +
                    "id INTEGER not NULL,\n" +
                    "authorId INTEGER not NULL,\n" +
                    "questionId INTEGER not NULL,\n" +
                    "questionAuthorId INTEGER not NULL,\n" +
                    "url TEXT not null,\n" +
                    "path TEXT," +
                    "PRIMARY KEY (id, authorId));\n";

    private static final String TABLE_COMMENTS_PIC_CREATE =
            "CREATE TABLE " + COMMENTS_PIC_TABLE_NAME + " (" +
                    "id INTEGER not NULL,\n" +
                    "authorId INTEGER not NULL,\n" +
                    "questionId INTEGER not NULL,\n" +
                    "questionAuthorId INTEGER not NULL,\n" +
                    "url TEXT not null,\n" +
                    "path TEXT," +
                    "PRIMARY KEY (id, authorId));\n";


    // 用于缓存头像
    private static final String TABLE_USER_CREATE =
            "CREATE TABLE " + USERS_TABLE_NAME + " (" +
                    "uid INTEGER not NULL,\n" +
                    "name TEXT NULL,\n" +   // 登录名
                    "nick TEXT NULL,\n" +
                    "portraitUrl TEXT,\n" + // 如果只有文件名，则从asset中读取
                    "portraitPath TEXT,\n" +
                    "PRIMARY KEY (uid));\n";

    public DbHelper(final Context context) {
        super(context,
                DATABASE_NAME, null, DATABASE_VERSION);

        Log.d(TAG, String.format("New QuestionsDbHelper instance, db name %s, db version: %d.",
                DATABASE_NAME, DATABASE_VERSION));
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");

        db.beginTransaction();
        try {
            Log.d(TAG, "create tables success.");
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "create tables failed.", ex);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final UpgradeExecutor[] executors = new UpgradeExecutor[]{
        };

        if (executors.length == 0) {
            return;
        }

        db.beginTransaction();
        try {
            for (UpgradeExecutor executor : executors) {
                if (executor.shouldExecute(oldVersion, newVersion)) {
                    executor.upgrade(db);
                }
            }
            db.setTransactionSuccessful();
            Log.e(TAG, String.format("upgrade %s from %d to %d success.", DATABASE_NAME, oldVersion, newVersion));
        } catch (SQLException se) {
            Log.e(TAG, "onUpgrade db failed.", se);
        } finally {
            db.endTransaction();
        }
    }
}
