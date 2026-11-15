package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.common.enumeration.OAuthProvider;
import com.app.yeoback_backend.domain.OAuthVO;
import com.app.yeoback_backend.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface OAuthMapper {
    //    회원가입
    public void insert(OAuthVO oAuthVO);
    //    이메일로 조회
    public Optional<MemberDTO> selectMemberByLoginId(@Param("loginId") String loginId,
                                                         @Param("provider") OAuthProvider provider);
}

