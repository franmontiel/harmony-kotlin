package com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.nodehandler;

import android.text.SpannableStringBuilder;
import net.nightwhistler.htmlspanner.SpanStack;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import org.htmlcleaner.TagNode;

/**
 * Many books use
 * <p>
 * and
 * <h1>tags as anchor points. This class harvests those point by wrapping
 * the original handler.
 */
public class AnchorHandler extends TagNodeHandler {

  private final TagNodeHandler wrappedHandler;

  private AnchorCallback callback;

  public AnchorHandler(TagNodeHandler wrappedHandler) {
    this.wrappedHandler = wrappedHandler;
  }

  @Override public void beforeChildren(TagNode node, SpannableStringBuilder builder, SpanStack spanStack) {
    this.wrappedHandler.beforeChildren(node, builder, spanStack);
  }

  @Override public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack spanStack) {
    final String id = node.getAttributeByName("id");
    if (id != null) {
      callback.registerAnchor(id, start);
    }

    wrappedHandler.handleTagNode(node, builder, start, end, spanStack);
  }

  public void setCallback(AnchorCallback callback) {
    this.callback = callback;
  }

  public TagNodeHandler getWrappedHandler() {
    return wrappedHandler;
  }

  public interface AnchorCallback {

    void registerAnchor(String anchor, int position);
  }
}