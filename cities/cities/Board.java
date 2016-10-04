package cities;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table.Cell;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableUndirectedGraph;
import com.google.common.graph.UndirectedGraph;

public class Board {
  private static ImmutableUndirectedGraph<Tile, String> buildGraph(
      ImmutableTable<Integer, Integer, Tile> layout) {
    UndirectedGraph<Tile, String> tmpGraph = Graphs.createUndirected(
        Graphs.config().expectedNodeCount(16).expectedEdgeCount(24).noSelfLoops());
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        if (row < 3) {
          tmpGraph.addEdge(String.format("%d,%d,down", row, col), layout.get(row, col),
              layout.get(row + 1, col));
        }
        if (col < 3) {
          tmpGraph.addEdge(String.format("%d,%d,right", row, col), layout.get(row, col),
              layout.get(row, col + 1));
        }
      }
    }
    return ImmutableUndirectedGraph.copyOf(tmpGraph);
  }

  private static ImmutableTable<Integer, Integer, Tile> buildTable(List<Tile> tiles) {
    int i = 0;
    ImmutableTable.Builder<Integer, Integer, Tile> builder = ImmutableTable.builder();
    for (Tile tile : tiles) {
      int row = i / 4;
      int column = i % 4;
      builder.put(row, column, tile);
      i++;
    }
    return builder.build();
  }

  final ImmutableUndirectedGraph<Tile, String> graph;

  final ImmutableTable<Integer, Integer, Tile> layout;

  final int factoryRanking;

  Board(int factoryRanking, List<Tile> tiles) {
    Preconditions.checkArgument(ImmutableSet.of(1,2,3).contains(factoryRanking));
    Preconditions.checkArgument(tiles.size() == 16, "There must be exactly 16 tiles!");
    this.factoryRanking = factoryRanking;
    layout = buildTable(tiles);
    graph = buildGraph(layout);
  }

  private String center(String toCenter) {
    int spacesToAdd = 12 - toCenter.length();
    return "          ".substring(0, spacesToAdd / 2).concat(toCenter);
  }

  @VisibleForTesting
  Collection<Integer> getParkGroupSizes() {
    Set<Set<Tile>> groups = new HashSet<>();
    Set<Tile> allParks =
        graph.nodes().stream().filter(Tile::isPark).collect(toCollection(HashSet::new));
    for (Tile park : allParks) {
      List<Set<Tile>> groupsContainingNeighbor = new ArrayList<>();
      for (Set<Tile> group : groups) {
        if (graph.adjacentNodes(park).stream().anyMatch(p -> group.contains(p))) {
          groupsContainingNeighbor.add(group);
        }
      }
      Set<Tile> mergedGroup = new HashSet<>();
      mergedGroup.add(park);
      groupsContainingNeighbor.forEach(groups::remove);
      groupsContainingNeighbor.forEach(mergedGroup::addAll);
      groups.add(mergedGroup);
    }

    List<Integer> sizes =
        groups.stream().map(group -> group.size()).collect(toCollection(ArrayList::new));
    return sizes;
  }

  private String getRowAsString(int row) {
    return String.format("|%-12s|%-12s|%-12s|%-12s|", center(layout.get(row, 0).toString()),
        center(layout.get(row, 1).toString()), center(layout.get(row, 2).toString()),
        center(layout.get(row, 3).toString()));
  }

  private Long getScoreForTaverns(Long numSetsOfLargestSize, int numElemsInEachSet) {
    if (numElemsInEachSet == 1) {
      return numSetsOfLargestSize;
    } else if (numElemsInEachSet == 2) {
      return 4 * numSetsOfLargestSize;
    } else if (numElemsInEachSet == 3) {
      return 9 * numSetsOfLargestSize;
    } else {
      return 17 * numSetsOfLargestSize;
    }
  }

  private boolean isTileAdjacentToFactory(Tile house) {
    return graph.adjacentNodes(house).stream().anyMatch(Tile::isFactory);
  }

  private int pointIfExists(Predicate<? super Tile> predicate) {
    return graph.nodes().stream().anyMatch(predicate) ? 1 : 0;
  }

  /**
   * In the city (or cities, if tied) with the most factory tiles compared to other cities, each
   * factory tile scores 4 points. In the city or cities with the second most factory tiles, each
   * factory tile scores 3 points. In all other cities, each factory tile scores 2 points.
   */
  int scoreFactories() {
    int numFactories = (int) layout.values().stream().filter(Tile::isFactory).count();
    if (factoryRanking == 1) {
      return 4 * numFactories;
    } else if (factoryRanking == 2) {
      return 3 * numFactories;
    } else {
      return 2 * numFactories;
    }
  }

  /**
   * Each house tile is worth 1 point for each other building type (excluding houses) in the city
   * (regardless of location or adjacency).
   * <p>
   * If there is one other building type in the city, each house is worth 1 point. If there are five
   * other building types in the city, each house is worth 5 points. All taverns count as a single
   * building type.
   * <p>
   * If a house tile is adjacent to a factory tile, that house tile instead scores 1 point (people
   * don’t want to live right next to a factory).
   */
  int scoreHouses() {
    int perHouseScore = pointIfExists(Tile::isTavern) + pointIfExists(Tile::isFactory)
        + pointIfExists(Tile::isShop) + pointIfExists(Tile::isOffice) + pointIfExists(Tile::isPark);

    return graph.nodes().stream().filter(Tile::isHouse)
        .mapToInt(house -> isTileAdjacentToFactory(house) ? 1 : perHouseScore).sum();

  }


  /**
   * Offices score 1 point for one office tile, 3 points for two office tiles, 6 points for three,
   * 10 points for four, 15 points for five, or 21 points for six office tiles (regardless of
   * location or adjacency to each other).
   * <p>
   * If a city has seven office tiles, the seventh starts a new set of offices, and the scoring
   * starts over again at 1 for that set. If a city has 7 office tiles, it scores 22 points for
   * offices (21 + 1).
   * <p>
   * In addition, each office gets +1 point if it is adjacent to at least one tavern. Each office
   * tile can only score 1 bonus point, but several offices can receive that point from the same
   * adjacent tavern tile.
   */
  int scoreOffices() {
    int numOffices = (int) graph.nodes().stream().filter(Tile::isOffice).count();
    int fullSetScore = (numOffices / 6) * 21;
    int remaining = numOffices % 6;
    int remainingScore = (remaining * (remaining + 1)) / 2;
    int bonusScore = (int) graph.nodes().stream().filter(Tile::isOffice)
        .filter(tile -> graph.adjacentNodes(tile).stream().anyMatch(Tile::isTavern)).count();
    return fullSetScore + remainingScore + bonusScore;
  }

  private int scoreParkGroup(Integer parkGroupSize) {
    switch (parkGroupSize) {
      case 0:
        return 0;
      case 1:
        return 2;
      case 2:
        return 8;
      case 3:
        return 12;
      default:
        return parkGroupSize + 9;
    }
  }

  /**
   * Parks score in groups of one or more connected parks.
   * <p>
   * A single unconnected park is worth 2 points. Two connected parks are worth 8. Three connected
   * parks are worth 12. Every additional connected park after the third increases the score by 1.
   * <p>
   * You may have more than one unconnected park group in your city. Score each park group
   * separately. To be in a connected group, a park must share a border with another park. The group
   * does not have to form a straight line.
   */
  int scoreParks() {
    Collection<Integer> parkGroupSizes = getParkGroupSizes();
    int totalScore = 0;
    for (Integer parkGroup : parkGroupSizes) {
      totalScore += scoreParkGroup(parkGroup);
    }
    return totalScore;
  }

  /**
   * Shops score when connected in a straight line (row or column): 2 points for one shop tile, 5
   * points for two connected shop tiles, 10 points for three connected shop tiles in a straight
   * line, or 16 points for four connected shop tiles in a straight line. If lines of shops cross
   * (in an L or T or + shape), each tile can only be counted for one of the sets.
   */
  int scoreShops() {
    Collection<Integer> storeGroupSizes = getStoreGroupSizes();
    int totalScore = 0;
    for (Integer storeGroup : storeGroupSizes) {
      totalScore += scoreStoreGroup(storeGroup);
    }
    return totalScore;
  }

  @VisibleForTesting
  Collection<Integer> getStoreGroupSizes() {
    Set<Set<Tile>> groups = new HashSet<>();
    for (Cell<Integer, Integer, Tile> shopCell : layout.cellSet().stream()
        .filter(cell -> cell.getValue().isShop())
        .sorted(compareRows().thenComparing(compareColumns())).collect(Collectors.toList())) {
      Set<Tile> rowSet = getRowSet(shopCell, groups);
      Set<Tile> columnSet = getColumnSet(shopCell, groups);
      if (rowSet.size() < columnSet.size()) {
        groups.add(columnSet);
      } else if (rowSet.size() > 0) {
        groups.add(rowSet);
      }
    }
    return groups.stream().map(group -> group.size()).collect(toCollection(ArrayList::new));
  }

  private boolean shopAlreadyInSet(Set<Set<Tile>> groups, Tile currentTile) {
    return groups.stream().anyMatch(group -> group.contains(currentTile));
  }

  private Set<Tile> getColumnSet(Cell<Integer, Integer, Tile> shopCell, Set<Set<Tile>> groups) {
    int currRow = shopCell.getRowKey();
    int column = shopCell.getColumnKey();
    Set<Tile> set = new HashSet<>();
    Tile currentTile = layout.get(currRow, column);
    while (currentTile != null && currentTile.isShop() && !shopAlreadyInSet(groups, currentTile)) {
      set.add(currentTile);
      currRow++;
      currentTile = layout.get(currRow, column);
    }
    return ImmutableSet.copyOf(set);
  }

  private Set<Tile> getRowSet(Cell<Integer, Integer, Tile> shopCell, Set<Set<Tile>> groups) {
    int row = shopCell.getRowKey();
    int currCol = shopCell.getColumnKey();
    Set<Tile> set = new HashSet<>();
    Tile currentTile = layout.get(row, currCol);
    while (currentTile != null && currentTile.isShop() && !shopAlreadyInSet(groups, currentTile)) {
      set.add(currentTile);
      currCol++;
      currentTile = layout.get(row, currCol);
    }
    return ImmutableSet.copyOf(set);
  }

  private Comparator<Cell<Integer, Integer, Tile>> compareRows() {
    return comparingInt(Cell::getRowKey);
  }

  private Comparator<Cell<Integer, Integer, Tile>> compareColumns() {
    return comparingInt(Cell::getColumnKey);
  }


  private int scoreStoreGroup(Integer storeGroupSize) {
    switch (storeGroupSize) {
      case 0:
        return 0;
      case 1:
        return 2;
      case 2:
        return 5;
      case 3:
        return 10;
      default:
        return 16;
    }
  }

  /**
   * There are four different tavern tiles, each with a different icon inside a red diamond.
   * <p>
   * Taverns score 1 point for one in a city, 4 points for two different taverns, 9 points for three
   * different taverns, or 17 points for all four different taverns in a city (regardless of
   * location or adjacency to each other)
   */
  int scoreTaverns() {
    Collection<Long> tavernTypeCounts = graph.nodes().stream().filter(Tile::isTavern)
        .collect(Collectors.groupingBy(Tile::getType, counting())).values();
    int totalScore = 0;
    while (!tavernTypeCounts.isEmpty()) {
      int numElemsInEachSet = tavernTypeCounts.size();
      Long numSetsOfLargestSize = tavernTypeCounts.stream().reduce(Long::min).orElse(0L);
      totalScore += getScoreForTaverns(numSetsOfLargestSize, numElemsInEachSet);
      tavernTypeCounts = tavernTypeCounts.stream().map(n -> (n - numSetsOfLargestSize))
          .filter(n -> (n > 0)).collect(toCollection(ArrayList::new));
    }
    return totalScore;
  }

  public int getScore() {
    return scoreFactories() + scoreHouses() + scoreOffices() + scoreParks() + scoreShops()
        + scoreTaverns();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + factoryRanking;
    result = prime * result + ((graph == null) ? 0 : graph.hashCode());
    result = prime * result + ((layout == null) ? 0 : layout.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Board other = (Board) obj;
    if (factoryRanking != other.factoryRanking)
      return false;
    if (graph == null) {
      if (other.graph != null)
        return false;
    } else if (!graph.equals(other.graph))
      return false;
    if (layout == null) {
      if (other.layout != null)
        return false;
    } else if (!layout.equals(other.layout))
      return false;
    return true;
  }

  @Override
  public String toString() {
    String topRow = "┌---------------------------------------------------┐";
    String bottomRow = "└---------------------------------------------------┘";
    String middleSeparators = "|------------|------------|------------|------------|";
    String space = "|            |            |            |            |";
    String row0 = getRowAsString(0);
    String row1 = getRowAsString(1);
    String row2 = getRowAsString(2);
    String row3 = getRowAsString(3);
    return "#" + factoryRanking + " in factories; Score: " + getScore() + "\n"
        + Stream
            .of(topRow, space, row0, space, middleSeparators, space, row1, space, middleSeparators,
                space, row2, space, middleSeparators, space, row3, space, bottomRow)
            .collect(joining("\n"));
  }
}
