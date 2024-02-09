import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;

/**
 * Main method with a bunch of static methods which serve as the primary engine
 * for the game itself as well as the bots that play it.
 * 
 * @author draga
 *
 */
public class Main {

  private static Wordles[] answer = new Wordles[5];
  private static ArrayList<Wordles> list = new ArrayList<Wordles>();
  private static ArrayList<Wordles> totalList = new ArrayList<Wordles>();
  public static int totalSize;
  private static String[][] colorTable;

  public static void main(String[] args) {
    uploadList("Ans.txt");
    fillTable();
    //test();
    inputNYT();
    //game("human", true, "", false);
    //getTestResults(10, 5, "EVBot", true);
    // System.out.println(getWinPercent(200, 0.05, "EVBot", true));
  }

  public static void test() {
    /* addAns("paddy");
    list.clear();
    list.add(new Wordles("daddy", 1));
    list.add(new Wordles("paddy", 1));
    list.add(new Wordles("zonal", 1));
    Wordles[] W = new Wordles[5];
    W[0] = new Wordles("d", -1);
    W[1] = new Wordles("a", 1);
    W[2] = new Wordles("d", 2);
    W[3] = new Wordles("d", 3);
    W[4] = new Wordles("y", 4);
    System.out.println(list);
    eliminate(W);
    System.out.println(list);
    //updateList(); */
  }

  /**
   * Takes a txt file with each word on its own line and converts it into an
   * ArrayList that can be edited by program.
   * totalList is the list of all possible answers.
   * list is the list of possible answers in that game.
   * 
   * @param file file you are uploading
   */
  public static void uploadList(String file) {
    String temp = "";
    try {
      Scanner input = new Scanner(new File(file));
      while (input.hasNext()) {
        temp = input.nextLine().trim();
        totalList.add(new Wordles(temp, 0));
      }
      input.close();
      totalSize = totalList.size();
      for (Wordles w : totalList)
        list.add(w);
    } catch (Exception e) {
      System.out.println("Unable to locate " + file);
    }
  }

  /**
   * resets list to include all possible answers after playing a game
   */
  public static void updateList() {
    list.clear();
    for (Wordles w : totalList)
      list.add(w);
    if (colorTable != null) {
      for (int r = 0; r < colorTable.length; r++) {
        for (int c = 0; c < colorTable[r].length; c++) {
          if (colorTable[r][c].length() == 6) {
            colorTable[r][c] = colorTable[r][c].substring(1);
          }
        }
      }
    }
  }

  /**
   * Based on the result from a guess, uses process of elimination to remove all
   * impossible words from list
   * 
   * @param test separated characters with -1 = black; 0-4= green index; 5-9 =
   *             yellow index
   */
  public static void eliminate(Wordles[] test) {
    for (int i = 0; i < 5; i++) {
      double f = test[i].getFreq();
      String c = test[i].getWord();
      for (int j = 0; j < list.size(); j++) {
        if (f < 0) {
          if (list.get(j).getWord().substring(i, i+1).equals(c)) {
            elimShortcut(j);
            j--;
          }
        } else if (f < 5) {
          if (!list.get(j).getWord().substring((int) f, (int) f + 1).equals(c)) {
            elimShortcut(j);
            j--;
          }
        } else if (f < 10) {
          //System.out.println(c);
          if (!list.get(j).getWord().contains(c)
              || list.get(j).getWord().substring((int) f - 5, (int) f - 4).equals(c)) {
            elimShortcut(j);
            j--;
          }
        }
      }
    }
  }

