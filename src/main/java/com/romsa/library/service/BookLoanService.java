package com.romsa.library.service;

import com.romsa.library.entity.Book;
import com.romsa.library.entity.BookLoan;
import com.romsa.library.entity.User;
import com.romsa.library.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookLoanService {

    private final BookService bookService;
    private final UserService userService;
    private final BookLoanRepository loanRepository;
    private final MessageSource messageSource;


    public List<BookLoan> findAll() {
        return loanRepository.findAll();
    }


    public void loanBook(Long userId, Long bookId) {
        User user = userService.findById(userId);
        Book book = bookService.findById(bookId);

        if (loanRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new RuntimeException(
                    messageSource.getMessage("user.borrow.error.alreadyLoaned", null, LocaleContextHolder.getLocale())
            );
        }
        if (book.getTotalCopies() < 1) {
            throw new RuntimeException(
                    messageSource.getMessage("user.borrow.error.noCopies", null, LocaleContextHolder.getLocale())
            );
        }
        makeLoan(user, book);
    }

    public void returnBook(Long userId, Long bookId) {
        User user = userService.findById(userId);
        Book book = bookService.findById(bookId);
        deleteLoan(user, book);
    }

    private void makeLoan(User user, Book book) {
        BookLoan loan = new BookLoan(null, user, book, LocalDate.now());

        user.getBorrowedBooks().add(loan);
        book.setTotalCopies(book.getTotalCopies() - 1);
        loanRepository.save(loan);
    }

    private void deleteLoan(User user, Book book) {
        BookLoan loan = loanRepository.findByUserIdAndBookId(user.getId(), book.getId())
                .orElseThrow(() -> new RuntimeException("Loan not found error"));

        user.getBorrowedBooks().remove(loan);
        book.setTotalCopies(book.getTotalCopies() + 1);
        loanRepository.delete(loan);
    }
}
