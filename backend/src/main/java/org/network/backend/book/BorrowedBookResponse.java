package org.network.backend.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorrowedBookResponse {
    private Integer id;
    private String title;
    private String authorName;
    private String isbn;
    private double rating;
    private boolean returned;
    private boolean returnApproved;
}
