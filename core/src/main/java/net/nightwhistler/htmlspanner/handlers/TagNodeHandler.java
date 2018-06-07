package net.nightwhistler.htmlspanner.handlers;

import android.text.SpannableStringBuilder;
import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.SpanStack;
import org.htmlcleaner.TagNode;

/**
 * A TagNodeHandler handles a specific type of tag (a, img, p, etc), and adds
 * the correct spans to a SpannableStringBuilder.
 *
 * For example: the TagNodeHandler for i (italic) tags would do
 *
 * <tt>
 * public void handleTagNode( TagNode node, SpannableStringBuilder builder, 
 * 		int start, int end ) {
 * 		builder.setSpan(new StyleSpan(Typeface.ITALIC), 
 * 			start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 * }
 * </tt>
 *
 * @author Alex Kuiper
 *
 */
public abstract class TagNodeHandler {

  private HtmlSpanner spanner;

  /**
   * Returns a reference to the HtmlSpanner.
   *
   * @return the HtmlSpanner;
   */
  protected HtmlSpanner getSpanner() {
    return spanner;
  }

  /**
   * Called by HtmlSpanner when this TagNodeHandler is registered.
   *
   * @param spanner
   */
  public void setSpanner(HtmlSpanner spanner) {
    this.spanner = spanner;
  }

  /**
   * Called before the children of this node are handled, allowing for text to
   * be inserted before the childrens' text.
   *
   * Default implementation is a no-op.
   *
   * @param node
   * @param builder
   */
  public void beforeChildren(TagNode node, SpannableStringBuilder builder, SpanStack spanStack) {

  }

  /**
   * If this TagNodeHandler takes care of rendering the content.
   *
   * If true, the parser will not add the content itself.
   *
   * @return
   */
  public boolean rendersContent() {
    return false;
  }

  /**
   * Handle the given node and add spans if needed.
   *
   * @param node
   *            the node to handle
   * @param builder
   *            the current stringbuilder
   * @param start
   *            start position of inner text of this node
   * @param end
   *            end position of inner text of this node.
   *
   * @param spanStack stack to push new spans on
   */
  public abstract void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack spanStack);

  /**
   * Utility method to append newlines while making sure that there are never
   * more than 2 consecutive newlines in the text (if whitespace stripping was
   * enabled).
   *
   * @param builder
   * @return true if a newline was added
   */
  protected boolean appendNewLine(SpannableStringBuilder builder) {
    int len = builder.length();

    if (spanner.isStripExtraWhiteSpace()) {
      // Should never have more than 2 \n characters in a row.
      if (len > 2 && builder.charAt(len - 1) == '\n' && builder.charAt(len - 2) == '\n') {
        return false;
      }
    }

    builder.append("\n");

    return true;
  }
}