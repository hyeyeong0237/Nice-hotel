package com.hotel.repository.member;

import com.hotel.constant.Role;
import com.hotel.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member>, MemberRepositoryCustom {

    Member findByEmail(String email);

    Member findByName(String name);

    Member findByIdAndRole(Long id, Role role);

}
