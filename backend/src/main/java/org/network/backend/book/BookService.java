package org.network.backend.book;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.network.backend.exception.OperationNotPermittedException;
import org.network.backend.file.FileStorageService;
import org.network.backend.history.BookTransactionHistory;
import org.network.backend.history.BookTransactionHistoryRepository;
import org.network.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;

    private static Pageable getPageable(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return pageable;
    }

    private static User getUser(Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        return user;
    }
    private Book getBookById(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID " + bookId));
        return book;
    }

    public Integer save(BookRequest request, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        var b = bookRepository.save(book);
        return b.getId();

    }

    public BookResponse findById(Integer bookId) {

        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Pageable pageable = getPageable(page, size);
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Pageable pageable = getPageable(page, size);
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Pageable pageable = getPageable(page, size);

        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());

        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Pageable pageable = getPageable(page, size);

        Page<BookTransactionHistory> allReturnedBooks = transactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());

        List<BorrowedBookResponse> bookResponses = allReturnedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                allReturnedBooks.getNumber(),
                allReturnedBooks.getSize(),
                allReturnedBooks.getTotalElements(),
                allReturnedBooks.getTotalPages(),
                allReturnedBooks.isFirst(),
                allReturnedBooks.isLast()
        );
    }
    public Integer updateSharableStatus(Integer bookId, Authentication connectedUser) {

        User user = getUser(connectedUser);
        Book book = getBookById(bookId);

        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("Only owner of book can update the sharable status");
        }

        book.setSharable(!book.isSharable()); //inverse the value
        return bookRepository.save(book).getId();
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        User user = getUser(connectedUser);
        Book book = getBookById(bookId);

        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("Only owner of book can update the archive status");
        }

        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {

        var user = getUser(connectedUser);
        var book = getBookById(bookId);

        if(book.isArchived() || !book.isSharable()){
            throw new OperationNotPermittedException("Book cannot be borrowed because it is Archived or not sharable");
        }
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("Owner cannot borrow his own book");
        }
        final boolean isAlreadyBorrowed = transactionHistoryRepository.isBookAlreadyBorrowedByUser(bookId, user.getId());
        if(isAlreadyBorrowed){
            throw new OperationNotPermittedException("Requested book is already borrowed");
        }

        BookTransactionHistory history = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return transactionHistoryRepository.save(history).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        var user = getUser(connectedUser);
        var book = getBookById(bookId);

        if(book.isArchived() || !book.isSharable()){
            throw new OperationNotPermittedException("Book cannot be returned because it is Archived or not sharable");
        }
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("Owner cannot return his own book");
        }

        BookTransactionHistory history = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

        history.setReturned(true);
        return transactionHistoryRepository.save(history).getId();
    }

    public Integer approvedReturnedBook(Integer bookId, Authentication connectedUser) {

        var user = getUser(connectedUser);
        var book = getBookById(bookId);

        if(book.isArchived() || !book.isSharable()){
            throw new OperationNotPermittedException("Book cannot be returned because it is Archived or not sharable");
        }
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You can't approve return of a book that you don't own");
        }

        BookTransactionHistory history = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet, so you cannot approve the return"));

        history.setReturnApproved(true);
        return transactionHistoryRepository.save(history).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        var user = getUser(connectedUser);
        var book = getBookById(bookId);

        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover); //bookCover is the file path of the uploaded file
        bookRepository.save(book);
    }
}
