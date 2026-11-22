package com.app.yeoback_backend.mybatis.handler;

import com.app.yeoback_backend.common.enumeration.Status;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 회원 상태(status) ↔ PostgreSQL enum 변환.
 * DB enum 값은 대문자('ACTIVE' · 'SUSPENDED' · 'WITHDRAWN', create.sql)라서 저장할 때는 enum 이름을 쓰고,
 * 읽을 때는 대소문자와 상관없이 바꾼다. (화면 · JSON 에는 Status.getValue() 의 소문자 값이 나간다)
 * BaseTypeHandler 를 상속해서 null 값은 MyBatis 가 알아서 처리한다.
 */
@MappedTypes(Status.class)
public class StatusHandler extends BaseTypeHandler<Status> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Status parameter, JdbcType jdbcType) throws SQLException {
        // Types.OTHER: PostgreSQL enum 컬럼에 문자열을 넣을 때 필요
        ps.setObject(i, parameter.name(), Types.OTHER);
    }

    @Override
    public Status getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toEnum(rs.getString(columnName));
    }

    @Override
    public Status getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toEnum(rs.getString(columnIndex));
    }

    @Override
    public Status getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toEnum(cs.getString(columnIndex));
    }

    private Status toEnum(String value) {
        return value == null ? null : Status.getStatus(value.trim().toLowerCase());
    }
}
