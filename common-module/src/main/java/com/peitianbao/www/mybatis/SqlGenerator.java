package com.peitianbao.www.mybatis;

import com.peitianbao.www.mybatis.annotation.Column;
import com.peitianbao.www.mybatis.annotation.Table;

import java.lang.reflect.Field;

/**
 * @author leg
 */
public class SqlGenerator {
    public static String generateInsertSql(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new RuntimeException("没有@Table注解");
        }
        String tableName = tableAnnotation.value();

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                columns.append(columnAnnotation.value()).append(",");
                placeholders.append("#{").append(field.getName()).append("},");
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                columns.deleteCharAt(columns.length() - 1),
                placeholders.deleteCharAt(placeholders.length() - 1));
    }

    public static String generateDeleteByIdSql(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new RuntimeException("没有@Table注解");
        }
        String tableName = tableAnnotation.value();

        return String.format("DELETE FROM %s WHERE id = #{id}", tableName);
    }

    public static String generateUpdateSql(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new RuntimeException("没有@Table注解");
        }
        String tableName = tableAnnotation.value();

        StringBuilder setClause = new StringBuilder();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !"id".equals(field.getName())) {
                setClause.append(columnAnnotation.value()).append(" = #{").append(field.getName()).append("},");
            }
        }

        return String.format("UPDATE %s SET %s WHERE id = #{id}",
                tableName,
                setClause.deleteCharAt(setClause.length() - 1));
    }

    public static String generateSelectByIdSql(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new RuntimeException("没有@Table注解");
        }
        String tableName = tableAnnotation.value();

        return String.format("SELECT * FROM %s WHERE id = #{id}", tableName);
    }

    public static String generateSelectAllSql(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new RuntimeException("没有@Table注解");
        }
        String tableName = tableAnnotation.value();

        return String.format("SELECT * FROM %s", tableName);
    }
}
