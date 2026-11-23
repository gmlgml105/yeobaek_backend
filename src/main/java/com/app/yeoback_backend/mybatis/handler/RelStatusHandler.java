package com.app.yeoback_backend.mybatis.handler;

import com.app.yeoback_backend.common.enumeration.RelStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 친구 관계 상태 ↔ PostgreSQL enum(rel_status) 변환. DB에는 소문자 값(RelStatus.getValue())으로 저장한다.
 */
@MappedTypes(RelStatus.class)
public class RelStatusHandler extends BaseTypeHandler<RelStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RelStatus parameter, JdbcType jdbcType) throws SQLException {
        // Types.OTHER: PostgreSQL enum 컬럼에 문자열을 넣을 때 필요
        ps.setObject(i, parameter.getValue(), Types.OTHER);
    }

    @Override
    public RelStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return RelStatus.from(rs.getString(columnName));
    }

    @Override
    public RelStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return RelStatus.from(rs.getString(columnIndex));
    }

    @Override
    public RelStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return RelStatus.from(cs.getString(columnIndex));
    }
}
