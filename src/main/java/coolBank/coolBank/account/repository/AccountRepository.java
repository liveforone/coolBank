package coolBank.coolBank.account.repository;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("select a from Account a join fetch a.member where a.id = :id")
    Account findOneById(@Param("id") Long id);

    @Query("select a from Account a join fetch a.member where a.accountNumber = :accountNumber")
    Account findOneByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("select a from Account a join fetch a.member m where m.email = :email")
    Account findOneByEmail(@Param("email") String email);

    @Query("select a from Account a join fetch a.member m where m.email = :email")
    List<Account> findAccountList(@Param("email") String email);

    @Query("select a from Account a join fetch a.member m where m.email = :email and a.category = :category")
    Account findCreditAccount(@Param("email") String email, @Param("category") Category category);

    @Modifying
    @Query("update Account a set a.balance = a.balance + :money where a.accountNumber = :accountNumber")
    void depositAccount(@Param("accountNumber") String accountNumber, @Param("money") int money);

    @Modifying
    @Query("update Account a set a.balance = a.balance - :money where a.accountNumber = :accountNumber")
    void withdrawAccount(@Param("accountNumber") String accountNumber, @Param("money") int money);
}
