package edu.brown.cs.jmrs.language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * An aggregation of characteristics of a set of word.
 *
 * Implements Collection and thus can be iterated over.
 *
 * @author mcisler
 *
 */
public final class Corpus extends AbstractCollection<String> {
  private final boolean toLower;
  private final Set<String> words;

  /**
   * Initializes internal fields, i.e. the ngram fields, to empty and default
   * values.
   */
  public Corpus() {
    this(true);
  }

  /**
   * Initializes internal fields, i.e. the ngram fields, to empty and default
   * values, with a decision for setting to lowercase.
   *
   * @param toLower
   *          whether to lowercase all input.
   */
  public Corpus(boolean toLower) {
    super();
    words = new HashSet<>();
    this.toLower = toLower;
  }

  /**
   * Assembles Corpus statistics from the provided words.
   *
   * @param words
   *          The words to use as input.
   */
  public Corpus(Iterable<String> words) {
    this();
    for (String word : words) {
      add(word);
    }
  }

  /**
   * Assembles Corpus statistics from the provided file.
   *
   * @param file
   *          The file to read into text before using as corpus input.
   * @throws FileNotFoundException
   *           If the file is not found.
   */
  public Corpus(File file) throws FileNotFoundException {
    this();
    add(file);
  }

  /**
   * Adds words from provided text to this corpus.
   *
   * @param text
   *          The text to retrieve words from.
   * @return Whether this corpus was modified in adding text.
   */
  @Override
  public boolean add(String text) {
    List<String> wordList = new ArrayList<>();
    readLine(text, wordList);
    return readWords(wordList);
  }

  /**
   * Adds words from the text of the provided file to this corpus.
   *
   * @param file
   *          The file to retrieve words from.
   * @throws FileNotFoundException
   *           If the file is not found.
   * @return Whether this corpus was modified in adding the text from file.
   */
  public boolean add(File file) throws FileNotFoundException {
    return readWords(readFile(file));
  }

  /**
   * @param word
   *          The word to check for in the corpus.
   * @return Whether that word is in this corpus. Uses a hashtable lookup for
   *         constant time.
   */
  public boolean contains(CharSequence word) {
    return words.contains(word);
  }

  /**
   * Reads and parses the raw text from the given file into a list of words.
   *
   * @param file
   *          The file to read from.
   * @return The list of words extracted from this file.
   * @param toLower
   *          Whether to lowercase the inputted words.
   * @throws FileNotFoundException
   *           If file is not found.
   */
  private List<String> readFile(File file) throws FileNotFoundException {
    List<String> wrds = new ArrayList<>();
    try (Scanner scanner = new Scanner(file, "UTF-8")) {
      while (scanner.hasNextLine()) {
        readLine(scanner.nextLine(), wrds);
      }
    }
    return wrds;
  }

  /**
   * Reads and parses the raw text from the given line of text into the provided
   * list of words.
   *
   * @param text
   *          The line to read from.
   * @param words
   *          The list of words to add those from this line to.
   * @param toLower
   *          Whether to lowercase the inputted words.
   */
  private void readLine(String text, List<String> wrds) {
    wrds.add(toLower ? text.toLowerCase(Locale.getDefault()) : text);
  }

  /**
   * Imports a list of words into this corpus.
   *
   * @param words
   *          A list of the words to import.
   * @return Whether this corpus was modified in adding words.
   */
  private boolean readWords(List<String> wrds) {
    return words.addAll(wrds);
  }

  @Override
  public String toString() {
    return words.toString();
  }

  /**
   * Methods required to implement Collection.
   */

  @Override
  public Iterator<String> iterator() {
    return words.iterator();
  }

  @Override
  public int size() {
    return words.size();
  }
}
