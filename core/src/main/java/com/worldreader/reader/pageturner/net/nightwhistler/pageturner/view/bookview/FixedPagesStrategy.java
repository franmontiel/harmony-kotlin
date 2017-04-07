/*
 * Copyright (C) 2013 Alex Kuiper
 *
 * This file is part of PageTurner
 *
 * PageTurner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PageTurner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PageTurner.  If not, see <http://www.gnu.org/licenses/>.*
 */

package com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview;

import android.graphics.Canvas;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;
import jedi.option.Option;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.configuration.Configuration;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.epub.PageTurnerSpine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static jedi.option.Options.*;

public class FixedPagesStrategy implements PageChangeStrategy {

  private static final String TAG = FixedPagesStrategy.class.getSimpleName();

  private Configuration config;

  private StaticLayoutFactory layoutFactory;

  private Spanned text;

  private int pageNum;

  private List<Integer> pageOffsets = new ArrayList<>();

  private BookView bookView;
  private TextView childView;

  private int storedPosition = -1;

  @Override public void setBookView(BookView bookView) {
    this.bookView = bookView;
    this.childView = bookView.getInnerView();
  }

  @Override public int getSizeChartDisplayed() {
    if (this.childView != null && this.childView.getText() != null) {
      return this.childView.getText().length();
    } else {
      return 0;
    }
  }

  public void setLayoutFactory(StaticLayoutFactory layoutFactory) {
    this.layoutFactory = layoutFactory;
  }

  public void setConfig(Configuration config) {
    this.config = config;
  }

  @Override public void clearStoredPosition() {
    this.pageNum = 0;
    this.storedPosition = 0;
  }

  @Override public void clearText() {
    this.text = new SpannableStringBuilder("");
    this.childView.setText(text);
    this.pageOffsets = new ArrayList<Integer>();
  }

  /**
   * Returns the current page INSIDE THE SECTION.
   */
  public int getCurrentPage() {
    return this.pageNum;
  }

  public List<Integer> getPageOffsets() {
    return new ArrayList<>(this.pageOffsets);
  }

  public List<Integer> getPageOffsets(CharSequence text, boolean includePageNumbers) {

    if (text == null) {
      return emptyList();
    }

    List<Integer> pageOffsets = new ArrayList<Integer>();

    TextPaint textPaint = bookView.getInnerView().getPaint();
    int boundedWidth = bookView.getInnerView().getMeasuredWidth();

    Log.d(TAG, "Page width: " + boundedWidth);

    StaticLayout layout =
        layoutFactory.create(text, textPaint, boundedWidth, bookView.getLineSpacing());

    if (layout == null) {
      return emptyList();
    }

    Log.d(TAG, "Layout height: " + layout.getHeight());

    layout.draw(new Canvas());

    //Subtract the height of the top margin
    int pageHeight = bookView.getMeasuredHeight() - bookView.getVerticalMargin();

    if (includePageNumbers) {
      String bottomSpace = "0\n";

      StaticLayout numLayout =
          layoutFactory.create(bottomSpace, textPaint, boundedWidth, bookView.getLineSpacing());
      numLayout.draw(new Canvas());

      //Subtract the height needed to show page numbers, or the
      //height of the margin, whichever is more
      pageHeight = pageHeight - Math.max(numLayout.getHeight(), bookView.getVerticalMargin());
    } else {
      //Just subtract the bottom margin
      pageHeight = pageHeight - bookView.getVerticalMargin();
    }

    Log.d(TAG, "Got pageHeight " + pageHeight);

    int totalLines = layout.getLineCount();
    int topLineNextPage = -1;
    int pageStartOffset = 0;

    while (topLineNextPage < totalLines - 1) {

      Log.d(TAG, "Processing line " + topLineNextPage + " / " + totalLines);

      int topLine = layout.getLineForOffset(pageStartOffset);
      topLineNextPage = layout.getLineForVertical(layout.getLineTop(topLine) + pageHeight);

      Log.d(TAG, "topLine " + topLine + " / " + topLineNextPage);
      if (topLineNextPage == topLine) { //If lines are bigger than can fit on a page
        topLineNextPage = topLine + 1;
      }

      int pageEnd = layout.getLineEnd(topLineNextPage - 1);

      Log.d(TAG, "pageStartOffset=" + pageStartOffset + ", pageEnd=" + pageEnd);

      if (pageEnd > pageStartOffset) {
        if (text.subSequence(pageStartOffset, pageEnd).toString().trim().length() > 0) {
          pageOffsets.add(pageStartOffset);
        }
        pageStartOffset = layout.getLineStart(topLineNextPage);
      }
    }

    return pageOffsets;
  }

