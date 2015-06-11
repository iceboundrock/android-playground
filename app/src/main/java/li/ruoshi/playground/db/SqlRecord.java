package li.ruoshi.playground.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by ruoshili on 2/10/15.
 */
public class SqlRecord {
    private static final String TAG = SqlRecord.class.getSimpleName();

    private static final HashMap<Class<?>, MappingInfo> mappingInfos = new HashMap<>();

    private static MappingInfo getMappingInfo(Class<?> clazz) {
        synchronized (mappingInfos) {
            if (mappingInfos.containsKey(clazz)) {
                return mappingInfos.get(clazz);
            }

            MappingInfo ret = new MappingInfo(clazz);
            mappingInfos.put(clazz, ret);
            return ret;
        }
    }

    private static Context context;

    public static void init(Context ctx) {
        context = ctx;
    }

    private static final String COMMA_SPACE = ", ";
    private static final String COMMA_NEWLINE = ",\n";
    private static final String AND = " and ";

    private static final List<? extends Class> IntegerTypes = Collections.unmodifiableList(Arrays.asList(
            boolean.class,
            Boolean.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            BigInteger.class));

    private static final List<? extends Class> RealTypes = Collections.unmodifiableList(Arrays.asList(
            float.class,
            Float.class,
            double.class,
            Double.class,
            BigDecimal.class
    ));

