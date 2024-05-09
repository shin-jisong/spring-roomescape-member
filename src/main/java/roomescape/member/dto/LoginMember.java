package roomescape.member.dto;

import roomescape.member.domain.Role;

public record LoginMember(long id, String name, String email, Role role) {
}