  /**
   * Deletes word in list with a given index and if using combinations look up
   * table, finds and marks all irrelevant combinations either because guess or
   * answer is no longer valid
   * 
   * @param n index of word being deleted in list
   */
  public static void elimShortcut(int n) {
    int removeIndex = n;
    Wordles removed = list.remove(removeIndex);
    if (colorTable != null) {
      for (int i = 0; i < totalList.size(); i++)
        if (totalList.get(i).getWord().equals(removed.getWord()))
          removeIndex = i;
      colorTable[removeIndex][0] = "/" + colorTable[removeIndex][0];
      for (int i = 0; i < colorTable.length; i++) {
        colorTable[i][removeIndex + 1] = "/" + colorTable[i][removeIndex + 1];
      }
    }
  }

  /**
   * checks if a letter appears more than once in an array of characters of a word
   * 
   * @param letter the letter you are checking for
   * @param check  the word you are checking with characters in seperate wordle
   *               objects
   * @return true if a letter appears only once
   */
  public static boolean doubleCheck(String letter, Wordles[] check) {
    int count = 0;
    for (int i = 0; i < 5; i++) {
      if (check[i].getWord().equals(letter))
        count++;
    }
    if (count == 1)
      return true;
    else
      return false;
  }

  /**
   * checks if a letter appears more than once in a word
   * 
   * @param letter letter you are checking
   * @param test   word you are checing in a single wordle object
   * @return true if a letter appears once or not at all in a word
   */
  public static boolean repeatCheck(String letter, Wordles test) {
    int count = 0;
    for (int i = 0; i < 5; i++) {
      if (test.getWord().substring(i, i + 1).equals(letter))
        count++;
    }
    if (count < 2)
      return false;
    else
      return true;
  }

  /**
   * Takes the first word list array in inputs it into the game
   * 
   * @return returns the guess in form of a length 5 string
   */
  public static String pickTopBot() {
    try {
      String temp = list.get(0).getWord();
      eliminate(checkGuess(temp));
      return temp;
    } catch (Exception e) {
      return "xxxxx";
    }
  }

  /**
   * plays Wordle with unknown answer.
   * input the result from computer in five letter String with g = green, y =
   * yellow, b = black
   */
  public static void inputNYT() {
    fillTable();
    int guessCount = 1;
    String key = "";
    Wordles[] result = new Wordles[5];
    Scanner keyboard = new Scanner(System.in);
    String word;
    while (guessCount < 7) {
      if (guessCount != 1) {
        calculateEV(true);
        word = highestEV(true);
      } else if (list.size() == 1) {
        word = list.get(1775).getWord();
      } else {
        word = "slate";
      }
      System.out.println(word + " (r to reset)");
      key = keyboard.next();
      if (key.equals("ggggg")) {
        System.out.println("YOU WIN");
        break;
      } else if (key.equals("r")) {
        guessCount = 1;
        updateList();
        System.out.println(list.get(1775).getWord() + " (r to reset)");
        word = "slate";
        key = keyboard.next();
      }
      for (int i = 0; i < 5; i++) {
        int index = -1;
        System.out.println(key);
        if (key.substring(i, i + 1).equals("g"))
          index = i;
        else if (key.substring(i, i + 1).equals("y"))
          index = i + 5;
        result[i] = new Wordles(word.substring(i, i + 1), index);
        System.out.println(result[i]);
      }
      eliminate(result);
      guessCount++;
    }
    if (guessCount == 6)
      System.out.println("YOU LOSE");
    keyboard.close();
  }

  /**
   * Returns result from guess based on known answer
   * 
   * @param guess length 5 String that is compared to the answer
   * @return array with separated characters paired with number that represents
   *         color and index(-1 = black, 0-4 = green, 5-9 = yellow)
   */
  public static Wordles[] checkGuess(String guess) {
    String result = "";
    Wordles[] arr = new Wordles[5];
    int guessIndex = stringSearch(guess);
    String ans = "";
    for (Wordles w : answer)
      ans += w.getWord();
    int answerIndex = stringSearch(ans);
    if (colorTable[guessIndex][answerIndex + 1].length() == 5)
      result = colorTable[guessIndex][answerIndex + 1];
    else
      result = colorTable[guessIndex][answerIndex + 1].substring(1);
    for (int i = 0; i < 5; i++) {
      String co = result.substring(i, i + 1);
      if (co.equals("g"))
        arr[i] = new Wordles(guess.substring(i, i + 1), i);
      else if (co.equals("y"))
        arr[i] = new Wordles(guess.substring(i, i + 1), i + 5);
      else
        arr[i] = new Wordles(guess.substring(i, i + 1), -1);
    }
    return arr;
  }

