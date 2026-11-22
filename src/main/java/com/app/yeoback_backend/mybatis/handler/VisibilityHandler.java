package com.app.yeoback_backend.mybatis.handler;

import com.app.yeoback_backend.common.enumeration.Visibility;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 공개 범위 ↔ PostgreSQL enum(visibility) 변환. DB에는 소문자 값(Visibility.getValue())으로 저장한다.
 */
@MappedTypes(Visibility.class)
public class VisibilityHandler extends BaseTypeHandler<Visibility> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Visibility parameter, JdbcType jdbcType) throws SQLException {
        // Types.OTHER: PostgreSQL enum 컬럼에 문자열을 넣을 때 필요
        ps.setObject(i, parameter.getValue(), Types.OTHER);
    }

    @Override
    public Visibility getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Visibility.from(rs.getString(columnName));
    }

    @Override
    public Visibility getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Visibility.from(rs.getString(columnIndex));
    }

    @Override
    public Visibility getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Visibility.from(cs.getString(columnIndex));
    }
}
