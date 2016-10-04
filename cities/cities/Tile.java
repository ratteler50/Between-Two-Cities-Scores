package cities;

import com.google.common.collect.ImmutableSet;

public class Tile {

  enum Type {
    FACTORY, HOUSE, OFFICE, PARK, SHOP, TAVERN_BED, TAVERN_DRINK, TAVERN_FOOD, TAVERN_MUSIC
  }

  public static Tile bed() {
    return new Tile(Type.TAVERN_BED);
  }

  public static Tile drink() {
    return new Tile(Type.TAVERN_DRINK);
  }

  public static Tile factory() {
    return new Tile(Type.FACTORY);
  }

  public static Tile food() {
    return new Tile(Type.TAVERN_FOOD);
  }

  public static Tile house() {
    return new Tile(Type.HOUSE);
  }

  public static Tile music() {
    return new Tile(Type.TAVERN_MUSIC);
  }

  public static Tile office() {
    return new Tile(Type.OFFICE);
  }

  public static Tile park() {
    return new Tile(Type.PARK);
  }

  public static Tile shop() {
    return new Tile(Type.SHOP);
  }


  private final Type type;

  Tile(Type type) {
    this.type = type;
  }

  Type getType() {
    return type;
  }

  boolean isFactory() {
    return type.equals(Type.FACTORY);
  }

  boolean isHouse() {
    return type.equals(Type.HOUSE);
  }

  boolean isOffice() {
    return type.equals(Type.OFFICE);
  }

  boolean isPark() {
    return type.equals(Type.PARK);
  }

  boolean isShop() {
    return type.equals(Type.SHOP);
  }

  boolean isTavern() {
    return ImmutableSet.of(Type.TAVERN_BED, Type.TAVERN_DRINK, Type.TAVERN_FOOD, Type.TAVERN_MUSIC)
        .contains(type);
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