  /**
   * Sees if a guess is the answer
   * 
   * @param guess    the word being check (length = 5)
   * @param printRes whether or not the result should be printed to console
   * @return returns true if the guess is the answer false otherwise
   */
  public static boolean checkWin(String guess, boolean printRes) {
    Wordles[] check = checkGuess(guess);
    for (int i = 0; i < 5; i++) {
      if (!check[i].getWord().equals(answer[i].getWord())) {
        if (printRes)
          printScore(check);
        return false;
      }
    }
    if (printRes) {
      printScore(check);
      System.out.println("YOU WIN");
    }
    return true;
  }

  public static String getAnswer() {
    String res = "";
    for (Wordles w : answer)
      res += w.getWord();
    return res;
  }

  /**
   * prints result based on array made by checkGuess()
   * 
   * @param guess characters of a word paired with Wordle value(-1 = black ...
   *              etc)
   */
  public static void printScore(Wordles[] guess) {
    for (int i = 0; i < 5; i++) {
      String result = "";
      result += guess[i].getWord() + ": ";
      if (guess[i].getFreq() < 0)
        result += "NO";
      else if (guess[i].getFreq() < 5)
        result += "YES";
      else if (guess[i].getFreq() < 10)
        result += "ALMOST";
      else
        result += "ERROR";
      System.out.println(result);
    }
    System.out.println("---------");
  }

  /**
   * set the answer to a five letter String
   * 
   * @param ans the new answer
   */
  public static void addAns(String ans) {
    for (int i = 0; i < 5; i++) {
      answer[i] = new Wordles(ans.substring(i, i + 1), i);
    }
  }

  public static boolean isInList(String ans) {
    for (Wordles w : list) {
      if (ans.equals(w.getWord()))
        return true;
    }
    return false;
  }

  /**
   * plays a game of Wordle
   * 
   * @param player   who is playing the game (human = human player, pickTopBot =
   *                 pickTopBot() method, EVBot = expected value method)
   * @param printRes whether or not to print the result from each individual guess
   * @param ans      if ans.length() = 5 then the answer is set to parameter,
   *                 otherwise a random choice is chosen from the list of possible
   *                 words.
   * @return the amount of guess it took to find the answer with a loss being 7
   *         guesses
   */
  public static int game(String player, boolean printRes, String ans, boolean hardMode) {
    Scanner keyboard = new Scanner(System.in);
    int ansNum;
    if (ans.length() == 5) {
      addAns(ans);
      ansNum = 0;
    } else {
      ansNum = (int) (Math.random() * (totalList.size() - 1));
      addAns(totalList.get(ansNum).getWord());
    }
    if (player.equals("human"))
      System.out.println("WORDLE: GUESS THE WORD IN 6");
    boolean gameOn = true;
    int countGuess = 0;
    String currentGuess = "";
    while (gameOn) {
      if (player.equals("human"))
        currentGuess = keyboard.next();
      else if (player.equals("pickTopBot"))
        currentGuess = pickTopBot();
      else if (player.equals("EVBot"))
        currentGuess = EVBot(hardMode);
      if (checkWin(currentGuess, printRes))
        gameOn = false;
      else
        countGuess++;
      if (countGuess == 6) {
        if (printRes)
          System.out.println("YOU LOSE");
        /*
         * for (Wordles w : answer) {
         * System.out.print(w.getWord());
         * }
         * System.out.println("");
         */
        gameOn = false;
      }
    }
    keyboard.close();
    updateList();
    return countGuess + 1;
  }

