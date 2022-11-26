package coolBank.coolBank.account.repository;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("select a from Account a join a.member where a.id = :id")
    Account findOneById(@Param("id") Long id);

    @Query("select a from Account a join a.member m where m.email = :email")
    List<Account> findAccountList(@Param("email") String email);

    @Query("select a from Account a join a.member m where m.email = :email and a.category = :category")
    Account findCreditAccount(@Param("email") String email, @Param("category") Category category);
}
