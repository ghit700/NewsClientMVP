package com.newsclient.nan.newsclient.components.db;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 查询条件辅助类（实现简单的单表查询，表的别名为：t）
 * <p>
 * 若有不完善的地方，再做修改
 *
 * @author zhengjinfeng
 */
public class QueryCondition {
    /**
     * 表名
     **/
    private String table;
    /**
     * select子句，默认查询全部
     **/
    private String select;
    /**
     * 条件
     **/
    private Map<String, String> condition = new ArrayMap<String, String>();
    /**
     * 查询参数
     **/
    private List<String> params = new ArrayList<String>();

    public QueryCondition() {

    }

    public QueryCondition(String table, List<String> params) {
        this.table = table;
        this.params = params;
    }

    /**
     * 表名
     **/
    public String getTable() {
        return table;
    }

    /**
     * 表名
     **/
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * select子句，默认查询全部
     **/
    public String getSelect() {
        return select;
    }

    /**
     * select子句，默认查询全部
     **/
    public void setSelect(String select) {
        this.select = select;
    }


    /**
     * 条件
     **/
    public Map<String, String> getCondition() {
        return condition;
    }

    /**
     * 条件
     **/
    public void setCondition(Map<String, String> condition) {
        this.condition = condition;
    }

    /**
     * 查询参数
     **/
    public List<String> getParams() {
        return params;
    }

    /**
     * 查询参数
     **/
    public void setParams(List<String> params) {
        this.params = params;
    }

    private void concat(StringBuffer sql) {
        if (condition != null && !condition.isEmpty()) {
            Set<String> keys = condition.keySet();
            int last_;
            String name;
            String type;
            String value;
            String and = " and t.";
            for (String key : keys) {
                last_ = key.lastIndexOf("_");
                if (last_ == -1)
                    continue;
                name = key.substring(0, last_);
                type = key.substring(last_ + 1);
                value = condition.get(key);

                if ("eq".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append("=?");
                    params.add(value);
                } else if ("neq".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append("<>?");
                    params.add(value);
                } else if ("isn".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" is null");
                } else if ("isnn".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" is not null");
                } else if ("like".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" like ?");
                    params.add('%' + value + '%');
                } else if ("likes".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" like ?");
                    params.add(value + '%');
                } else if ("likee".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" like ?");
                    params.add('%' + value);
                } else if ("gt".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" > ?");
                    params.add(value);
                } else if ("lt".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" < ?");
                    params.add(value);
                } else if ("ge".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" >= ?");
                    params.add(value);
                } else if ("le".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" <= ?");
                    params.add(value);
                } else if ("in".equalsIgnoreCase(type)) {
                    sql.append(and).append(name).append(" in (").append(value).append(")");
                } else if ("sql".equalsIgnoreCase(type)) {
                    sql.append(" and (").append(value).append(")");
                }

                // TODO 其他类型查询待完善
            }
        }
    }


    /**
     * 获取查询语句（先执行该方法，查询参数才不会为空）
     **/
    public String getSql() {
        StringBuffer sql;
        if (select != null && !select.equals("")) {
            sql = new StringBuffer(select).append(table).append(" t where 1=1");
        } else {
            sql = new StringBuffer("select t.* from ").append(table).append(" t where 1=1");
        }
        this.concat(sql);
        return sql.toString();
    }
}