  /**
   * calculates average score over multiple games for a bot
   * 
   * @param n      number of games being played
   * @param player which bot is being test (same of game() method)
   * @param inc    increment of the progress message expressed in decimal form
   *               (.05 for update every 5%)
   * @return the average score as a double
   */
  public static void getTestResults(int n, int inc, String player, boolean hardMode) {
    double totalScore = 0;
    int guessIndex;
    double winCount = 0;
    int cutoff = inc;
    for (int i = 0; i < n; i++) {
      if (n != totalList.size())
        guessIndex = (int) (Math.random() * totalSize);
      else
        guessIndex = i;
      int g = game(player, false, totalList.get(guessIndex).getWord(), hardMode);
      totalScore += g;
      if (g < 7) winCount++;
      if ((i * 100 / n) >= cutoff) {
        System.out.println(cutoff + "% DONE");
        cutoff += inc;
      }

    }
    System.out.println("===========FINAL==========");
    System.out.println("AVG SCORE: " + (totalScore / n));
    System.out.println("WIN PERCENT: " + (winCount / n)*100);
  }

  /**
   * calculates the percentage of games won by a bot
   * 
   * @param n      number of games to play
   * @param inc    increment of the progress message expressed in decimal form
   *               (.05 for update every 5%)
   * @param player which bot is being tested (same as game() method)
   * @return how games won as a percentage
   */
  public static double getWinPercent(int n, double inc, String player, boolean hardMode) {
    double winCount = 0;
    int guessIndex;
    double cutoff = inc;
    for (int i = 0; i < n; i++) {
      if (n != totalList.size())
        guessIndex = (int) (Math.random() * totalSize);
      else
        guessIndex = i;
      if (game(player, false, totalList.get(guessIndex).getWord(), hardMode) < 7)
        winCount++;
      if ((i / (double) n) >= cutoff) {
        System.out.println((int) (cutoff * 100) + "% DONE");
        cutoff += inc;
      }
    }
    return (winCount / n) * 100;
  }

  /**
   * uses brute force in order to find out which words are best to start with
   * 
   * @param n the index that is being optimized
   * @param s how many answers are being checked for words (decrease to decrease
   *          run time)
   */
  /* public static void bestOrder(int n, int s) {
    double tempScore;
    for (int i = n; i < totalList.size(); i++) {
      double minScore = 7.0;
      int minIndex = 0;
      for (int j = i; j < totalList.size(); j++) {
        if (j % 20 == 0)
          System.out.println("ON: " + i + "." + j);
        totalList.add(i, totalList.get(j));
        totalList.remove(j + 1);
        updateList();
        tempScore = getWinPercent(s, 1.1, "pickTopBot", true);
        if (tempScore < minScore) {
          minScore = tempScore;
          minIndex = j;
        }
        totalList.add(j + 1, totalList.get(i));
        totalList.remove(i);
      }
      totalList.add(i, totalList.get(minIndex));
      System.out.println("===============================");
      for (int k = n; k <= i; k++)
        System.out.println("NEW TOP WORD: " + totalList.get(k).getWord());
      System.out.println("===============================");
      totalList.remove(minIndex + 1);
    }
    for (Wordles w : totalList)
      System.out.println(w.getWord());
  } */

