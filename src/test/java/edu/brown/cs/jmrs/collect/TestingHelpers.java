package edu.brown.cs.jmrs.collect

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Static class to contain testing helpers common to recommender and autocorrect
 * functionality.
 *
 * @author mcisler
 *
 */
public final class TestingHelpers {
  /**
   * Override default constructor.
   */
  private TestingHelpers() {
    return;
  }

  /**
   * Given a series of single words, returns the list of lists where each word
   * is converted to an inner list. For use as input to Suggester.suggest()
   *
   * @param words
   *          The list of words to convert into lists of lists of each word.
   * @return The list of single-word lists.
   */
  public static List<List<String>> singleWordList(String... words) {
    List<List<String>> wordLists = new ArrayList<>(words.length);
    for (String word : words) {
      wordLists.add(ImmutableList.of(word));
    }
    return wordLists;
  }

  /**
   * @param lst1
   *          The Collection to compare with lst2
   * @param lst2
   *          The Collection to compare with lst1
   * @return Whether the two provided collections contain the same elements.
   *         Ignores order.
   * @param <T>
   *          The type in the collections.
   */
  public static <T> boolean containsSameElements(Collection<T> lst1,
      Collection<T> lst2) {
    List<T> missingElms1 = new ArrayList<>();
    List<T> missingElms2 = new ArrayList<>();

    for (T elm1 : lst1) {
      if (!lst2.contains(elm1)) {
        missingElms1.add(elm1);
      }
    }
    for (T elm2 : lst2) {
      if (!lst1.contains(elm2)) {
        missingElms2.add(elm2);
      }
    }

    if (missingElms1.size() > 0) {
      System.out.println(
          "Second list did not contain these elements from the first list: "
              + missingElms1);
      System.out.println(lst1.size() - missingElms1.size()
          + " elements from the first WERE in the second.");
    }
    if (missingElms2.size() > 0) {
      System.out.println(
          "First list did not contain these elements from the second list: "
              + missingElms2);
      System.out.println(lst2.size() - missingElms2.size()
          + " elements from the second WERE in the first.");
    }
    return missingElms1.size() == 0 && missingElms2.size() == 0;
  }
}
