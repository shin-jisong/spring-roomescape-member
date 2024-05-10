package roomescape.member.domain.repository;

import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;

public interface MemberRepository {
    Member save(Member member);

    boolean existBy(String email, String password);

    Member findByEmail(String email);

    Member findById(long memberId);

    List<Member> findAll();
}
