package coolBank.coolBank.statement.repository;

import coolBank.coolBank.statement.model.Statement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatementRepository extends JpaRepository<Statement, Long> {

    @Query("select s from Statement s join s.account a where a.accountNumber = :accountNumber")
    Page<Statement> findStatementByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);
}
