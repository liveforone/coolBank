package coolBank.coolBank.statement.repository;

import coolBank.coolBank.statement.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatementRepository extends JpaRepository<Statement, Long> {
}
