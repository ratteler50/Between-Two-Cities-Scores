package cities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import cities.Tile.Type;

public class BoardGenerator {
  static final long MAX_LONG_EXCLUSIVE = (long) (3 * Math.pow(9, 16));
  private static final AtomicLong boardNumber = new AtomicLong();

  Stream<Board> randomBoards() {
    return new Random().longs(0, MAX_LONG_EXCLUSIVE)
        .mapToObj(BoardGenerator::generateBoardFromLong);
  }

  public static Stream<Board> sequentialBoards(long startingBoard) {
    return LongStream.iterate(startingBoard, n -> n + 1 % MAX_LONG_EXCLUSIVE)
        .mapToObj(BoardGenerator::generateBoardFromLong);
  }


  public static Board generateBoardFromLong(long lng) {
    List<Type> types = ImmutableList.copyOf(Type.values());
    List<Tile> tileList = new ArrayList<>(16);
    int rank = (int) (lng % 3) + 1;
    lng = lng / 3;
    while (tileList.size() < 16) {
      tileList.add(new Tile(types.get((int) (lng % 9))));
      lng = lng / 9;
    }
    Collections.reverse(tileList);
    return new Board(rank, ImmutableList.copyOf(tileList));
  }

  public static void main(String[] args) {
    Map<Integer, Long> scores = new TreeMap<>();
    Set<Long> highestScoringBoards = new HashSet<>();
    int bestScore = 0;
    startTimer();
    while (boardNumber.get() < MAX_LONG_EXCLUSIVE) {
      Board newBoard = generateBoardFromLong(boardNumber.get());
      int newBoardScore = newBoard.getScore();
      long previousCount = scores.getOrDefault(newBoardScore, 0L);
      scores.put(newBoardScore, previousCount + 1);
      if (newBoardScore >= bestScore) {
        if (newBoardScore > bestScore) {
          highestScoringBoards.clear();
          bestScore = newBoardScore;
        }
        highestScoringBoards.add(boardNumber.get());
        bestScore = newBoardScore;
        System.err.println(newBoard);
      }
      boardNumber.incrementAndGet();
    }
  }

  private static void startTimer() {
    new Timer(true).schedule(new TimerTask() {

      @Override
      public void run() {
        System.err.println(String.format("Currently on board # %d", boardNumber.get()));
      }
    }, 0, 10000);
  }
}