  /**
   * assigns an expected value for words that will be removed from list if that
   * word is guess to each word in list
   */
  public static void calculateEV(boolean hardMode) {
    ArrayList<Wordles> combinations = new ArrayList<Wordles>();
    String tempCom;
    int sumOfSquares;
    int count = 0;
    if (colorTable == null)
      fillTable();
    combinations.add(new Wordles("ggggg", 0));
    for (int i = 0; i < totalList.size(); i++) { // test for all possible words
      sumOfSquares = 0;
      for (Wordles w : combinations) {
        w.setFreq(0);
      }
      if (!hardMode || colorTable[i][0].length() == 5) {
        for (int k = 0; k < totalList.size(); k++) { // test each possible word against every possible word
          tempCom = colorTable[i][k + 1];
          if (tempCom.length() == 5) {
            boolean comFound = false; // creates a list with all possible combinations
            for (Wordles w : combinations) {
              if (w.getWord().equals(tempCom)) {
                w.setFreq(w.getFreq() + 1);
                comFound = true;
                break;
              }
            }
            if (!comFound) {
              combinations.add(new Wordles(tempCom, 1));
            }
          }
        }
        for (Wordles w : combinations) { // uses expected value formula with some rearranging
          sumOfSquares += Math.pow(w.getFreq(), 2);
        }
        if (hardMode) {
          list.get(count).setFreq((Math.pow(list.size(), 2) - sumOfSquares) / (double) list.size());
          count++;
        } else {
          totalList.get(i).setFreq((Math.pow(list.size(), 2) - sumOfSquares) / (double) list.size());
        }
      }
    }
  }

  /**
   * picks the word with the highest expected value in list and plays it as a
   * guess as well as eliminate all impossible answers
   * 
   * @return if guessCount = 1 then returns raise(word with highest EV from
   *         totalList), if list.size() = 1 returns only element in list,
   *         otherwise calculates EV and returns word with high
   */
  public static String EVBot(boolean hardMode) {
    try {
      if (list.size() == totalList.size()) {
      eliminate(checkGuess("raise"));
      return "raise";
    } else if (list.size() == 2) {
      eliminate(checkGuess(list.get(0).getWord()));
      if (list.size() == 0) System.out.println("Failed");
      return list.get(0).getWord();
    } else if (list.size() == 1) {
      return list.get(0).getWord();
    } else {
      calculateEV(hardMode);
    }
    String maxWord = highestEV(hardMode);
    if (maxWord.equals("xxxxx"))
      System.out.println("ERROR: COULD NOT FIND HIGHEST WORD");
    eliminate(checkGuess(maxWord));
    return maxWord;
    } catch (Exception e) {
      System.out.println(getAnswer());
      return "xxxxx";
    }
  }

  /**
   * Finds the word with the highestEV currently in list
   * 
   * @return String of the word with highest EV
   */
  public static String highestEV(boolean hardMode) {
    double maxEV = -1.0;
    String maxWord = "xxxxx";
    ArrayList<Wordles> arr;
    if (hardMode)
      arr = list;
    else
      arr = totalList;
    for (Wordles w : arr) {
      if (w.getFreq() > maxEV) {
        maxEV = w.getFreq();
        maxWord = w.getWord();
      }
    }
    return maxWord;
  }

  /**
   * Fills in a look up 2D array with every possible combination of guess and
   * answer
   */
  public static void fillTable() {
    colorTable = new String[totalList.size()][totalList.size() + 1];
    Wordles[] tempAns = new Wordles[5];
    for (int r = 0; r < colorTable.length; r++) {
      Wordles rowCheck = totalList.get(r);
      colorTable[r][0] = rowCheck.getWord();
      for (int c = 0; c < colorTable[r].length - 1; c++) {
        String tempCom = "bbbbb";
        for (int j = 0; j < 5; j++) {
          tempAns[j] = new Wordles(totalList.get(c).getWord().substring(j, j + 1), j); // deep copy
        }
        for (int g = 0; g < 5; g++) { // check for green
          if (tempAns[g].getWord().equals(rowCheck.getWord().substring(g, g + 1))) {
            tempCom = tempCom.substring(0, g) + "g" + tempCom.substring(g + 1);
            tempAns[g].setWord("/");
          }
        }
        for (int y = 0; y < 5; y++) { // check for yellow
          if (!tempCom.substring(y, y + 1).equals("g")) {
            for (int j = 0; j < 5; j++) {
              if (rowCheck.getWord().substring(y, y + 1).equals(tempAns[j].getWord())) {
                tempCom = tempCom.substring(0, y) + "y" + tempCom.substring(y + 1);
                tempAns[j].setWord("/");
                break;
              }
            }
          }
        }
        colorTable[r][c + 1] = tempCom;
      }
    }
  }

