package li.ruoshi.playground.db;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ruoshili on 2/11/15.
 */
public class MappingInfo {
    private static final String TAG = MappingInfo.class.getSimpleName();

    public final Class<?> clazz;

    public final List<FieldInfo> fieldInfoList;

    public final List<FieldInfo> primaryKeyFields;

    public final String tableName;

    public final List<String> columnNames;

    public MappingInfo(final Class<?> clazz){
        this.clazz = clazz;

        DbTable tableAnn = clazz.getAnnotation(DbTable.class);

        if(tableAnn == null){
            Log.w(TAG, String.format("Class %s has no DbTable annotation", clazz.getName()));
            throw new IllegalArgumentException("Class has no DbTable annotation");
        }

        tableName = tableAnn.name();

        Field[] fields = clazz.getDeclaredFields();

        ArrayList<FieldInfo> fieldInfos = new ArrayList<>(fields.length);
        ArrayList<String> colNames = new ArrayList<>(fields.length);
        ArrayList<FieldInfo> pks = new ArrayList<>(fields.length);
        for(Field f : fields){
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
               continue;
            }
            NotInDb notInDb = f.getAnnotation(NotInDb.class);

            if(notInDb != null){
                continue;
            }
            FieldInfo fi = new FieldInfo(f);

            fieldInfos.add(fi);
            colNames.add(fi.columnName);
            if(fi.primaryKey != null){
                pks.add(fi);
            }
        }
        this.fieldInfoList = Collections.unmodifiableList(fieldInfos);
        this.primaryKeyFields = Collections.unmodifiableList(pks);
        this.columnNames = Collections.unmodifiableList(colNames);

        Log.d(TAG, String.format("%s has %d fields, %d pks", clazz.getSimpleName(), fieldInfoList.size(), primaryKeyFields.size()));
    }

    public <T> T fromCursor(Cursor c){
        try {
            Constructor constructor = clazz.getConstructor();
            try {
                Object obj = constructor.newInstance();

                int index = 0;
                for(FieldInfo f : fieldInfoList){
                    try {
                        Class<?> fieldType = f.typeOfField;

                        if(CharSequence.class.isAssignableFrom(fieldType)){
                            String s = c.getString(index++);
                            f.field.set(obj, s);
                        }else if (fieldType == boolean.class) {
                            boolean b = c.getInt(index++) != 0;
                            f.field.setBoolean(obj, b);
                        } else if (fieldType == Boolean.class) {
                            boolean b = c.getInt(index++) != 0;
                            f.field.setBoolean(obj, b);
                        } else if (fieldType == byte.class) {
                            byte b = (byte)c.getShort(index++);
                            f.field.setByte(obj, b);
                        } else if (fieldType == Byte.class) {
                            byte b = (byte)c.getShort(index++);
                            f.field.setByte(obj, b);
                        } else if (fieldType == short.class) {
                            short b = c.getShort(index++);
                            f.field.setShort(obj, b);
                        } else if (fieldType == Short.class) {
                            short b = c.getShort(index++);
                            f.field.setShort(obj, b);
                        } else if (fieldType == int.class) {
                            int b = c.getInt(index++);
                            f.field.setInt(obj, b);
                        } else if (fieldType == Integer.class) {
                            int b = c.getInt(index++);
                            f.field.setInt(obj, b);
                        } else if (fieldType == long.class) {
                            long b = c.getLong(index++);
                            f.field.setLong(obj, b);
                        } else if (fieldType == Long.class) {
                            long b = c.getLong(index++);
                            f.field.setLong(obj, b);
                        } else if (fieldType == float.class) {
                            float b = c.getFloat(index++);
                            f.field.setFloat(obj, b);
                        } else if (fieldType == Float.class) {
                            float b = c.getFloat(index++);
                            f.field.setFloat(obj, b);
                        } else if (fieldType == double.class) {
                            double b = c.getDouble(index++);
                            f.field.setDouble(obj, b);
                        } else if (fieldType == Double.class) {
                            double b = c.getDouble(index++);
                            f.field.setDouble(obj, b);
                        } else if (fieldType.isEnum()){
                            String s = c.getString(index++);
                            Method valueOf = fieldType.getMethod("valueOf", String.class);
                            f.field.set(obj, valueOf.invoke(null, s));
                        }
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "getContentValues failed.", e);
                        throw new RuntimeException("getContentValues failed.", e);
                    }
                }

                return (T)obj;

            } catch (Throwable e){
                Log.e(TAG, "fromCursor failed.", e);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "fromCursor failed.", e);
        }
        return null;
    }
}

