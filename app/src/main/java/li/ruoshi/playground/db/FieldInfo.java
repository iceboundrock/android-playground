package li.ruoshi.playground.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ruoshili on 2/11/15.
 */
public class FieldInfo {
    public final Field field;

    public final Class<?> typeOfField;

    public final String columnName;

    public final PrimaryKey primaryKey;

    public final List<DbIndex> indexes;

    public final boolean notNull;

    public FieldInfo(final Field field) {
        this.field = field;

        field.setAccessible(true);

        typeOfField = field.getType();

        primaryKey = field.getAnnotation(PrimaryKey.class);

        DbColumn colAnn = field.getAnnotation(DbColumn.class);

        this.notNull = field.getAnnotation(DbNotNull.class) != null;

        this.columnName = colAnn == null ? field.getName() : colAnn.name();

        DbIndexes multiIndexes = field.getAnnotation(DbIndexes.class);
        DbIndex singleIndex = field.getAnnotation(DbIndex.class);
        if (multiIndexes != null) {
            indexes = Collections.unmodifiableList(Arrays.asList(multiIndexes.indexes()));
        } else if (singleIndex != null) {
            indexes = Collections.unmodifiableList(Arrays.asList(singleIndex));
        } else {
            indexes = Collections.unmodifiableList(new ArrayList<DbIndex>(0));
        }
    }
}