  @Override public void reset() {
    clearStoredPosition();
    this.pageOffsets.clear();
    clearText();
  }

  private void updatePageNumber() {
    for (int i = 0; i < this.pageOffsets.size(); i++) {
      if (this.pageOffsets.get(i) > this.storedPosition) {
        this.pageNum = i - 1;
        return;
      }
    }

    this.pageNum = this.pageOffsets.size() - 1;
  }

  @Override public void updatePosition() {

    if (pageOffsets.isEmpty() || text.length() == 0 || this.pageNum == -1) {
      return;
    }

    if (storedPosition != -1) {
      updatePageNumber();
    }

    CharSequence sequence = getTextForPage(this.pageNum).getOrElse("");

    if (sequence.length() > 0) {

      // #555 Remove \n at the end of sequence which get InnerView size changed
      int endIndex = sequence.length();
      while (sequence.charAt(endIndex - 1) == '\n') {
        endIndex--;
      }

      sequence = sequence.subSequence(0, endIndex);
    }

    try {
      this.childView.setText(sequence);

      //If we get an error setting the formatted text,
      //strip formatting and try again.

    } catch (IndexOutOfBoundsException ie) {
      this.childView.setText(sequence.toString());
    }
  }

  private Option<CharSequence> getTextForPage(int page) {

    if (pageOffsets.size() < 1 || page < 0) {
      return none();
    } else if (page >= pageOffsets.size() - 1) {
      int startOffset = pageOffsets.get(pageOffsets.size() - 1);

      if (startOffset >= 0 && startOffset <= text.length() - 1) {
        return some(applySpans(this.text.subSequence(startOffset, text.length()), startOffset));
      } else {
        return some(applySpans(text, 0));
      }
    } else {
      int start = this.pageOffsets.get(page);
      int end = this.pageOffsets.get(page + 1);
      return some(applySpans(this.text.subSequence(start, end), start));
    }
  }

  private CharSequence applySpans(CharSequence text, int offset) {
    return text;
  }

  @Override public void setPosition(int pos) {
    this.storedPosition = pos;
  }

  @Override public void setRelativePosition(double position) {

    int intPosition = (int) (this.text.length() * position);
    setPosition(intPosition);
  }

  public int getTopLeftPosition() {

    if (pageOffsets.isEmpty()) {
      return 0;
    }

    if (this.pageNum >= this.pageOffsets.size()) {
      return this.pageOffsets.get(this.pageOffsets.size() - 1);
    }

    return this.pageOffsets.get(this.pageNum);
  }

  public int getProgressPosition() {

    if (storedPosition > 0 || this.pageOffsets.isEmpty() || this.pageNum == -1) {
      return this.storedPosition;
    }

    return getTopLeftPosition();
  }

  public Option<Spanned> getText() {
    return option(text);
  }

  public boolean isAtEnd() {
    return pageNum == this.pageOffsets.size() - 1;
  }

  public boolean isAtStart() {
    return this.pageNum == 0;
  }

  public boolean isScrolling() {
    return false;
  }

  @Override public Option<CharSequence> getNextPageText() {

    if (isAtEnd()) {
      return none();
    }

    return getTextForPage(this.pageNum + 1);
  }

  @Override public Option<CharSequence> getPreviousPageText() {
    if (isAtStart()) {
      return none();
    }

    return getTextForPage(this.pageNum - 1);
  }

  @Override public void pageDown() {

    this.storedPosition = -1;

    if (isAtEnd()) {
      PageTurnerSpine spine = bookView.getSpine();

      if (spine == null || !spine.navigateForward()) {
        return;
      }

      this.clearText();
      this.pageNum = 0;
      bookView.loadText();
    } else {
      this.pageNum = Math.min(pageNum + 1, this.pageOffsets.size() - 1);
      updatePosition();
    }
  }

  @Override public void pageUp() {

    this.storedPosition = -1;

    if (isAtStart()) {
      PageTurnerSpine spine = bookView.getSpine();

      if (spine == null || !spine.navigateBack()) {
        return;
      }

      this.clearText();
      this.storedPosition = Integer.MAX_VALUE;
      this.bookView.loadText();
    } else {
      this.pageNum = Math.max(pageNum - 1, 0);
      updatePosition();
    }
  }

  @Override public void loadText(Spanned text) {
    this.text = text;
    this.pageNum = 0;
    this.pageOffsets = getPageOffsets(text, config.isShowPageNumbers());
  }

  @Override public void updateGUI() {
    updatePosition();
  }
}
