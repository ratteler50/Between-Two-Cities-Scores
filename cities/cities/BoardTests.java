package cities;

import static cities.Tile.bed;
import static cities.Tile.drink;
import static cities.Tile.factory;
import static cities.Tile.food;
import static cities.Tile.house;
import static cities.Tile.music;
import static cities.Tile.office;
import static cities.Tile.park;
import static cities.Tile.shop;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class BoardTests {

  @Test
  public void allTheSameTypeofTavernOnlyScoresOnePer() {
    Board board = new Board(1,
        ImmutableList.of( //
            bed(), bed(), bed(), bed(), //
            bed(), bed(), bed(), bed(), //
            bed(), bed(), bed(), bed(), //
            bed(), bed(), bed(), bed()));

    assertThat(board.scoreTaverns()).isEqualTo(16);
  }

  @Test
  public void boardCanPrintItself() {
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("#1 in factories.\n");
    expectedOutput.append("┌---------------------------------------------------┐\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|  FACTORY   |TAVERN_DRINK|   OFFICE   |    PARK    |\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|------------|------------|------------|------------|\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|    SHOP    |TAVERN_MUSIC|  FACTORY   |   HOUSE    |\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|------------|------------|------------|------------|\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|   OFFICE   |    PARK    |    SHOP    | TAVERN_BED |\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|------------|------------|------------|------------|\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("|  FACTORY   |TAVERN_FOOD |   OFFICE   |    PARK    |\n");
    expectedOutput.append("|            |            |            |            |\n");
    expectedOutput.append("└---------------------------------------------------┘");
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), drink(), office(), park(), //
            shop(), music(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), food(), office(), park()));
    assertThat(board.toString()).isEqualTo(expectedOutput.toString());
  }

  @Test
  public void boardCanScoreFactoriesRankOne() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), house(), office(), park(), //
            shop(), bed(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.scoreFactories()).isEqualTo(3 * 4);
  }

  @Test
  public void boardCanScoreFactoriesRankTwo() {
    Board board = new Board(2,
        ImmutableList.of( //
            factory(), house(), office(), park(), //
            shop(), bed(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.scoreFactories()).isEqualTo(3 * 3);
  }

  @Test
  public void boardCanScoreFactoriesRankThree() {
    Board board = new Board(3,
        ImmutableList.of( //
            factory(), house(), office(), park(), //
            shop(), bed(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.scoreFactories()).isEqualTo(3 * 2);
  }

  @Test
  public void boardWithoutRightNumberOfTilesThrows() {
    try {
      new Board(1, ImmutableList.<Tile>of());
      assert_().fail();
    } catch (IllegalArgumentException expected) {
      // Expected exception.
    }
  }


  @Test
  public void partialTavernSetsScoreCorrectly() {
    Board board = new Board(1,
        ImmutableList.of( //
            bed(), bed(), bed(), bed(), //
            music(), music(), music(), factory(), //
            food(), food(), factory(), factory(), //
            drink(), factory(), factory(), factory()));

    assertThat(board.scoreTaverns()).isEqualTo(31);
  }

  @Test
  public void testGraph() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), house(), office(), park(), //
            shop(), bed(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.graph.nodes().size()).isEqualTo(16);
    assertThat(board.graph.edges().size()).isEqualTo(24);
  }

  @Test
  public void parkGroupSizesCorrectlyDecided() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), park(), office(), park(), //
            shop(), park(), factory(), park(), //
            park(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.getParkGroupSizes()).containsExactly(1, 2, 4);
  }

  @Test
  public void parksCorrectlyScored() {
    Board board = new Board(2,
        ImmutableList.of( //
            factory(), park(), office(), park(), //
            shop(), park(), factory(), park(), //
            park(), park(), shop(), bed(), //
            factory(), house(), office(), park()));
    assertThat(board.getParkGroupSizes()).containsExactly(4,2,1);
    assertThat(board.scoreParks()).isEqualTo(23);
  }

  @Test
  public void testHouseScore() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), drink(), office(), house(), //
            shop(), music(), factory(), house(), //
            office(), park(), shop(), bed(), //
            factory(), food(), office(), park()));
    assertThat(board.scoreHouses()).isEqualTo(6);
  }

  @Test
  public void testIsTavern() {
    assertThat(bed().isTavern()).isTrue();
    assertThat(factory().isTavern()).isFalse();
  }

  @Test
  public void testOfficeOnlyGetsBonusOnceForTavern() {
    Board board = new Board(1,
        ImmutableList.of( //
            drink(), office(), drink(), drink(), //
            drink(), drink(), drink(), drink(), //
            drink(), drink(), drink(), drink(), //
            drink(), drink(), drink(), drink()));
    assertThat(board.scoreOffices()).isEqualTo(2);
  }

  @Test
  public void testOfficeScore() {
    Board board = new Board(1,
        ImmutableList.of( //
            office(), office(), office(), office(), //
            office(), office(), office(), office(), //
            office(), office(), office(), office(), //
            office(), office(), office(), office()));
    assertThat(board.scoreOffices()).isEqualTo(52);
  }

  @Test
  public void testOfficeScoreWithNoBonus() {
    Board board = new Board(1,
        ImmutableList.of( //
            office(), office(), office(), office(), //
            office(), office(), office(), office(), //
            shop(), shop(), shop(), shop(), //
            shop(), drink(), shop(), shop()));
    assertThat(board.scoreOffices()).isEqualTo(24);
  }

  @Test
  public void twoOfficesCanGetBonusFromSameTavern() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), office(), factory(), factory(), //
            factory(), drink(), factory(), factory(), //
            factory(), office(), factory(), factory(), //
            factory(), factory(), factory(), factory()));
    assertThat(board.scoreOffices()).isEqualTo(5); // 3 + 2 bonus
  }

  @Test
  public void storeInLShape() {
    Board board = new Board(1,
        ImmutableList.of( //
            shop(), shop(), shop(), shop(), //
            shop(), house(), office(), music(), //
            shop(), park(), office(), office(), //
            shop(), park(), house(), park()));

    assertThat(board.getStoreGroupSizes()).containsExactly(3, 4);
  }

  @Test
  public void storeInTShape() {
    Board board = new Board(1,
        ImmutableList.of( //
            park(), park(), shop(), park(), //
            shop(), shop(), shop(), shop(), //
            park(), park(), shop(), park(), //
            park(), park(), shop(), park()));

    assertThat(board.getStoreGroupSizes()).containsExactly(1, 2, 4);
  }

  @Test
  public void storeInTShapeWithExtra() {
    Board board = new Board(1,
        ImmutableList.of( //
            park(), park(), shop(), park(), //
            shop(), shop(), shop(), shop(), //
            park(), shop(), shop(), park(), //
            park(), park(), shop(), park()));

    assertThat(board.getStoreGroupSizes()).containsExactly(1, 1, 2, 4);
  }

  @Test
  public void cityScoringExampleOne() {
    Board board = new Board(1,
        ImmutableList.of( //
            shop(), shop(), shop(), shop(), //
            house(), house(), office(), music(), //
            office(), park(), office(), office(), //
            office(), park(), house(), park()));

    assertThat(board.getScore()).isEqualTo(56);
  }

  @Test
  public void cityScoringExampleTwo() {
    Board board = new Board(1,
        ImmutableList.of( //
            music(), factory(), factory(), factory(), //
            park(), park(), factory(), park(), //
            shop(), shop(), music(), park(), //
            house(), house(), house(), factory()));
    assertThat(board.getScore()).isEqualTo(52);
  }

  @Test
  public void cityScoringExampleThree() {
    Board board = new Board(2,
        ImmutableList.of( //
            music(), drink(), factory(), factory(), //
            house(), food(), factory(), park(), //
            shop(), house(), bed(), park(), //
            house(), house(), house(), office()));
    assertThat(board.getScore()).isEqualTo(62);
  }

  @Test
  public void cityScoringExampleFour() {
    Board board = new Board(3,
        ImmutableList.of( //
            music(), drink(), factory(), factory(), //
            office(), food(), office(), bed(), //
            office(), office(), office(), office(), //
            shop(), shop(), shop(), shop()));
    assertThat(board.getScore()).isEqualTo(62);
  }

  @Test
  public void cityScoringExampleFive() {
    Board board = new Board(3,
        ImmutableList.of( //
            music(), drink(), house(), house(), //
            shop(), food(), office(), bed(), //
            shop(), office(), house(), house(), //
            shop(), factory(), park(), park()));
    assertThat(board.getScore()).isEqualTo(62);
  }

  @Test
  public void cityScoringExampleSix() {
    Board board = new Board(1,
        ImmutableList.of( //
            factory(), shop(), factory(), factory(), //
            park(), factory(), park(), park(), //
            factory(), bed(), house(), park(), //
            park(), park(), house(), house()));
    assertThat(board.getScore()).isEqualTo(57);
  }
}
