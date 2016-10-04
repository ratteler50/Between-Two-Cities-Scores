package cities;

import static cities.Tile.Type.FACTORY;
import static cities.Tile.Type.HOUSE;
import static cities.Tile.Type.TAVERN_MUSIC;
import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Collectors;

import org.junit.Test;

public class BoardGeneratorTests {

  @Test
  public void generateBoardFromLong() {
    Board board0 = BoardGenerator.generateBoardFromLong(0L);
    assertThat(board0.graph.nodes().stream().map(Tile::getType).collect(Collectors.toList()))
        .containsExactly(FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY,
            FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY)
        .inOrder();
    assertThat(board0.factoryRanking).isEqualTo(1);
  }

  @Test
  public void testGenerateTwo() {
    Board board2 = BoardGenerator.generateBoardFromLong(2L);
    assertThat(board2.graph.nodes().stream().map(Tile::getType).collect(Collectors.toList()))
        .containsExactly(FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY,
            FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY)
        .inOrder();
    assertThat(board2.factoryRanking).isEqualTo(3);
  }

  @Test
  public void testGenerateThree() {
    Board board3 = BoardGenerator.generateBoardFromLong(3L);
    assertThat(board3.graph.nodes().stream().map(Tile::getType).collect(Collectors.toList()))
        .containsExactly(FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY,
            FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, FACTORY, HOUSE)
        .inOrder();
    assertThat(board3.factoryRanking).isEqualTo(1);
  }

  @Test
  public void generateBoardFromLongEnd() {
    Board boardMAX = BoardGenerator.generateBoardFromLong(BoardGenerator.MAX_LONG_EXCLUSIVE - 1);
    assertThat(boardMAX.graph.nodes().stream().map(Tile::getType).collect(Collectors.toList()))
        .containsExactly(TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC,
            TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC,
            TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC, TAVERN_MUSIC)
        .inOrder();
    assertThat(boardMAX.factoryRanking).isEqualTo(3);
  }
}
