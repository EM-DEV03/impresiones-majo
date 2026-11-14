package com.impresionesmajo.model.dao;

import java.util.*;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/** Identificadores SQL exclusivamente de metadatos internos, valores parametrizados. */
public abstract class BaseDAO<T> {
    protected final JdbcTemplate jdbc;
    private final String table;
    private final List<String> columns;
    private final BeanPropertyRowMapper<T> mapper;
    protected BaseDAO(JdbcTemplate jdbc, String table, Class<T> type, String columns) {
        this.jdbc = jdbc; this.table = table; this.columns = List.of(columns.split(","));
        this.mapper = new BeanPropertyRowMapper<>(type);
    }
    /** Lista registros por id descendente para pantallas administrativas. */
    public List<T> findAll() { return jdbc.query("SELECT * FROM " + table + " ORDER BY id DESC", mapper); }
    /** Busca una fila sin lanzar excepción cuando no existe. */
    public Optional<T> findById(long id) {
        return jdbc.query("SELECT * FROM " + table + " WHERE id=?", mapper, id).stream().findFirst();
    }
    /** Obtiene una fila con bloqueo pesimista; requiere una transacción activa. */
    public T lock(long id) {
        return jdbc.query("SELECT * FROM " + table + " WHERE id=? FOR UPDATE", mapper, id)
            .stream().findFirst().orElseThrow(() -> new IllegalArgumentException("El registro no existe."));
    }
    /** Inserta propiedades no nulas y devuelve la clave generada. */
    public long insert(T value) {
        var bean = new BeanWrapperImpl(value);
        var included = columns.stream().filter(c -> !c.equals("id") && bean.getPropertyValue(property(c)) != null).toList();
        String sql = "INSERT INTO " + table + " (" + String.join(",", included) + ") VALUES (" + String.join(",", Collections.nCopies(included.size(), "?")) + ")";
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            for (int i=0; i<included.size(); i++) ps.setObject(i+1, bean.getPropertyValue(property(included.get(i))));
            return ps;
        }, key);
        long id = Objects.requireNonNull(key.getKey()).longValue();
        bean.setPropertyValue("id", id);
        return id;
    }
    /** Actualiza una fila por id, excluyendo timestamps administrados por la base. */
    public void update(T value) {
        var bean = new BeanWrapperImpl(value);
        var included = columns.stream().filter(c -> !c.equals("id") && !c.startsWith("fecha_creacion") && !c.equals("fecha_actualizacion")).toList();
        var args = new ArrayList<Object>();
        included.forEach(c -> args.add(bean.getPropertyValue(property(c))));
        args.add(bean.getPropertyValue("id"));
        int count = jdbc.update("UPDATE " + table + " SET " + String.join(",", included.stream().map(c -> c + "=?").toList()) + " WHERE id=?", args.toArray());
        if (count != 1) throw new IllegalArgumentException("El registro no existe.");
    }
    private static String property(String column) {
        var parts=column.split("_"); var result=new StringBuilder(parts[0]);
        for(int i=1;i<parts.length;i++) result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        return result.toString();
    }
}
