# Alch Buddy

A RuneLite plugin for comparing Grand Exchange and alchemy values.

- Highlights bank and inventory items when their high-alch value is greater than their current GE price plus one nature rune.
- Lists the full tradeable-item catalogue in a searchable sidebar with sortable columns for GE price, low alch, high alch, GE buy limit, and per-item high-alch profit.
- Uses RuneLite's configured item-price source and current nature-rune price.
- Never casts spells, clicks items, or changes menus.

Items with missing price data are not highlighted. Profit is calculated before any other costs and assumes one nature rune per cast.

## Development

Build and test with Java 11:

```text
./gradlew clean test
./gradlew run
```

The `run` task starts RuneLite in developer mode with this plugin loaded.
