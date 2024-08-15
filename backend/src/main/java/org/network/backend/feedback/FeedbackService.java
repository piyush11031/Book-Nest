package org.network.backend.feedback;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.network.backend.book.Book;
import org.network.backend.book.BookRepository;
import org.network.backend.book.PageResponse;
import org.network.backend.exception.OperationNotPermittedException;
import org.network.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    private Book getBookById(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID " + bookId));
        return book;
    }
    private static User getUser(Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        return user;
    }
    private static Pageable getPageable(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pageable;
    }

    public Integer saveFeedback(FeedbackRequest request, Authentication connectedUser) {

        var book = getBookById(request.bookId());
        var user = getUser(connectedUser);

        if(book.isArchived() || !book.isSharable()){
            throw new OperationNotPermittedException("Cannot give feedback because Book is Archived or not sharable");
        }
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("Owner cannot give feedback to their own book");
        }

        Feedback feedback = feedbackMapper.toFeedback(request);
        return feedbackRepository.save(feedback).getId();
    }

    public PageResponse<FeedbackResponse> findAllFeedbacksOfBook(Integer bookId, int page, int size, Authentication connectedUser) {
        var book = getBookById(bookId);
        var user = getUser(connectedUser);
        var pageable = getPageable(page, size);

        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);

        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();

        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
