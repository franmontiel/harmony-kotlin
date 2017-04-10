package com.worldreader.reader.epublib.nl.siegmann.epublib.epub;

import com.worldreader.reader.epublib.nl.siegmann.epublib.domain.Book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A book processor that combines several other bookprocessors
 *
 * Fixes coverpage/coverimage.
 * Cleans up the XHTML.
 *
 * @author paul.siegmann
 */
public class BookProcessorPipeline implements BookProcessor {

  //private Logger log = LoggerFactory.getLogger(BookProcessorPipeline.class);
  private List<BookProcessor> bookProcessors;

  public BookProcessorPipeline() {
    this(null);
  }

  public BookProcessorPipeline(List<BookProcessor> bookProcessingPipeline) {
    this.bookProcessors = bookProcessingPipeline;
  }

  @Override public Book processBook(Book book) {
    if (bookProcessors == null) {
      return book;
    }
    for (BookProcessor bookProcessor : bookProcessors) {
      try {
        book = bookProcessor.processBook(book);
      } catch (Exception e) {
        //log.error(e.getMessage(), e);
      }
    }
    return book;
  }

  public void addBookProcessor(BookProcessor bookProcessor) {
    if (this.bookProcessors == null) {
      bookProcessors = new ArrayList<BookProcessor>();
    }
    this.bookProcessors.add(bookProcessor);
  }

  public void addBookProcessors(Collection<BookProcessor> bookProcessors) {
    if (this.bookProcessors == null) {
      this.bookProcessors = new ArrayList<BookProcessor>();
    }
    this.bookProcessors.addAll(bookProcessors);
  }

  public List<BookProcessor> getBookProcessors() {
    return bookProcessors;
  }

  public void setBookProcessingPipeline(List<BookProcessor> bookProcessingPipeline) {
    this.bookProcessors = bookProcessingPipeline;
  }
}