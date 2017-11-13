package com.worldreader.reader.wr.fragments;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.mobilejazz.logger.library.Logger;
import com.worldreader.core.R;
import com.worldreader.core.analytics.Analytics;
import com.worldreader.core.analytics.event.AnalyticsEventConstants;
import com.worldreader.core.analytics.event.BasicAnalyticsEvent;
import com.worldreader.core.application.helper.AndroidFutures;
import com.worldreader.core.application.helper.reachability.Reachability;
import com.worldreader.core.application.helper.ui.Dimens;
import com.worldreader.core.application.ui.dialog.DialogFactory;
import com.worldreader.core.application.ui.widget.TutorialView;
import com.worldreader.core.application.ui.widget.discretebar.DiscreteSeekBar;
import com.worldreader.core.common.date.Dates;
import com.worldreader.core.common.deprecated.callback.CompletionCallback;
import com.worldreader.core.common.deprecated.error.ErrorCore;
import com.worldreader.core.datasource.StreamingBookDataSource;
import com.worldreader.core.domain.interactors.dictionary.GetWordDefinitionInteractor;
import com.worldreader.core.domain.interactors.user.FinishBookInteractor;
import com.worldreader.core.domain.interactors.user.SendReadPagesInteractor;
import com.worldreader.core.domain.model.BookMetadata;
import com.worldreader.core.domain.model.UserFlow;
import com.worldreader.core.domain.model.WordDefinition;
import com.worldreader.core.userflow.UserFlowTutorial;
import com.worldreader.core.userflow.model.TutorialModel;
import com.worldreader.reader.epublib.nl.siegmann.epublib.domain.Author;
import com.worldreader.reader.epublib.nl.siegmann.epublib.domain.Book;
import com.worldreader.reader.epublib.nl.siegmann.epublib.domain.Metadata;
import com.worldreader.reader.epublib.nl.siegmann.epublib.domain.Resource;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.animation.Animator;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.configuration.ColorProfile;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.configuration.Configuration;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.configuration.ReadingDirection;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.dto.TocEntry;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.AnimatedImageView;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.FastBitmapDrawable;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.ActionModeListener;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.BookNavigationGestureDetector;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.BookView;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.BookViewListener;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.TextSelectionCallback;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.resources.FileEpubResourcesLoader;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.resources.ResourcesLoader;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.resources.TextLoader;
import com.worldreader.reader.pageturner.net.nightwhistler.pageturner.view.bookview.spanner.HtmlSpannerFactory;
import com.worldreader.reader.wr.activities.AbstractReaderActivity;
import com.worldreader.reader.wr.helper.BrightnessManager;
import com.worldreader.reader.wr.helper.systemUi.SystemUiHelper;
import com.worldreader.reader.wr.widget.DefinitionView;
import jedi.option.Option;
import net.nightwhistler.htmlspanner.spans.CenterSpan;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static jedi.functional.FunctionalPrimitives.firstOption;

public abstract class AbstractReaderFragment extends Fragment implements BookViewListener, SystemUiHelper.OnVisibilityChangeListener {

  public static final String CHANGE_FONT_KEY = "change_font_key";
  public static final String CHANGE_BACKGROUND_KEY = "change.background.key";

  private static final String TAG = AbstractReaderFragment.class.getSimpleName();
  private static final String POS_KEY = "offset:";
  private static final String IDX_KEY = "index:";

  private final Handler handler = new Handler(Looper.getMainLooper());
  private final SavedConfigState savedConfigState = new SavedConfigState();

  private Context context;

  private TextLoader textLoader;
  private String bookTitle;
  private String author;
  protected BookMetadata bookMetadata;
  private List<TocEntry> tableOfContents;

  private ProgressDialog progressDialog;
  private OnBookTocEntryListener bookTocEntryListener;

  private ViewSwitcher viewSwitcher;
  private BookView bookView;
  private AnimatedImageView dummyView;
  private TextView pageNumberView;
  private TextView readingTitleProgressTv;
  private DiscreteSeekBar chapterProgressDsb;
  private TextView chapterProgressPagesTv;
  private DefinitionView definitionView;
  private TutorialView tutorialView;
  private View containerTutorialView;
  private View progressContainer;

  protected DICompanion di;

  protected int currentScrolledPages = 0;
  private boolean hasSharedText;

  private enum Orientation {
    HORIZONTAL, VERTICAL
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    try {
      bookTocEntryListener = (OnBookTocEntryListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
    }
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reader, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    this.context = getActivity();

    di = onProvideDICompanionObject();

    // Intent content checked properly in AbstractReaderActivity (if BookMetadata is not present we can't continue further)
    final Intent intent = getActivity().getIntent();
    this.bookMetadata = (BookMetadata) intent.getSerializableExtra(AbstractReaderActivity.BOOK_METADATA_KEY);

    onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_INITIALIZE_EVENT);

    if (bookMetadata.getCollectionId() > 0) {
      onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_START_BOOK_FROM_COLLECTION_EVENT);
    }

    final boolean isFontChanged = intent.getBooleanExtra(CHANGE_FONT_KEY, false);
    final boolean isBackgroundChanged = intent.getBooleanExtra(CHANGE_BACKGROUND_KEY, false);

