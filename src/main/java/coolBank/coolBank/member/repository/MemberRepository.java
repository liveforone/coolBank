package coolBank.coolBank.member.repository;

import coolBank.coolBank.member.model.Grade;
import coolBank.coolBank.member.model.Member;
import coolBank.coolBank.member.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);

    @Query("select m from Member m where m.nickname = :nickname")
    Member findByNickname(@Param("nickname") String nickname);

    @Modifying
    @Query("update Member m set m.auth = :auth where m.email = :email")
    void updateAuth(@Param("auth") Role auth, @Param("email") String email);

    @Modifying
    @Query("update Member m set m.nickname = :nickname where m.email = :email")
    void updateNickname(@Param("nickname") String nickname, @Param("email") String email);

    @Modifying
    @Query("update Member m set m.email = :newEmail where m.email = :oldEmail")
    void updateEmail(@Param("oldEmail") String oldEmail, @Param("newEmail") String newEmail);

    @Modifying
    @Query("update Member m set m.password = :password where m.id = :id")
    void updatePassword(@Param("id") Long id, @Param("password") String password);

    @Modifying
    @Query("update Member m set m.grade = :grade where m.id = :id")
    void updateGrade(@Param("grade") Grade grade, @Param("id") Long id);
}
