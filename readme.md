The example of Gameplay usage for testing of TestBot:
The Gameplay reflects the logic of processing of bot step. The developer can debug bot logic locally without of using the server. 

```
class TestBot{
    public static void main(String[] args) {
            Gameplay gameplay = new Gameplay();
            List<Bot> bots = Arrays.asList(new MyBot("1"), new MyBot("2"));
            List<String> botNames = bots.stream().map(Bot::getName).collect(toList());
            ModelGameState mgs = gameplay.createMatch(10, 20, bots, 100L, 0.9, 0).getGameState();
            for (int it = 0; it < 100; it++) {
                for (int k = 0; k < bots.size(); k++) {
                    GameState gs = gameplay.getClientGameState(mgs, k);
                    Move move = bots.get(k).move(gs);
                    gameplay.step(mgs, k, move);
                    System.out.println("move = " + move + " current game state = \n" +
                    gameplay.describeGameState(mgs, botNames, false, false));
                }
            }
        }
    }
}
```