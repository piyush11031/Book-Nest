package org.network.backend.history;

import org.network.backend.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {
    @Query("""
        SELECT history FROM BookTransactionHistory history
            WHERE history.user.id = :userId
""")
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, Integer userId);

    //"history.book.owner.id = :userId" finds all returned books that belong to the owner
    @Query("""
    SELECT history FROM BookTransactionHistory history
        WHERE history.book.owner.id = :userId
""")
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Integer userId);

    @Query("""
    SELECT (COUNT (*) > 0) AS isBorrowed
    FROM BookTransactionHistory history
        WHERE history.user.id = :userId
            AND history.book.id = :bookId
            AND history.returnApproved = false
""")
    boolean isBookAlreadyBorrowedByUser(Integer bookId, Integer id);

    @Query("""
    SELECT history FROM BookTransactionHistory history
    WHERE history.user.id = :userId
        AND history.book.id = :bookId
        AND history.returned = false
        AND history.returnApproved = false
""")
    Optional<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer id);

    @Query("""
    SELECT history FROM BookTransactionHistory history
        WHERE history.book.owner.id = :ownerId
            AND history.book.id = :bookId
            AND history.returned = true
            AND history.returnApproved =false
""")
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer ownerId);
}