    public static boolean createTable(SQLiteDatabase db, Class<?> clazz) {
        MappingInfo mappingInfo = getMappingInfo(clazz);

        String sql = generateCreateTableSql(mappingInfo);

        if (TextUtils.isEmpty(sql)) {
            Log.w(TAG, "generate create table sql failed.");
        }

        try {
            Log.d(TAG, "Create table SQL: " + sql);
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, "Execute create table sql failed.", e);
            return false;
        }
        return true;
    }

    public static boolean insert(Object object) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }

        Class<?> clazz = object.getClass();
        DbTable tableAnn = clazz.getAnnotation(DbTable.class);
        if (tableAnn == null) {
            Log.w(TAG, String.format("%s has no Table annotation", clazz.getName()));
            return false;
        }

        final DbHelper helper = new DbHelper(context);

        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            return insert(db, object, tableAnn.name());
        } catch (SQLException se) {
            Log.e(TAG, "insert failed.", se);
        } finally {
            helper.close();
        }
        return false;
    }

    public static <T> boolean insert(List<T> list, Class<T> clazz) {
        if (list == null) {
            throw new NullPointerException("list is null");
        }

        if (list.size() == 0) {
            return true;
        }

        DbTable tableAnn = clazz.getAnnotation(DbTable.class);
        if (tableAnn == null) {
            Log.w(TAG, String.format("%s has no Table annotation", clazz.getName()));
            return false;
        }

        Log.d(TAG, "Insert objects of " + clazz.getSimpleName());

        final DbHelper helper = new DbHelper(context);
        final SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (T object : list) {
                if (object == null) {
                    continue;
                }

                if (!insert(db, object, tableAnn.name())) {
                    Log.d(TAG, "Insert a object of " + clazz.getSimpleName() + " failed.");
                    return false;
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException se) {
            Log.e(TAG, "insert failed.", se);
        } finally {
            db.endTransaction();
            helper.close();
        }
        return false;
    }

    private static boolean insert(SQLiteDatabase db, Object object, String tableName) {
        return db.insert(tableName, null, getContentValues(object)) == 1;
    }

    private static String generatePkWhereClause(MappingInfo mappingInfo) {
        StringBuilder sb = new StringBuilder();

        for (FieldInfo f : mappingInfo.primaryKeyFields) {
            sb.append(f.columnName).append(" = ? ").append(AND);
        }

        sb.delete(sb.length() - AND.length(), sb.length());
        return sb.toString();
    }

    private static String[] getPkValues(Object object, MappingInfo mappingInfo) {
        String[] ret = new String[mappingInfo.primaryKeyFields.size()];
        int i = 0;
        for (FieldInfo f : mappingInfo.primaryKeyFields) {
            try {
                ret[i++] = String.valueOf(f.field.get(object));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "getPkValues failed.", e);
            }
        }
        return ret;
    }

    public static boolean delete(Object object) {
        if (object == null) {
            Log.e(TAG, "object is null");
            throw new NullPointerException("object is null.");
        }

        final Class<?> clazz = object.getClass();
        final MappingInfo mappingInfo = getMappingInfo(clazz);

        if (mappingInfo.primaryKeyFields.size() == 0) {
            Log.w(TAG, "Object has no PrimaryKey annotation");
            return false;
        }

        String[] pkValues = getPkValues(object, mappingInfo);
        final DbHelper helper = new DbHelper(context);
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            return db.delete(mappingInfo.tableName, generatePkWhereClause(mappingInfo), pkValues) == 1;
        } catch (SQLException sqlEx) {
            Log.e(TAG, "Delete failed.", sqlEx);
            return false;
        } finally {
            helper.close();
        }
    }

    private static ContentValues getContentValues(Object object) {
        return getContentValues(object, null);
    }

    private static ContentValues getContentValues(Object object, final List<String> columnNames) {
        ContentValues cv = new ContentValues();

        Class<?> clazz = object.getClass();
        MappingInfo mappingInfo = getMappingInfo(clazz);

        for (FieldInfo f : mappingInfo.fieldInfoList) {

            String colName = f.columnName;

            if (columnNames != null
                    && columnNames.size() > 0
                    && !columnNames.contains(colName)) {
                continue;
            }

            try {
                Class<?> fieldType = f.field.getType();
                if (fieldType == boolean.class) {
                    boolean b = f.field.getBoolean(object);
                    cv.put(colName, b ? 1 : 0);
                } else if (fieldType == Boolean.class) {
                    boolean b = (Boolean) f.field.get(object);
                    cv.put(colName, b ? 1 : 0);
                } else if (fieldType == byte.class) {
                    cv.put(colName, f.field.getByte(object));
                } else if (fieldType == Byte.class) {
                    cv.put(colName, (Byte) f.field.get(object));
                } else if (fieldType == short.class) {
                    cv.put(colName, f.field.getShort(object));
                } else if (fieldType == Short.class) {
                    cv.put(colName, (Short) f.field.get(object));
                } else if (fieldType == int.class) {
                    cv.put(colName, f.field.getInt(object));
                } else if (fieldType == Integer.class) {
                    cv.put(colName, (Integer) f.field.get(object));
                } else if (fieldType == long.class) {
                    cv.put(colName, f.field.getLong(object));
                } else if (fieldType == Long.class) {
                    cv.put(colName, (Long) f.field.get(object));
                } else if (fieldType == float.class) {
                    cv.put(colName, f.field.getFloat(object));
                } else if (fieldType == Float.class) {
                    cv.put(colName, (Float) f.field.get(object));
                } else if (fieldType == double.class) {
                    cv.put(colName, f.field.getDouble(object));
                } else if (fieldType == Double.class) {
                    cv.put(colName, (Double) f.field.get(object));
                } else {
                    cv.put(colName, String.valueOf(f.field.get(object)));
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, "getContentValues failed.", e);
                throw new RuntimeException("getContentValues failed.", e);
            }
        }
        return cv;
    }

    public static boolean update(Object object) {
        return update(object, null);
    }

    public static boolean update(Object object, final String[] columnNames) {
        if (object == null) {
            Log.e(TAG, "object is null");
            throw new NullPointerException("object is null.");
        }

        final Class<?> clazz = object.getClass();

        MappingInfo mappingInfo = getMappingInfo(clazz);

        if (mappingInfo.primaryKeyFields.size() == 0) {
            Log.w(TAG, "Object has no PrimaryKey annotation");
            return false;
        }

        DbHelper dbHelper = new DbHelper(context);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(mappingInfo.tableName,
                    getContentValues(object, columnNames == null ? null : Arrays.asList(columnNames)),
                    generatePkWhereClause(mappingInfo),
                    getPkValues(object, mappingInfo)) == 1;
        } catch (SQLException sqlEx) {
            Log.e(TAG, "update failed.", sqlEx);
            return false;
        } finally {
            dbHelper.close();
        }
    }

    public static <T> List<T> select(Class<T> clazz, String whereClause, String[] whereArgs) {
        return select(clazz, whereClause, whereArgs, null, null, null, null);
    }

    public static <T> List<T> select(Class<T> clazz, String whereClause, String[] whereArgs, String limit) {
        return select(clazz, whereClause, whereArgs, null, null, null, limit);
    }

    public static <T> List<T> select(Class<T> clazz, String whereClause, String[] whereArgs,
                                     String groupBy,
                                     String having,
                                     String orderBy,
                                     String limit) {
        MappingInfo mappingInfo = getMappingInfo(clazz);

        final DbHelper helper = new DbHelper(context);
        try {
            final SQLiteDatabase db = helper.getReadableDatabase();
            final Cursor c = db.query(mappingInfo.tableName,
                    mappingInfo.columnNames.toArray(new String[mappingInfo.columnNames.size()]),
                    whereClause,
                    whereArgs,
                    groupBy,
                    having,
                    orderBy,
                    limit);

            ArrayList<T> ret = new ArrayList<>();

            if (!c.moveToFirst()) {
                return new ArrayList<>(0);
            }
            do {
                T t = mappingInfo.fromCursor(c);
                if (t == null) {
                    Log.w(TAG, "fromCursor result is null");
                    continue;
                }
                ret.add(t);
            } while (c.moveToNext());
            c.close();
            return ret;
        } catch (SQLException sqlEx) {
            Log.e(TAG, "select failed.", sqlEx);
        } finally {
            helper.close();
        }
        return new ArrayList<>(0);
    }


    private static String generatePkClause(MappingInfo mappingInfo) {
        int pkCount = mappingInfo.primaryKeyFields.size();

        HashMap<Integer, String> pkColumns = new HashMap<>(pkCount);

        for (FieldInfo f : mappingInfo.primaryKeyFields) {
            PrimaryKey pkAnn = f.primaryKey;

            if (pkAnn == null) {
                continue;
            }
            int order = pkAnn.order();

            pkColumns.put(order, f.columnName);
        }

        List<Integer> pkOrders = Arrays.asList(pkColumns.keySet().toArray(new Integer[pkCount]));
        Collections.sort(pkOrders);
        StringBuilder sb = new StringBuilder();
        sb.append("PRIMARY KEY ( ");

        for (Integer c : pkOrders) {
            sb.append(pkColumns.get(c)).append(COMMA_SPACE);
        }

        sb.delete(sb.length() - COMMA_SPACE.length(), sb.length());
        sb.append(" ) ");

        return sb.toString();
    }

    private static String generateCreateTableSql(MappingInfo mappingInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(mappingInfo.tableName).append(" ( ");


        for (FieldInfo f : mappingInfo.fieldInfoList) {
            sb.append(generateColumnRow(f)).append(COMMA_NEWLINE);
        }

        String pkClause = generatePkClause(mappingInfo);

        if (TextUtils.isEmpty(pkClause)) {
            sb.delete(sb.length() - COMMA_NEWLINE.length(), sb.length());
        } else {
            sb.append(pkClause);
        }
        sb.append(" ); ");

        return sb.toString();
    }


    private static String generateColumnRow(FieldInfo f) {
        String colName = f.columnName;

        String type = "TEXT";
        if (IntegerTypes.indexOf(f.typeOfField) >= 0) {
            type = "INTEGER";
        } else if (RealTypes.indexOf(f.typeOfField) >= 0) {
            type = "REAL";
        }

        return String.format("%s %s %s NULL", colName, type, f.notNull ? "not" : "");
    }
}
