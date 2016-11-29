package com.newsclient.nan.newsclient.components.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.newsclient.nan.newsclient.components.db.annotation.Column;
import com.newsclient.nan.newsclient.components.db.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by wzn on 2016/11/21.
 */

public class Db {


    /**
     * 缓存注解表数据
     */
    private Map<String, Map<String, Field>> mapColumns = new ArrayMap<>();
    private  SQLiteOpenHelper mHelper;
    private static Db instance;

    public static Db getInstance() {
        if (instance == null) {
            synchronized (Db.class) {
                if (instance == null) {
                    instance = new Db();
                }
            }
        }
        return instance;
    }

    public void init(SQLiteOpenHelper helper) {
        mHelper = helper;
    }

    public SQLiteOpenHelper getDb() throws Exception {
        if (mHelper != null) {
            return mHelper;
        } else {
            throw new Exception("请初始化数据库辅助类");
        }
    }


    /**
     * 保存对象到数据库中
     *
     * @param o 对象
     * @return 对象的id
     */
    public long save(@NonNull Object o) {
        if (o != null) {

            Cursor cursor = null;
            try {

                Class clazz = o.getClass();
                if (!clazz.isAnnotationPresent(Table.class)) {
                    return 0L;
                }
                Table table = (Table) clazz.getAnnotation(Table.class);

                long id = checkExits(table.value(), clazz, o);
                if (id != 0L) {
                    return id;
                }

                ContentValues cv = new ContentValues();

                Map<String, Field> columns = getColumns(clazz);
                for (String name : columns.keySet()) {
                    Field f = columns.get(name);
                    Object of = f.get(o);
                    if (of != null) {
                        Class type = f.getType();
                        if (type == Integer.class || type == int.class) {
                            cv.put(name, (int) of);
                        } else if (type == Long.class || type == long.class) {
                            cv.put(name, (long) of);
                        } else if (type == Float.class || type == float.class) {
                            cv.put(name, (Float) of);
                        } else if (type == Double.class || type == double.class) {
                            cv.put(name, (double) of);
                        } else if (type == Boolean.class || type == boolean.class) {
                            cv.put(name, (boolean) of ? 1 : 0);
                        } else if (type == String.class) {
                            cv.put(name, (String) of);
                        } else if (type == Date.class) {
                            cv.put(name, ((Date) of).getTime());
                        }
                    }
                }
                return mHelper.getWritableDatabase().insert(table.value(), null, cv);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return 0L;
    }


    /**
     * 批量插入
     *
     * @param clazz
     * @param list
     * @param <T>
     */
    public <T> void saveList(Class<T> clazz, @NonNull List<T> list) {
        if (clazz.isAnnotationPresent(Table.class)) {
            try {
                Table table = clazz.getAnnotation(Table.class);
                Field id = null;
                Map<String, Field> columns = getColumns(clazz);
                //sql语句
                StringBuilder sql = new StringBuilder();
                sql.append("insert into ").append(table.value()).append("(");
                for (String name : columns.keySet()) {
                    if (name.equals("id")) {
                        id = columns.get(name);
                    }
                    sql.append(name).append(",");
                }
                sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, ")values(");
                int size = columns.size();
                for (int i = 0; i < size; i++) {
                    sql.append("?,");
                }
                sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, ")");
                List<Object> params = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                sb.append("select id from ").append(table.value()).append(" where id=?");
                Collection<Field> fields = columns.values();
                String[] whereId = new String[1];
                for (T t : list) {
                    Object of = id.get(t);
                    if (of != null) {
                        whereId[0] = (String) of;
                        Cursor cursor = execRawQuery(sb.toString(), whereId);
                        int count = cursor.getCount();
                        cursor.close();
                        if (count > 0) {
                            updateById(t, (Long) of);
                            continue;
                        }
                    }
                    params.clear();
                    for (Field f : fields) {
                        Class type = f.getType();
                        if (type == boolean.class || type == Boolean.class) {
                            Object is = f.get(t);
                            if (is != null) {
                                params.add(((boolean) is) ? 1 : 0);
                            } else {
                                params.add(null);
                            }
                        } else if (type == Date.class) {
                            Object time = f.get(t);
                            if (time != null) {
                                params.add(((Date) time).getTime());
                            } else {
                                params.add(null);
                            }
                        } else {
                            params.add(f.get(t));
                        }
                    }


                    execSQL(sql.toString(), params.toArray(new Object[size]));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 更新数据
     *
     * @param o              对象
     * @param whereCondition 条件
     */
    public void update(Object o, Map<String, String> whereCondition) {
        if (o != null) {
            Cursor cursor = null;
            try {
                Class clazz = o.getClass();
                if (!clazz.isAnnotationPresent(Table.class)) {
                    return;
                }
                Table table = (Table) clazz.getAnnotation(Table.class);

                StringBuilder sql = new StringBuilder();

                sql.append("update ").append(table.value()).append(" set ");
                Map<String, Field> columns = getColumns(clazz);
                List<Object> params = new ArrayList<>();
                for (String name : columns.keySet()) {
                    Field f = columns.get(name);
                    Object of = f.get(o);
                    if (of != null) {
                        sql.append(name).append("=?,");
                        Class type = f.getType();
                        if (type == Date.class) {
                            //若是date类型，则存long类型的
                            Date date = (Date) of;
                            params.add(date.getTime());
                        } else if (type == Boolean.class || type == boolean.class) {
                            params.add(((boolean) of) ? 1 : 0);
                        } else {
                            params.add(of);
                        }
                    }
                }
                sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, "");

                if (whereCondition != null && !whereCondition.isEmpty()) {
                    sql.append(" where 1=1");
                    for (String key :
                            whereCondition.keySet()) {
                        sql.append(" and ").append(key).append("=?,");
                        params.add(whereCondition.get(key));
                    }
                    sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, "");
                }

                execSQL(sql.toString(), params.toArray(new Object[params.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }

    }

    /**
     * 更新数据通过id
     *
     * @param o  对象
     * @param id id
     */
    public void updateById(Object o, @NonNull Long id) {
        if (o != null) {
            Cursor cursor = null;
            try {
                Class clazz = o.getClass();
                if (!clazz.isAnnotationPresent(Table.class)) {
                    return;
                }
                Table table = (Table) clazz.getAnnotation(Table.class);

                StringBuilder sql = new StringBuilder();

                //更新语句
                sql.append("update ").append(table.value()).append(" set ");
                Map<String, Field> columns = getColumns(clazz);
                List<Object> params = new ArrayList<>();
                for (String name : columns.keySet()) {
                    Field f = columns.get(name);
                    Object of = f.get(o);
                    if (of != null) {
                        sql.append(name).append("=?,");
                        Class type = f.getType();
                        if (type == Date.class) {
                            //若是date类型，则存long类型的
                            Date date = (Date) of;
                            params.add(date.getTime());
                        } else if (type == Boolean.class || type == boolean.class) {
                            params.add(((boolean) of) ? 1 : 0);
                        } else {
                            params.add(of);
                        }
                    }
                }
                sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, "");

                sql.append(" where id=?");
                params.add(id);

                execSQL(sql.toString(), params.toArray(new Object[params.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }

    }


    /**
     * 删除数据
     *
     * @param clazz          类
     * @param whereCondition 条件
     */
    public void delete(Class clazz, Map<String, String> whereCondition) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            return;
        }
        Table table = (Table) clazz.getAnnotation(Table.class);
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(table.value());
        sql.append(" where 1=1");
        List<Object> params = new ArrayList<>();
        if (whereCondition != null && !whereCondition.isEmpty()) {
            Set<String> keys = whereCondition.keySet();
            for (String key : keys) {
                sql.append(" and ");
                sql.append(key);
                sql.append("=?");
                params.add(whereCondition.get(key));
            }
        }
        execSQL(sql.toString(), params.toArray(new Object[params.size()]));
    }

    /**
     * 删除数据通过id
     *
     * @param clazz 类
     * @param id    id
     */
    public void deleteById(Class clazz, @NonNull Long id) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            return;
        }
        Table table = (Table) clazz.getAnnotation(Table.class);
        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.value());
        sb.append(" where id=?");
        execSQL(sb.toString(), new String[]{id.toString()});

    }


    /**
     * 通过条件获取对象
     *
     * @param clazz     类
     * @param condition 条件
     * @param <T>       对象
     * @return 对象
     */
    public <T> T query(Class<T> clazz, Map<String, String> condition) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            return null;
        }
        Cursor cursor = null;
        T o = null;
        try {
            o = clazz.newInstance();
            Table table = clazz.getAnnotation(Table.class);
            QueryCondition qc = new QueryCondition();
            qc.setTable(table.value());
            if (condition != null) {
                qc.setCondition(condition);
            }
            // 这两行的先后顺序不可换
            String sql = qc.getSql();
            List<String> params = qc.getParams();

            cursor = execRawQuery(sql, params.toArray(new String[params.size()]));


            while (cursor.moveToNext()) {
                getObjectByCursorCache(cursor, o, getColumns(clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return o;

    }

    /**
     * 通过id获取对象
     *
     * @param clazz 类
     * @param id    id
     * @param <T>   对象
     * @return 对象
     */
    public <T> T queryById(Class<T> clazz, @NonNull Long id) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            return null;
        }
        Cursor cursor = null;
        T o = null;
        try {
            Table table = clazz.getAnnotation(Table.class);
            o = clazz.newInstance();
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ").append(table.value()).append(" where id=?");
            cursor = execRawQuery(sql.toString(), new String[]{id.toString()});

            while (cursor.moveToNext()) {
                getObjectByCursorCache(cursor, o, getColumns(clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return o;

    }

    /**
     * 通过条件获取对象数组
     *
     * @param clazz     类
     * @param condition 条件
     * @param order     排序
     * @param <T>       数组
     * @return 数组
     */
    public <T> List<T> queryList(Class<T> clazz, Map<String, String> condition, String order) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            return null;
        }
        Cursor cursor = null;
        List<T> list = new ArrayList<>();
        try {
            Table table = clazz.getAnnotation(Table.class);
            QueryCondition qc = new QueryCondition();
            qc.setTable(table.value());
            if (condition != null) {
                qc.setCondition(condition);
            }
            // 这两行的先后顺序不可换
            String sql = qc.getSql();
            List<String> params = qc.getParams();
            if (order != null) {
                sql += " order by " + order + " desc";
            }

            cursor = execRawQuery(sql, params.toArray(new String[params.size()]));


            while (cursor.moveToNext()) {
                T o = clazz.newInstance();
                getObjectByCursorCache(cursor, o, getColumns(clazz));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;

    }

    /**
     * 通过条件获取对象数组,分页
     *
     * @param clazz     类
     * @param condition 条件
     * @param order     排序
     * @param <T>       返回的数组
     * @return 分页数组
     */
    public <T> List<T> queryListPage(Class<T> clazz, Map<String, String> condition, String order, Integer pageNo, Integer pageSize) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        Cursor cursor = null;
        List<T> list = new ArrayList<>();
        try {

            QueryCondition qc = new QueryCondition();
            qc.setTable(table.value());
            if (condition != null) {
                qc.setCondition(condition);
            }
            // 这两行的先后顺序不可换
            String sql = qc.getSql();
            List<String> params = qc.getParams();
            if (order != null) {
                sql += " order by " + order + " desc";
            }
            sql += " limit ?,?";
            params.add(String.valueOf((pageNo - 1) * pageSize));
            params.add(String.valueOf(pageSize));

            cursor = execRawQuery(sql, params.toArray(new String[params.size()]));


            while (cursor.moveToNext()) {
                T o = clazz.newInstance();
                getObjectByCursorCache(cursor, o, getColumns(clazz));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 通过sql语句获取数组
     *
     * @param clazz
     * @param sql
     * @param lstParams
     * @param <T>
     * @return
     */
    public <T> List<T> queryListBySQL(Class<T> clazz, @NonNull String sql, List<String> lstParams) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Cursor cursor = null;
            try {
                String[] params;
                if (lstParams == null || lstParams.isEmpty()) {
                    params = null;
                } else {
                    params = lstParams.toArray(new String[lstParams.size()]);
                }
                cursor = execRawQuery(sql, params);
                List<T> list = new ArrayList<T>();
                while (cursor.moveToNext()) {
                    T t = clazz.newInstance();
                    getObjectByCursorCache(cursor, t, getColumns(clazz));
                    list.add(t);
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    /**
     * 通过sql语句获取对象
     *
     * @param clazz
     * @param sql
     * @param lstParams
     * @param <T>
     * @return
     */
    public <T> T queryBySQL(Class<T> clazz, @NonNull String sql, List<String> lstParams) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Cursor cursor = null;
            try {
                String[] params;
                if (lstParams == null || lstParams.isEmpty()) {
                    params = null;
                } else {
                    params = lstParams.toArray(new String[lstParams.size()]);
                }
                cursor = execRawQuery(sql, params);
                execRawQuery(sql, params);
                T t = clazz.newInstance();
                if (cursor.moveToNext()) {
                    getObjectByCursorCache(cursor, t, getColumns(clazz));
                }
                return t;
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    /**
     * 获取数量
     *
     * @param sql
     * @param lstParams
     * @return
     */
    public int getCountBySQL(@NonNull String sql, List<Object> lstParams) {
        String[] params;
        if (lstParams == null || lstParams.isEmpty()) {
            params = null;
        } else {
            params = lstParams.toArray(new String[lstParams.size()]);
        }
        Cursor cursor = execRawQuery(sql, params);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    /**
     * 通过游标赋值给对象
     *
     * @param cursor  游标
     * @param o       对象
     * @param hmField 变量
     * @throws Exception
     */
    private <T> void getObjectByCursorCache(@NonNull Cursor cursor, T o, @NonNull Map<String, Field> hmField) throws Exception {
        for (String columnName : hmField.keySet()) {
            Field f = hmField.get(columnName);
            //判断表该字段是否有值
            int index = cursor.getColumnIndex(columnName);
            if (!cursor.isNull(index)) {
                Class type = f.getType();
                if (type == Integer.class || type == int.class) {
                    f.set(o, cursor.getInt(index));
                } else if (type == Long.class || type == long.class) {
                    f.set(o, cursor.getLong(index));
                } else if (type == Float.class || type == float.class) {
                    f.set(o, cursor.getFloat(index));
                } else if (type == Double.class || type == double.class) {
                    f.set(o, cursor.getDouble(index));
                } else if (type == Boolean.class || type == boolean.class) {
                    f.set(o, cursor.getInt(index) == 1);
                } else if (type == String.class) {
                    f.set(o, cursor.getString(index));
                } else if (type == Date.class) {
                    f.set(o, new Date(cursor.getLong(index)));
                }

            }


        }
    }

    /**
     * 判断是否已经存在该id,若存在则做更新操作
     *
     * @param tableName
     * @param clazz
     * @param o
     * @return
     */
    private Long checkExits(@NonNull String tableName, Class clazz, @NonNull Object o) {
        Cursor cursor = null;
        try {
            Field field = clazz.getDeclaredField("id");
            field.setAccessible(true);
            Object of = field.get(o);
            if (of != null) {
                StringBuilder sql = new StringBuilder();
                sql.append("select id from ").append(tableName).append(" where id=?");
                cursor = execRawQuery(sql.toString(), new String[]{of.toString()});
                if (cursor.getCount() > 0) {
                    updateById(o, (Long) of);
                    return (Long) of;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return 0L;
    }

    /**
     * 获取注解的变量
     *
     * @param clazz
     * @return
     */
    private Map<String, Field> getColumns(Class clazz) {
        Map<String, Field> columns = mapColumns.get(clazz.getName());
        if (columns == null) {
            Map<String, Field> columnMap = new ArrayMap<>();
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Column.class)) {
                    f.setAccessible(true);
                    columnMap.put(f.getAnnotation(Column.class).value(), f);
                }
            }
            columns = columnMap;
            mapColumns.put(clazz.getName(), columnMap);
        }
        return columns;
    }

    private Cursor execRawQuery(String sql, String[] params) {
        return mHelper.getReadableDatabase().rawQuery(sql, params);
    }

    private void execSQL(String sql, Object[] args) {
        mHelper.getWritableDatabase().execSQL(sql, args);
    }
}



