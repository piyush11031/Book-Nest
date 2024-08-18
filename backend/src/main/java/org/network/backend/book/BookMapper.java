package org.network.backend.book;

import lombok.Data;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.network.backend.file.CustomFileUtil;
import org.network.backend.history.BookTransactionHistory;
import org.springframework.stereotype.Service;

import javax.swing.plaf.BorderUIResource;

@Service
public class BookMapper {

    public Book toBook(BookRequest request) {

        return Book.builder()
                .id(request.id())
                .title(request.title())
                .authorName(request.authorName())
                .synopsis(request.synopsis())
                .archived(false)
                .sharable(request.sharable())
                .build();
    }

    public BookResponse toBookResponse(Book book) {

        return BookResponse.builder()
                .id(book.getId())
                .authorName(book.getAuthorName())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .rating(book.getRating())
                .archived(book.isArchived())
                .sharable(book.isSharable())
                .owner(book.getOwner().fullName())
                .cover(CustomFileUtil.readFileFromLocation(book.getBookCover()))
                .build();
    }

    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory bookTransactionHistory) {

        return BorrowedBookResponse.builder()
                .id(bookTransactionHistory.getBook().getId())
                .title(bookTransactionHistory.getBook().getTitle())
                .authorName(bookTransactionHistory.getBook().getAuthorName())
                .isbn(bookTransactionHistory.getBook().getIsbn())
                .rating(bookTransactionHistory.getBook().getRating())
                .returned(bookTransactionHistory.isReturned())
                .returnApproved(bookTransactionHistory.isReturnApproved())
                .build();
    }
}