    onNotifyGamificationFontOrBackgroundReaderEvents(isFontChanged, isBackgroundChanged);
    onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_READ_ON_X_CONTINOUS_DAYS_EVENT);
    onReaderFragmentEvent(BookReaderEvents.SAVE_CURRENTLY_BOOK_READING_EVENT);

    final View view = getView();

    this.viewSwitcher = (ViewSwitcher) view.findViewById(R.id.reading_fragment_main_container);
    this.bookView = (BookView) view.findViewById(R.id.reading_fragment_bookView);
    this.dummyView = (AnimatedImageView) view.findViewById(R.id.reading_fragment_dummy_view);
    this.pageNumberView = (TextView) view.findViewById(R.id.reading_fragment_page_number_view);

    this.readingTitleProgressTv = (TextView) view.findViewById(R.id.reading_fragment_progress_chapter_title_tv);
    this.chapterProgressDsb = (DiscreteSeekBar) view.findViewById(R.id.reading_fragment_chapter_progress_dsb);
    this.chapterProgressPagesTv = (TextView) view.findViewById(R.id.reading_fragment_chapter_progress_pages_tv);
    this.definitionView = (DefinitionView) view.findViewById(R.id.reading_fragment_word_definition_dv);
    this.tutorialView = (TutorialView) view.findViewById(R.id.reading_fragment_tutorial_view);
    this.containerTutorialView = view.findViewById(R.id.reading_fragment_container_tutorial_view);
    this.progressContainer = view.findViewById(R.id.reading_fragment_chapter_progress_container);

    final ResourcesLoader resourcesLoader =
        new FileEpubResourcesLoader(di.logger); //new StreamingResourcesLoader(bookMetadata, di.streamingBookDataSource, di.logger);

    this.textLoader = new TextLoader(HtmlSpannerFactory.create(di.config), resourcesLoader);

    this.bookView.init(bookMetadata, resourcesLoader, textLoader, di.streamingBookDataSource, di.logger);
    this.bookView.setListener(this);
    this.bookView.setTextSelectionCallback(new TextSelectionCallback() {
      @Override public void lookupDictionary(String text) {
        if (di.reachability.isReachable()) {
          if (text != null) {
            text = text.trim();

            final StringTokenizer st = new StringTokenizer(text);
            if (st.countTokens() == 1) {
              definitionView.showLoading();
              showDefinitionView();
              final ListenableFuture<WordDefinition> getWordDefinitionFuture = di.getWordDefinitionInteractor.execute(text);
              AndroidFutures.addCallbackMainThread(getWordDefinitionFuture, new FutureCallback<WordDefinition>() {
                @Override public void onSuccess(@Nullable WordDefinition result) {
                  if (isAdded()) {
                    definitionView.setWordDefinition(result);
                    definitionView.showDefinition();
                  }
                }

                @Override public void onFailure(@NonNull Throwable t) {
                  // Ignore error
                }
              });
            } else {
              Toast.makeText(getContext(), R.string.ls_book_reading_select_one_word, Toast.LENGTH_SHORT).show();
            }
          }
        } else {
          final MaterialDialog networkErrorDialog =
              DialogFactory.createDialog(getContext(), R.string.ls_error_signup_network_title, R.string.ls_error_definition_not_internet,
                  R.string.ls_generic_accept, DialogFactory.EMPTY, new DialogFactory.ActionCallback() {
                    @Override public void onResponse(MaterialDialog dialog, final DialogFactory.Action action) {
                    }
                  });

          networkErrorDialog.setCancelable(false);
          networkErrorDialog.show();
        }
      }

      @Override public void share(String selectedText) {
        String author = null;

        if (!bookView.getBook().getMetadata().getAuthors().isEmpty()) {
          author = bookView.getBook().getMetadata().getAuthors().get(0).toString();
        }

        String text;
        if (author == null) {
          text = bookTitle + " \n\n" + selectedText;
        } else {
          text = bookTitle + ", " + author + "\n\n" + selectedText;
        }

        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        startActivity(Intent.createChooser(sendIntent, getString(R.string.ls_generic_share)));
        setShareFlag();
      }
    }, new ActionModeListener() {
      @Override public void onCreateActionMode() {
        if (getSystemUiHelper() != null && !getSystemUiHelper().isShowing()) {
          getSystemUiHelper().show();
        }
      }

      @Override public void onDestroyActionMode() {
        if (getSystemUiHelper() != null && getSystemUiHelper().isShowing()) {
          getSystemUiHelper().hide();
        }
      }
    });

    // Hide the tutorial view because we need to wait that the book is loaded
    setTutorialViewVisibility(View.INVISIBLE);

    this.definitionView.setOnClickCrossListener(new DefinitionView.OnClickCrossListener() {
      @Override public void onClick(DefinitionView view) {
        hideDefinitionView();
      }
    });

    final int pl = progressContainer.getPaddingLeft();
    final int pt = progressContainer.getPaddingTop();
    final int pr = progressContainer.getPaddingRight();
    final int pb = progressContainer.getPaddingBottom() + Dimens.obtainNavBarHeight(context);
    this.progressContainer.setPadding(pl, pt, pr, pb);

    this.chapterProgressDsb.setEnabled(true);
    this.chapterProgressDsb.setOnProgressChangeListener(new DiscreteSeekBar.SimpleOnProgressChangeListener() {

      private int seekValue;

      @Override public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        this.seekValue = value;
      }

      @Override public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        bookView.navigateToPercentageInChapter(this.seekValue);
        formatPageChapterProgress();
      }
    });

    final AppCompatActivity activity = (AppCompatActivity) getActivity();

    final DisplayMetrics metrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    final GestureDetector gestureDetector = new GestureDetector(context, new BookNavigationGestureDetector(bookView, metrics, this));

    displayPageNumber(-1); // Initializes the pagenumber view properly

    final View.OnTouchListener gestureListener = new View.OnTouchListener() {
      @SuppressLint("ClickableViewAccessibility") @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    };

    viewSwitcher.setOnTouchListener(gestureListener);
    bookView.setOnTouchListener(gestureListener);
    dummyView.setOnTouchListener(gestureListener);

    registerForContextMenu(bookView);
    saveConfigState();
    updateFromPrefs();
    updateFileName(savedInstanceState);

    bookView.restore();
  }

  @Override public void onResume() {
    super.onResume();
    checkIfHasBeenSharedQuote();
  }

  @Override public void onPause() {
    Log.d(TAG, "onPause() called.");
    saveReadingPosition();
    onReaderFragmentEvent(BookReaderEvents.READ_PAGE_ANALYTICS_EVENT);
    super.onPause();
  }

  @Override public void onStop() {
    super.onStop();
    Log.d(TAG, "onStop() called.");
    dismissProgressDialog();
  }

  @Override public void onDestroy() {
    this.bookView.releaseResources();
    this.dismissProgressDialog();
    super.onDestroy();
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
    this.textLoader.clearCachedText();
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    onFragmentActivityResult(requestCode, resultCode, data);
  }

  @Override public void onSaveInstanceState(final Bundle outState) {
    if (this.bookView != null) {
      outState.putInt(POS_KEY, this.bookView.getProgressPosition());
      outState.putInt(IDX_KEY, this.bookView.getIndex());
    }
  }

  @Override public void onVisibilityChange(final boolean visible) {
    final AbstractReaderActivity activity = ((AbstractReaderActivity) getActivity());
    if (activity == null) {
      return;
    }

    final AppBarLayout toolbarLayout = activity.getToolbarLayout();
    final AnimatorSet set = new AnimatorSet();
    final ObjectAnimator progressViewAnimator;
    final ObjectAnimator toolbarViewAnimator;

    if (visible) {
      progressViewAnimator = ViewPropertyObjectAnimator.animate(progressContainer)
          .alpha(1)
          .translationY(0)
          .setDuration(300)
          .setInterpolator(new FastOutSlowInInterpolator())
          .addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(android.animation.Animator animation) {
              progressContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
          })
          .get();
      toolbarViewAnimator =
          ViewPropertyObjectAnimator.animate(toolbarLayout).alpha(1).translationY(0).setDuration(300).setInterpolator(new FastOutSlowInInterpolator()).get();
    } else {
      progressViewAnimator = ViewPropertyObjectAnimator.animate(progressContainer)
          .alpha(0)
          .translationY(progressContainer.getBottom())
          .setDuration(300)
          .setInterpolator(new FastOutSlowInInterpolator())
          .addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
              progressContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
          })
          .get();
      toolbarViewAnimator = ViewPropertyObjectAnimator.animate(toolbarLayout)
          .alpha(0)
          .translationY(-toolbarLayout.getBottom())
          .setDuration(300)
          .setInterpolator(new FastOutSlowInInterpolator())
          .get();
    }

    set.playTogether(progressViewAnimator, toolbarViewAnimator);
    set.start();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.show_book_content) {
      bookTocEntryListener.displayBookTableOfContents();
      return true;
    } else if (itemId == R.id.display_options) {
      final ModifyReaderSettingsDialog d = new ModifyReaderSettingsDialog();
      d.setBrightnessManager(di.brightnessManager);
      d.setConfiguration(di.config);
      d.setOnModifyReaderSettingsListener(new ModifyReaderSettingsDialog.ModifyReaderSettingsListener() {
        @Override public void onReaderSettingsModified(ModifyReaderSettingsDialog.Action action) {
          switch (action) {
            case MODIFIED:
              updateFromPrefs();
              break;
          }
        }
      });
      final FragmentManager fm = getFragmentManager();
      d.show(fm, ModifyReaderSettingsDialog.TAG);
      return true;
    } else if (itemId == R.id.text_to_speech) {
      onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_TEXT_TO_SPEECH_ACTIVATED_EVENT);
      return true;
    } else if (itemId == android.R.id.home) {
      if (isPhotoViewerDisplayed()) {
        hidePhotoViewer();
        return false;
      }
      getActivity().finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onOptionsMenuClosed(Menu menu) {
    updateFromPrefs();
  }

  protected abstract void onFragmentActivityResult(final int requestCode, final int resultCode, final Intent data);

  private void checkIfHasBeenSharedQuote() {
    if (hasSharedText) {
      hasSharedText = false;
      handler.postDelayed(new Runnable() {
        @Override public void run() {
          onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_SHARED_QUOTE_EVENT);
        }
      }, 500);
    }
  }

  private void updateFromPrefs() {
    final AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity == null) {
      return;
    }

    bookView.setTextSize(di.config.getTextSize());

    int marginH = di.config.getHorizontalMargin();
    int marginV = di.config.getVerticalMargin();

    bookView.setFontFamily(di.config.getSerifFontFamily());

    textLoader.fromConfiguration(di.config);

    bookView.setHorizontalMargin(marginH);
    bookView.setVerticalMargin(marginV);

    if (!isAnimating()) {
      bookView.setEnableScrolling(di.config.isScrollingEnabled());
    }

    bookView.setLineSpacing(di.config.getLineSpacing());

    final ActionBar actionBar = activity.getSupportActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setTitle("");

    final SystemUiHelper systemUiHelper = getSystemUiHelper();
    if (systemUiHelper != null) {
      if (di.config.isKeepScreenOn()) {
        systemUiHelper.keepScreenOn();
      } else {
        systemUiHelper.keepScreenOff();
      }
    }

    restoreColorProfile();

    // Check if we need a restart

    final boolean isFontChanged = !di.config.getSerifFontFamily().getName().equalsIgnoreCase(savedConfigState.serifFontName);
    final boolean isBackgroundChanged = di.config.getColourProfile() != savedConfigState.colorProfile;
    final boolean isStripWhiteSpaceEnabled = di.config.isStripWhiteSpaceEnabled() != savedConfigState.stripWhiteSpace;
    final boolean fontNameChanged = di.config.getDefaultFontFamily().getName().equalsIgnoreCase(savedConfigState.fontName);
    final boolean isSansSerifFontNameEqual = di.config.getSansSerifFontFamily().getName().equalsIgnoreCase(savedConfigState.sansSerifFontName);
    if (!savedConfigState.usePageNum
        || isStripWhiteSpaceEnabled
        || !fontNameChanged
        || isFontChanged
        || !isSansSerifFontNameEqual
        || di.config.getHorizontalMargin() != savedConfigState.hMargin
        || di.config.getVerticalMargin() != savedConfigState.vMargin
        || di.config.getTextSize() != savedConfigState.textSize
        || di.config.isScrollingEnabled() != savedConfigState.scrolling
        || di.config.isUseColoursFromCSS() != savedConfigState.allowColoursFromCSS) {

      textLoader.invalidateCachedText();
      restartActivity(isFontChanged, isBackgroundChanged);
    }
  }

  private boolean isAnimating() {
    final Animator anim = dummyView.getAnimator();
    return anim != null && !anim.isFinished();
  }

  @Nullable private SystemUiHelper getSystemUiHelper() {
    if (getActivity() != null) {
      return ((AbstractReaderActivity) getActivity()).getSystemUiHelper();
    }
    return null;
  }

  private void restoreColorProfile() {
    this.bookView.setBackgroundColor(di.config.getBackgroundColor());
    this.viewSwitcher.setBackgroundColor(di.config.getBackgroundColor());

    this.bookView.setTextColor(di.config.getTextColor());
    this.bookView.setLinkColor(di.config.getLinkColor());

    int brightness = di.config.getBrightness();
    di.brightnessManager.setBrightness(getActivity().getWindow(), brightness);
  }

  private void restartActivity(boolean isChangedFont, boolean isBackgroundModified) {
    onStop();

    //Clear any cached text.
    textLoader.closeCurrentBook();

    final Intent intent = getActivity().getIntent();
    intent.putExtra(AbstractReaderActivity.BOOK_METADATA_KEY, bookMetadata);
    intent.putExtra(CHANGE_FONT_KEY, isChangedFont);
    intent.putExtra(CHANGE_BACKGROUND_KEY, isBackgroundModified);
    startActivity(intent);

    AppCompatActivity activity = (AppCompatActivity) getActivity();

    if (activity != null) {
      activity.finish();
    }
  }

  private void dismissProgressDialog() {
    if (progressDialog != null) {
      this.progressDialog.dismiss();
      this.progressDialog = null;
    }
  }

  public void saveReadingPosition() {
    if (this.bookView != null) {
      int index = this.bookView.getIndex();
      int position = this.bookView.getProgressPosition();

      if (index != -1 && position != -1 && !bookView.isAtEnd()) {
        di.config.setLastPosition(this.bookMetadata.getBookId(), position);
        di.config.setLastIndex(this.bookMetadata.getBookId(), index);
      } else if (bookView.isAtEnd()) {
        di.config.setLastPosition(this.bookMetadata.getBookId(), -1);
        di.config.setLastIndex(this.bookMetadata.getBookId(), -1);
      }
    }
  }

  protected abstract DICompanion onProvideDICompanionObject();

  private void onNotifyGamificationFontOrBackgroundReaderEvents(final boolean isFontChanged, final boolean isBackgroundChanged) {
    handler.postDelayed(new Runnable() {
      @Override public void run() {
        if (isFontChanged) {
          onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_FONT_SIZE_CHANGED_EVENT);
        } else if (isBackgroundChanged) {
          onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_BACKGROUND_CHANGED_EVENT);
        }
      }
    }, 500);
  }

  protected abstract void onReaderFragmentEvent(@BookReaderFragmentEvent int event);

  public void saveConfigState() {
    // Cache old settings to check if we'll need a restart later
    savedConfigState.stripWhiteSpace = di.config.isStripWhiteSpaceEnabled();

    savedConfigState.usePageNum = true;

    savedConfigState.hMargin = di.config.getHorizontalMargin();
    savedConfigState.vMargin = di.config.getVerticalMargin();

    savedConfigState.textSize = di.config.getTextSize();
    savedConfigState.fontName = di.config.getDefaultFontFamily().getName();
    savedConfigState.serifFontName = di.config.getSerifFontFamily().getName();
    savedConfigState.sansSerifFontName = di.config.getSansSerifFontFamily().getName();

    savedConfigState.scrolling = di.config.isScrollingEnabled();
    savedConfigState.allowColoursFromCSS = di.config.isUseColoursFromCSS();

    savedConfigState.colorProfile = di.config.getColourProfile();
  }

  private void updateFileName(Bundle savedInstanceState) {
    int lastPos = di.config.getLastPosition(bookMetadata.getBookId());
    int lastIndex = di.config.getLastIndex(bookMetadata.getBookId());

    if (savedInstanceState != null) {
      lastPos = savedInstanceState.getInt(POS_KEY, lastPos);
      lastIndex = savedInstanceState.getInt(IDX_KEY, lastIndex);
    }

    this.bookView.setFileName(bookMetadata.getBookId());
    this.bookView.setPosition(lastPos);
    this.bookView.setIndex(lastIndex);

    //config.setLastOpenedFile(fileName);
  }

  public void onWindowFocusChanged(boolean hasFocus) {
    //final SystemUiHelper systemUiHelper = getSystemUiHelper();
    //if (hasFocus) {
    //  updateFromPrefs();
    //  if (systemUiHelper != null && systemUiHelper.isShowing()) {
    //    systemUiHelper.delayHide(SystemUiHelper.SHORT_DELAY);
    //  }
    //} else {
    //  if (systemUiHelper != null) {
    //    systemUiHelper.keepScreenOff();
    //  }
    //}
  }

  public boolean onTouchEvent(MotionEvent event) {
    return bookView.onTouchEvent(event);
  }

  @Override public void onBookOpened(final Book book) {
    final AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity == null) {
      return;
    }

    this.bookTitle = book.getTitle();

    final Metadata metadata = book.getMetadata();

    if (metadata != null) {
      // Author
      final Author authorsOption = firstOption(metadata.getAuthors()).getOrElse(new Author(getString(R.string.ls_book_reading_unknown_author)));
      this.author = TextUtils.isEmpty(authorsOption.getLastname()) ? getString(R.string.ls_book_reading_unknown_author) : authorsOption.getLastname();
    } else {
      // Assuming defaults
      this.author = getString(R.string.ls_book_reading_unknown_author);
    }

    activity.invalidateOptionsMenu();

    if (bookTocEntryListener != null) {
      final Option<List<TocEntry>> optionableToc = bookView.getTableOfContents();
      tableOfContents = optionableToc.unsafeGet();
      bookTocEntryListener.onBookTableOfContentsLoaded(optionableToc);
    }

    updateFromPrefs();
  }

  @Override public void onStartRenderingText() {
    if (isAdded()) {
      final ProgressDialog progressDialog = getProgressDialog(R.string.ls_loading_text);
      progressDialog.show();
    }
  }

  @Override public void onErrorOnBookOpening() {
    dismissProgressDialog();
  }

  @Override public void onParseEntryStart(int entry) {
    if (!isAdded() || getActivity() == null) {
      return;
    }

    this.viewSwitcher.clearAnimation();
    this.viewSwitcher.setBackground(null);

    restoreColorProfile();
    displayPageNumber(-1);

    final ProgressDialog progressDialog = getProgressDialog(R.string.ls_loading_text);
    progressDialog.show();
  }

  @Override public void onParseEntryComplete(final Resource resource) {
    final Activity activity = getActivity();
    if (activity == null) {
      return;
    }

    dismissProgressDialog();

    // Set chapter
    String currentChapter = null;
    if (resource != null && resource.getHref() != null && tableOfContents != null) {
      for (TocEntry content : tableOfContents) {
        final String contentHref = content.getHref();
        if (resource.getHref().equals(contentHref)) {
          currentChapter = content.getTitle();
          break;
        }
      }
    }

    // Pages remaining for chapter
    formatPageChapterProgress();

    readingTitleProgressTv.setText(currentChapter != null ? currentChapter : bookTitle);

    // Tutorial feature
    if (di.userFlowTutorial != null) { // TODO: Remove this check
      di.userFlowTutorial.get(UserFlow.Type.READER, new CompletionCallback<List<TutorialModel>>() {
        private boolean isTutorial(List<TutorialModel> tutorials) {
          boolean isTutorial = false;

          for (TutorialModel tutorialModel : tutorials) {
            if (tutorialModel.getType() == TutorialModel.Type.TUTORIAL) {
              isTutorial = true;
              break;
            }
          }
          return isTutorial;
        }

        @Override public void onSuccess(final List<TutorialModel> tutorials) {
          if (tutorials.isEmpty()) {
            tutorialView.setVisibility(View.GONE);
          } else {
            boolean isTutorial = isTutorial(tutorials);

            if (isTutorial) {
              setTutorialViewVisibility(View.VISIBLE);
              tutorialView.setTutorialListener(new TutorialView.TutorialListener() {
                @Override public void onCompleted() {
                  tutorialView.setVisibility(View.GONE);
                }
              });
              tutorialView.setIsTrianglesDisabled(true);
              tutorialView.setTutorials(tutorials);
            } else {
              setTutorialViewVisibility(View.GONE);

              TutorialModel tutorialModel = tutorials.get(0);
              if (tutorialModel.getType() == TutorialModel.Type.SET_YOUR_GOALS) {
                if (getActivity() != null) {
                  MaterialDialog setYourGoalsDialog = DialogFactory.createSetYourGoalsDialog(getActivity(), new DialogFactory.ActionCallback() {
                    @Override public void onResponse(MaterialDialog dialog, DialogFactory.Action action) {
                      if (action == DialogFactory.Action.OK) {
                        onReaderFragmentEvent(BookReaderEvents.NAVIGATION_TO_GOALS_SCREEN_EVENT);
                      }
                    }
                  });

                  if (getActivity().hasWindowFocus()) {
                    setYourGoalsDialog.show();
                  }
                }
              } else if (tutorialModel.getType() == TutorialModel.Type.BECOME_WORLDREADER) {
                displayUserNotRegisteredDialog();
              }
            }
          }
        }

        @Override public void onError(ErrorCore errorCore) {
          tutorialView.setVisibility(View.GONE);
        }
      });
    }
  }

  @Override public void onProgressUpdate(int progressPercentage) {
    chapterProgressDsb.setProgress(progressPercentage);
  }

  @Override public boolean onSwipeLeft() {
    final SystemUiHelper systemUiHelper = getSystemUiHelper();
    if (systemUiHelper != null && systemUiHelper.isShowing()) {
      systemUiHelper.hide();
    }

    if (di.config.getReadingDirection() == ReadingDirection.LEFT_TO_RIGHT) {
      pageDown(Orientation.HORIZONTAL);
    } else {
      pageUp(Orientation.HORIZONTAL);
    }

    if (bookView.isAtEnd()) {
      notifyFinishedBookEventInteractor();
    }

    return true;
  }

  @Override public boolean onSwipeRight() {
    final SystemUiHelper systemUiHelper = getSystemUiHelper();
    if (systemUiHelper != null && systemUiHelper.isShowing()) {
      systemUiHelper.hide();
    }

    if (di.config.getReadingDirection() == ReadingDirection.LEFT_TO_RIGHT) {
      pageUp(Orientation.HORIZONTAL);
    } else {
      pageDown(Orientation.HORIZONTAL);
    }

    return true;
  }

  @Override public boolean onTapLeftEdge() {
    final SystemUiHelper systemUiHelper = getSystemUiHelper();
    if (systemUiHelper != null && systemUiHelper.isShowing()) {
      systemUiHelper.hide();
    }

    if (di.config.getReadingDirection() == ReadingDirection.LEFT_TO_RIGHT) {
      pageUp(Orientation.HORIZONTAL);
    } else {
      pageDown(Orientation.HORIZONTAL);
    }

    return true;
  }

  @Override public boolean onTapRightEdge() {
    final SystemUiHelper systemUiHelper = getSystemUiHelper();
    if (systemUiHelper != null && systemUiHelper.isShowing()) {
      systemUiHelper.hide();
    }

    if (di.config.getReadingDirection() == ReadingDirection.LEFT_TO_RIGHT) {
      pageDown(Orientation.HORIZONTAL);
    } else {
      pageUp(Orientation.HORIZONTAL);
    }

    if (bookView.isAtEnd()) {
      notifyFinishedBookEventInteractor();
    }

    return true;
  }

  @Override public boolean onPreSlide() {
    if (isDefinitionViewDisplayed()) {
      hideDefinitionView();
    }
    return false;
  }

  @Override public boolean onLeftEdgeSlide(int value) {
    return false;
  }

  @Override public boolean onRightEdgeSlide(int value) {
    return false;
  }

  @Override public boolean onPreScreenTap() {
    if (isDefinitionViewDisplayed()) {
      hideDefinitionView();
      return true;
    }

    return isPhotoViewerDisplayed();
  }

  @Override public void onScreenTap() {
    final AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity == null) {
      return;
    }
    stopAnimating();
    getSystemUiHelper().toggle();
  }

  @Override public void onPageDown() {
    currentScrolledPages += 1;
    onReaderFragmentEvent(BookReaderEvents.GAMIFICATION_PAGE_DOWN_EVENT);
  }

  @Override public void onPageDownFirstPage() {
  }

  @Override public void onLastScreenPageDown() {
    currentScrolledPages += 1;
    // Update book metadata with author and title
    bookMetadata.setTitle(bookTitle);
    bookMetadata.setAuthor(author);
    onReaderFragmentEvent(BookReaderEvents.NAVIGATION_TO_BOOK_FINISHED_SCREEN_EVENT);
  }

  @Override public void onBookImageClicked(final Drawable drawable) {
    displayPhotoViewer((FastBitmapDrawable) drawable);
  }

  private void displayPageNumber(int pageNumber) {
    final String pageString = !di.config.isScrollingEnabled() && pageNumber > 0 ? Integer.toString(pageNumber) + "\n" : "\n";

    final SpannableStringBuilder builder = new SpannableStringBuilder(pageString);
    builder.setSpan(new CenterSpan(), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    pageNumberView.setTextColor(di.config.getTextColor());
    pageNumberView.setTextSize(di.config.getTextSize());
    pageNumberView.setTypeface(di.config.getDefaultFontFamily().getDefaultTypeface());
    pageNumberView.setText(builder);
    pageNumberView.invalidate();
  }

  private void displayPhotoViewer(final FastBitmapDrawable drawable) {
    final FragmentActivity activity = getActivity();
    if (activity != null) {
      final View imageViewContainer = activity.findViewById(R.id.photo_viewer);
      final View closeButton = activity.findViewById(R.id.photo_viewer_close_btn);
      closeButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {
          hidePhotoViewer();
        }
      });

      final Bitmap bitmap = drawable.getBitmap();

      if (bitmap != null) {
        final ImageSource imageSource = ImageSource.cachedBitmap(bitmap);
        final SubsamplingScaleImageView imageScaleView = (SubsamplingScaleImageView) activity.findViewById(R.id.photo_viewer_iv);
        imageScaleView.setImage(imageSource);
        imageScaleView.setMaxScale(3);
        imageViewContainer.setVisibility(View.VISIBLE);
      }
    }
  }

  private void hidePhotoViewer() {
    final FragmentActivity activity = getActivity();
    if (activity != null) {
      final View imageViewContainer = activity.findViewById(R.id.photo_viewer);
      imageViewContainer.setVisibility(View.GONE);

      final View closeButton = activity.findViewById(R.id.photo_viewer_close_btn);
      closeButton.setOnClickListener(null);

      final SubsamplingScaleImageView imageScaleView = (SubsamplingScaleImageView) activity.findViewById(R.id.photo_viewer_iv);
      imageScaleView.recycle();
    }
  }

  private boolean isPhotoViewerDisplayed() {
    final FragmentActivity activity = getActivity();
    if (activity != null) {
      final View imageViewContainer = activity.findViewById(R.id.photo_viewer);
      return imageViewContainer.getVisibility() == View.VISIBLE;
    } else {
      return false;
    }
  }

  private void stopAnimating() {
    if (dummyView.getAnimator() != null) {
      dummyView.getAnimator().stop();
      this.dummyView.setAnimator(null);
    }

    if (viewSwitcher.getCurrentView() == this.dummyView) {
      viewSwitcher.showNext();
    }

    this.pageNumberView.setVisibility(View.VISIBLE);
    bookView.setKeepScreenOn(false);
  }

  ///////////////////////////////////////////////////////////////////////////
  // BookViewListener Callbacks
  ///////////////////////////////////////////////////////////////////////////

  private boolean isDefinitionViewDisplayed() {
    return definitionView.getVisibility() == View.VISIBLE;
  }

  private void hideDefinitionView() {
    definitionView.setVisibility(View.GONE);
  }

  private ProgressDialog getProgressDialog(@StringRes int message) {
    if (this.progressDialog == null) {
      this.progressDialog = new ProgressDialog(context);
      this.progressDialog.setOwnerActivity(getActivity());
      this.progressDialog.setCancelable(false);
      this.progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          return true;
        }
      });
      this.progressDialog.setMessage(getString(message));
    }

    return this.progressDialog;
  }

  private void setShareFlag() {
    hasSharedText = true;
  }

  private void showDefinitionView() {
    definitionView.setVisibility(View.VISIBLE);
  }

  public boolean dispatchKeyEvent(KeyEvent event) {
    int action = event.getAction();
    int keyCode = event.getKeyCode();

    Log.d(TAG, "Got key event: " + keyCode + " with action " + action);

    if (isAnimating() && action == KeyEvent.ACTION_DOWN) {
      stopAnimating();
      return true;
    }

    Log.d(TAG, "Key event is NOT a media key event.");

    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        if (action == KeyEvent.ACTION_DOWN) {
          pageDown(Orientation.HORIZONTAL);
        }
        return true;

      case KeyEvent.KEYCODE_DPAD_LEFT:
        if (action == KeyEvent.ACTION_DOWN) {
          pageUp(Orientation.HORIZONTAL);
        }
        return true;

      case KeyEvent.KEYCODE_BACK:
        if (action == KeyEvent.ACTION_DOWN) {
          if (isDefinitionViewDisplayed()) {
            hideDefinitionView();
            return true;
          }

          if (isPhotoViewerDisplayed()) {
            hidePhotoViewer();
            return true;
          }

          getActivity().finish();
          return true;
        } else {
          return false;
        }
    }

    Log.d(TAG, "Not handling key event: returning false.");
    return false;
  }

  private void pageDown(Orientation o) {
    if (bookView.isAtEnd()) {
      bookView.lastPageDown();
      return;
    }

    stopAnimating();
    bookView.pageDown();
    formatPageChapterProgress();
  }

  private void pageUp(Orientation o) {
    if (bookView.isAtStart()) {
      return;
    }

    stopAnimating();
    bookView.pageUp();
    formatPageChapterProgress();
  }

  public void onNavigateToTocEntry(TocEntry tocEntry) {
    this.bookView.navigateTo(tocEntry);
  }

  private void setTutorialViewVisibility(int visibility) {
    tutorialView.setVisibility(visibility);
    containerTutorialView.setVisibility(visibility);
  }

  private void displayUserNotRegisteredDialog() {
    MaterialDialog dialog = DialogFactory.createDialog(getActivity(), R.string.ls_not_registered_dialog_title, R.string.ls_not_registered_dialog_message,
        R.string.ls_generic_accept, R.string.ls_generic_cancel, new DialogFactory.ActionCallback() {
          @Override public void onResponse(MaterialDialog dialog, DialogFactory.Action action) {
            if (action == DialogFactory.Action.OK) {
              onReaderFragmentEvent(BookReaderEvents.NAVIGATION_TO_SIGNUP_SCREEN_EVENT);
            }
          }
        });

    dialog.show();
  }

  private void notifyFinishedBookEventInteractor() {
    final ListenableFuture<Boolean> finishBookFuture = di.finishBookInteractor.execute(bookMetadata.getBookId());
    Futures.addCallback(finishBookFuture, new FutureCallback<Boolean>() {
      @Override public void onSuccess(final Boolean result) {
        // Ignored result
      }

      @Override public void onFailure(@NonNull final Throwable t) {
        // Ignored result
      }
    }, MoreExecutors.directExecutor());
  }

  private void formatPageChapterProgress() {
    chapterProgressPagesTv.setText(
        bookView.getPagesForResource() < bookView.getCurrentPage() ? "" : String.format("%s / %s", bookView.getCurrentPage(), bookView.getPagesForResource()));

    final Option<Spanned> text = bookView.getStrategy().getText();
    final Spanned spanned = text.getOrElse(new SpannableString(""));
    if (bookView.getPagesForResource() > 0) {
      final Map<String, String> amaAttributes = new HashMap<String, String>() {{
        //Book toc size
        put(AnalyticsEventConstants.BOOK_AMOUNT_OF_TOC_ENTRIES, String.valueOf(bookView.getTableOfContents().getOrElse(new ArrayList<TocEntry>()).size()));

        // Book spine size
        put(AnalyticsEventConstants.BOOK_SPINE_SIZE, String.valueOf(bookView.getSpineSize()));

        //Currently reading toc entry number
        put(AnalyticsEventConstants.BOOK_READING_SPINE_ELEM_IN_SPINE_POSITION, String.valueOf(bookView.getIndex()));
        put(AnalyticsEventConstants.BOOK_READING_SPINE_ELEM_SIZE_IN_CHARS, String.valueOf(spanned.length()));
        put(AnalyticsEventConstants.BOOK_READING_CURRENT_PAGE_IN_SPINE_ELEM, String.valueOf(bookView.getCurrentPage()));
        put(AnalyticsEventConstants.BOOK_READING_AMOUNT_OF_PAGES_IN_SPINE_ELEM, String.valueOf(bookView.getPagesForResource()));
        put(AnalyticsEventConstants.BOOK_READING_SCREEN_TEXT_SIZE_IN_CHARS, String.valueOf(bookView.getStrategy().getSizeChartDisplayed()));
        put(AnalyticsEventConstants.BOOK_ID_ATTRIBUTE, bookMetadata.getBookId());
        put(AnalyticsEventConstants.BOOK_TITLE_ATTRIBUTE, bookMetadata.getTitle());
      }};

      di.analytics.sendEvent(new BasicAnalyticsEvent(AnalyticsEventConstants.BOOK_READ_EVENT, amaAttributes));
    }
  }

  public interface OnBookTocEntryListener {

    void onBookTableOfContentsLoaded(Option<List<TocEntry>> book);

    void displayBookTableOfContents();
  }

  private static class SavedConfigState {

    private boolean stripWhiteSpace;
    private String fontName;
    private String serifFontName;
    private String sansSerifFontName;
    private boolean usePageNum;
    private int vMargin;
    private int hMargin;
    private int textSize;
    private boolean scrolling;
    private boolean allowColoursFromCSS;
    private ColorProfile colorProfile;
  }

  public static class DICompanion {

    @Inject public Configuration config;
    @Inject public StreamingBookDataSource streamingBookDataSource;
    @Inject public GetWordDefinitionInteractor getWordDefinitionInteractor;
    @Inject public FinishBookInteractor finishBookInteractor;
    @Inject public SendReadPagesInteractor sendReadPagesInteractor;
    @Inject public UserFlowTutorial userFlowTutorial;
    @Inject public BrightnessManager brightnessManager;
    @Inject public Dates dateUtils;
    @Inject public Reachability reachability;
    @Inject public Analytics analytics;
    @Inject public Logger logger;
  }

  public static class BookReaderEvents {

    public static final int GAMIFICATION_INITIALIZE_EVENT = 0;
    public static final int GAMIFICATION_START_BOOK_FROM_COLLECTION_EVENT = 1;
    public static final int GAMIFICATION_SHARED_QUOTE_EVENT = 2;
    public static final int GAMIFICATION_READ_ON_X_CONTINOUS_DAYS_EVENT = 3;
    public static final int GAMIFICATION_FONT_SIZE_CHANGED_EVENT = 4;
    public static final int GAMIFICATION_BACKGROUND_CHANGED_EVENT = 5;
    public static final int GAMIFICATION_TEXT_TO_SPEECH_ACTIVATED_EVENT = 6;
    public static final int GAMIFICATION_PAGE_DOWN_EVENT = 7;
    public static final int NAVIGATION_TO_BOOK_FINISHED_SCREEN_EVENT = 8;
    public static final int NAVIGATION_TO_GOALS_SCREEN_EVENT = 9;
    public static final int NAVIGATION_TO_SIGNUP_SCREEN_EVENT = 10;
    public static final int READ_PAGE_ANALYTICS_EVENT = 11;
    public static final int SAVE_CURRENTLY_BOOK_READING_EVENT = 12;
  }

  @IntDef({
      BookReaderEvents.GAMIFICATION_INITIALIZE_EVENT, BookReaderEvents.GAMIFICATION_START_BOOK_FROM_COLLECTION_EVENT,
      BookReaderEvents.GAMIFICATION_SHARED_QUOTE_EVENT, BookReaderEvents.GAMIFICATION_READ_ON_X_CONTINOUS_DAYS_EVENT,
      BookReaderEvents.GAMIFICATION_FONT_SIZE_CHANGED_EVENT, BookReaderEvents.GAMIFICATION_BACKGROUND_CHANGED_EVENT,
      BookReaderEvents.GAMIFICATION_TEXT_TO_SPEECH_ACTIVATED_EVENT, BookReaderEvents.GAMIFICATION_PAGE_DOWN_EVENT,
      BookReaderEvents.NAVIGATION_TO_BOOK_FINISHED_SCREEN_EVENT, BookReaderEvents.NAVIGATION_TO_GOALS_SCREEN_EVENT,
      BookReaderEvents.NAVIGATION_TO_SIGNUP_SCREEN_EVENT, BookReaderEvents.READ_PAGE_ANALYTICS_EVENT, BookReaderEvents.SAVE_CURRENTLY_BOOK_READING_EVENT
  }) @Retention(RetentionPolicy.SOURCE) @interface BookReaderFragmentEvent {

  }
}

