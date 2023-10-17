package com.web.billim.member.dto.response;

import com.web.billim.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfoResponse {
    private long memberId;
    private String email;
    private String nickname;
    private String address;
    private String profileImageUrl;
    private String type;

    public static MemberInfoResponse from(Member member) {
        return MemberInfoResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .address(member.getAddress())
                .profileImageUrl(member.getProfileImageUrl())
                .type(member.getMemberType())
                .build();
    }
}
