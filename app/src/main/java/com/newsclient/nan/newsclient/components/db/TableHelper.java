package com.newsclient.nan.newsclient.components.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.newsclient.nan.newsclient.config.Config;
import com.newsclient.nan.newsclient.components.db.annotation.Column;
import com.newsclient.nan.newsclient.components.db.annotation.Table;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;


/**
 * Created by wzn on 2016/11/21.
 */

public class TableHelper extends SQLiteOpenHelper {

    private Context mContext;


    public TableHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    public TableHelper(Context context) {
        super(context, Config.db_name, null, Config.db_version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            List<String> lstClassNames = getClassName(Config.entity_package_path);
            for (String className :
                    lstClassNames) {
                Class clazz = Class.forName(className);
                buildTable(clazz, db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }

    /**
     * 判断表是否存在
     *
     * @param tableName 表名
     * @param db
     * @return
     */
    public boolean isExistsTable(@NonNull String tableName, SQLiteDatabase db) {
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name= ?";
        Cursor cursor = db.rawQuery(sql.toString(), new String[]{tableName});
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    /**
     * 创建表
     *
     * @param clz
     * @throws Exception
     */
    public void buildTable(@NonNull Class clz, SQLiteDatabase db) throws Exception {

        StringBuilder sql = new StringBuilder();
        if (clz.isAnnotationPresent(Table.class)) {
            String tableName = ((Table) clz.getAnnotation(Table.class)).value();

            sql.append("create table ").append(tableName).append("(");
            for (Field f : clz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Column.class)) {
                    sql.append(buildColumn(f));
                }
            }
            sql.replace(sql.length() - 1, sql.length(), ")");
        }
        if (sql.length() > 0) {
            db.execSQL(sql.toString());
        }
    }

    /**
     * 创建字段
     *
     * @param f
     * @return
     */
    public String buildColumn(@NonNull Field f) {
        StringBuilder sql = new StringBuilder();
        Column column = f.getAnnotation(Column.class);
        sql.append(column.value()).append(" ");
        sql.append(getColumnType(f.getType())).append(" ");
        if (column.id()) {
            sql.append("PRIMARY KEY AUTOINCREMENT");
        }
        sql.append(",");
        return sql.toString();
    }

    /**
     * 获取变量的类型
     *
     * @param clz
     * @return
     */
    public String getColumnType(@NonNull Class clz) {
        String type;
        if (int.class == clz || Integer.class.equals(clz)
                || long.class == clz || Long.class.equals(clz)
                || short.class == clz || Short.class == clz ||
                boolean.class == clz || Boolean.class == clz ||
                Date.class == clz) {
            type = "INTEGER";
        } else if (float.class == clz || Float.class == clz
                || double.class == clz || Double.class == clz) {
            type = "REAL";
        } else if (byte[].class == clz) {
            type = "Blob";
        } else {
            type = "TEXT";
        }
        return type;
    }


    /**
     * 调整表结构或新建表或添加表字段
     *
     * @param clz
     * @param db
     * @param isAlterTable
     * @throws Exception
     */
    public void alterTable(@NonNull Class clz, SQLiteDatabase db, boolean isAlterTable) throws Exception {
        if (clz.isAnnotationPresent(Table.class)) {
            Table table = (Table) clz.getAnnotation(Table.class);
            String tableName = table.value();
            if (isExistsTable(tableName, db)) {
                if (isAlterTable) {
//                    1.修改原表为临时表
                    alterTableToTemp(tableName, db);
//                    2.新建修改字段后的表
                    buildTable(clz, db);
//                    3.从旧表中查询出数据插入新表
//                    4.删除旧表
                } else {
                    addColumn(tableName, clz, db);
                }
            } else {
                buildTable(clz, db);
            }
        }

    }

    /**
     * 修改原表为临时表,方便数据备份
     *
     * @param tableName
     */
    public void alterTableToTemp(@NonNull String tableName, SQLiteDatabase db) {
        String tempTableName = tableName + "_old";
        StringBuilder sql = new StringBuilder("alter table '");
        sql.append(tableName).append("' rename to '").append(tempTableName).append("'");
        db.execSQL(sql.toString());
    }


    /**
     * 添加字段
     */
    public void addColumn(@NonNull String tableName, @NonNull Class clz, SQLiteDatabase db) {
        String sql = "select * from " + tableName + " limit 0";
        Cursor cursor = db.rawQuery(sql, null);
        List<String> lstColumns = Arrays.asList(cursor.getColumnNames());
        StringBuilder addColumnSQL = new StringBuilder();
        for (Field f : clz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                Column column = f.getAnnotation(Column.class);
                String columnName = column.value();
                String type = getColumnType(f.getType());
                if (!lstColumns.contains(columnName)) {
                    addColumnSQL.delete(0, addColumnSQL.length());
                    addColumnSQL.append("ALTER TABLE '");
                    addColumnSQL.append(tableName);
                    addColumnSQL.append("' ADD '");
                    addColumnSQL.append(columnName);
                    addColumnSQL.append("' ");
                    addColumnSQL.append(type);
                    db.execSQL(addColumnSQL.toString());
                }
            }
        }
        cursor.close();

    }

    /**
     * 获取包名下的所有类名
     *
     * @param packageName
     * @return
     */
    public List<String> getClassName(@NonNull String packageName) {
        List<String> classNameList = new ArrayList<String>();
        try {
            //通过DexFile查找当前的APK中可执行文件
            DexFile df = new DexFile(mContext.getPackageCodePath());
            //获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            Enumeration<String> enumeration = df.entries();
            while (enumeration.hasMoreElements()) {//遍历
                String className = (String) enumeration.nextElement();

                if (className.contains(packageName)) {
                    //在当前所有可执行的类里面查找包含有该包名的所有类
                    classNameList.add(className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNameList;
    }

//    /**
//     * 是否需要调整字段类型
//     *
//     * @param clz
//     * @return
//     */
//    public boolean isAlterColumn(Class clz, String tableName, SQLiteDatabase db) {
//        String sql = "select * from " + tableName + " limit 0";
//        Cursor cursor = db.rawQuery(sql, null);
//        for (Field f : clz.getDeclaredFields()) {
//            if (f.isAnnotationPresent(Column.class)) {
//                Column column = f.getAnnotation(Column.class);
//                int index = cursor.getColumnIndex(column.value());
//                if (index != -1) {
//                    int columnType = cursor.getType(index);
//                    String type = getColumnType(f.getType());
//                    if (columnType == Cursor.FIELD_TYPE_INTEGER && !type.equals("INTEGER")) {
//                        return true;
//                    } else if (columnType == Cursor.FIELD_TYPE_FLOAT && !type.equals("REAL")) {
//                        return true;
//                    } else if (columnType == Cursor.FIELD_TYPE_BLOB && !type.equals("Blob")) {
//                        return true;
//                    } else if (columnType == Cursor.FIELD_TYPE_STRING && !type.equals("TEXT")) {
//                        return true;
//                    }
//                }
//            }
//        }
//        cursor.close();
//        return false;
//    }


}