  public static Wordles[] convert(String word, String colors) {
    Wordles[] res = new Wordles[5];
    for (int i = 0; i < 5; i++) {
      String color = colors.substring(i, i + 1);
      double index = -1;
      if (color.equals("g"))
        index = i;
      else if (color.equals("y"))
        index = i + 5;
      res[i] = new Wordles(word.substring(i, i + 1), index);
    }
    return res;
  }

  public static int stringSearch(String word) {
    int min = 0;
    int max = totalSize;
    while (min <= max) {
      int mid = (min + max) / 2;
      if (totalList.get(mid).getWord().compareTo(word) < 0) {
        min = mid + 1;
      } else if (totalList.get(mid).getWord().compareTo(word) > 0) {
        max = mid - 1;
      } else {
        return mid;
      }
    }
    return -1;
  }

  public static void twoStageEV() {
    int sumOfSquares;
    int count = 0;
    double totalEV = -1;
    double twoEV = -1;
    ArrayList<Wordles> combinations = new ArrayList<Wordles>();
    if (colorTable == null)
      fillTable();
    for (int i = 0; i < totalList.size(); i++) { // test for all possible words
      sumOfSquares = 0;
      if (colorTable[i][0].length() == 5) {
        combinations = combinationList(i);
        for (Wordles w : combinations) { // uses expected value formula with some rearranging
          sumOfSquares += Math.pow(w.getFreq(), 2);
          twoEV += w.getFreq() * secondEV(totalList.get(i).getWord(), w.getWord());
        }
        totalEV = (Math.pow(list.size(), 2) - sumOfSquares + twoEV) / (double) list.size();
        list.get(count).setFreq((totalEV));
        count++;
      }
      if (i % 20 == 0)
        System.out.println(i);
    }
  }

  public static ArrayList<Wordles> combinationList(int guessIndex) {
    String tempCom;
    ArrayList<Wordles> combinations = new ArrayList<Wordles>();
    for (int k = 0; k < list.size(); k++) { // test each possible word against every possible word
      int ansIndex = stringSearch(list.get(k).getWord());
      tempCom = colorTable[guessIndex][ansIndex + 1];
      if (tempCom.length() == 5) {
        boolean comFound = false; // creates a list with all possible combinations
        for (Wordles w : combinations) {
          if (w.getWord().equals(tempCom)) {
            w.setFreq(w.getFreq() + 1);
            comFound = true;
            break;
          }
        }
        if (!comFound) {
          combinations.add(new Wordles(tempCom, 1));
        }
      }
    }
    return combinations;
  }

  public static double secondEV(String firstGuess, String combination) {
    eliminate(convert(firstGuess, combination));
    ArrayList<Wordles> combinations = combinationList(stringSearch(firstGuess));
    int sumOfSquares = 0;
    for (Wordles w : combinations) {
      sumOfSquares += Math.pow(w.getFreq(), 2);
    }
    updateList(); // problem since you are going to earse all your progress
    return (Math.pow(list.size(), 2) - sumOfSquares) / (double) list.size();
  }
}

class Wordles {
  private String word;
  private double frequency;

  public Wordles(String w, double f) {
    word = w;
    frequency = f;
  }

  public String getWord() {
    return word;
  }

  public double getFreq() {
    return frequency;
  }

  public void setWord(String w) {
    word = w;
  }

  public void setFreq(double f) {
    frequency = f;
  }

  public void addFreq() {
    frequency++;
  }

  public String toString() {
    return word + "(" + frequency + ")";
  }
}
