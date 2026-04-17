# Pokémon GO Search Syntax Reference

Complete reference for every search keyword supported in Pokémon GO as of early 2026.

---

## Operators

| Operator | Symbol | Example | Meaning |
|----------|--------|---------|---------|
| AND      | `&`    | `shiny&lucky` | Both conditions must be true |
| OR       | `,`    | `shiny,lucky` | Either condition must be true |
| NOT      | `!`    | `!shiny` | Condition must be false |
| Group    | `()`   | `(shiny,lucky)&cp-1500` | Override precedence |

**Precedence:** `&` (AND) binds more tightly than `,` (OR), just like multiplication vs. addition.  
`a,b&c` is equivalent to `a,(b&c)`.

---

## Pokémon Identity

| Keyword | Example | Matches |
|---------|---------|---------|
| *(name)* | `pikachu` | Partial name match |
| `#N` | `#151` | Single Pokédex number |
| `#N-M` | `#1-151` | Pokédex range |

---

## Types

Use the type name directly:

`normal` · `fire` · `water` · `electric` · `grass` · `ice` · `fighting` · `poison` · `ground` · `flying` · `psychic` · `bug` · `rock` · `ghost` · `dragon` · `dark` · `steel` · `fairy`

---

## Generations

| Keyword | Region | Aliases |
|---------|--------|---------|
| `gen1` | Kanto | `kanto` |
| `gen2` | Johto | `johto` |
| `gen3` | Hoenn | `hoenn` |
| `gen4` | Sinnoh | `sinnoh` |
| `gen5` | Unova | `unova` |
| `gen6` | Kalos | `kalos` |
| `gen7` | Alola | `alola` |
| `gen8` | Galar | `galar` |
| `gen9` | Paldea | `paldea` |

---

## Combat Stats

### CP

| Syntax | Meaning |
|--------|---------|
| `cp1500` | Exactly 1500 CP |
| `cp-1500` | Up to 1500 CP |
| `cp1500-` | 1500 CP and above |
| `cp1000-1500` | Between 1000 and 1500 CP |

### HP

Same syntax as CP with `hp` prefix: `hp-100`, `hp50-100`, etc.

### IV Stars

| Keyword | Star Rating | IV Range |
|---------|------------|----------|
| `0*` | 0 stars | 0% |
| `1*` | 1 star | 51–63% |
| `2*` | 2 stars | 66–80% |
| `3*` | 3 stars | 82–97% |
| `4*` | 4 stars (perfect) | 100% |

### Individual IVs

Same range syntax as CP/HP:

| Prefix | Stat |
|--------|------|
| `atk` | Attack IV (0–15) |
| `def` | Defense IV (0–15) |
| `sta` | Stamina IV (0–15) |

Examples: `atk15`, `def-14`, `sta10-15`

### Level

| Syntax | Meaning |
|--------|---------|
| `level40` | Exactly level 40 |
| `level-40` | Level 40 and below |
| `level40-` | Level 40 and above |
| `level20-40` | Between levels 20 and 40 |

---

## Moves

| Keyword | Matches |
|---------|---------|
| `@<movename>` | Has this move in any slot |
| `@1<type>` | Fast move is of this type (e.g. `@1fire`) |
| `@2<type>` | Charge move is of this type (e.g. `@2dragon`) |
| `@legacy` | Has at least one legacy move |
| `@elite` | Has a move obtained via Elite TM |
| `@special` | Has a special/event-exclusive move |
| `@purified` | Has the Return move (purified Pokémon) |

Example: `@ember` matches any Pokémon with Ember in any move slot.

---

## Status & Special

| Keyword | Meaning |
|---------|---------|
| `shiny` | Is a shiny Pokémon |
| `lucky` | Is a Lucky Pokémon |
| `shadow` | Is a Shadow Pokémon |
| `purified` | Is a Purified Pokémon |
| `legendary` | Is a Legendary |
| `mythical` | Is a Mythical |
| `ultrabeast` | Is an Ultra Beast |
| `mega` | Is currently Mega-evolved or Primal |
| `defender` | Is currently defending a Gym |
| `traded` | Was obtained via trade |
| `costume` | Has a costume |
| `new` | Is an unseen form for your account |
| `weather` | Is currently weather-boosted |
| `regional` | Is a regional-exclusive Pokémon |
| `baby` | Is a Baby Pokémon |
| `background` | Is a Background Pokémon |

### Pokémon Size

| Keyword | Meaning |
|---------|---------|
| `xxs` | Extra extra small size |
| `xs` | Extra small size |
| `xl` | Extra large size |
| `xxl` | Extra extra large size |

### Combat Type Filters

| Keyword | Meaning |
|---------|---------|
| `<fire` | Weak to Fire type |
| `>fire` | Strong against Fire type |

Replace `fire` with any Pokémon type.

---

## Evolution

| Keyword | Meaning |
|---------|---------|
| `evolve` | Has enough candy/items to evolve |
| `tradeevolve` | Can benefit from trade evolution |
| `megaevolve` | Can Mega Evolve |
| `evolvenew` | Evolving would register a new Pokédex entry |
| `item` | Requires an evolution item |
| `mega0` | Mega level 0 (never Mega evolved) |
| `mega1` | Mega level 1 |
| `mega2` | Mega level 2 |
| `mega3` | Mega level 3 (max Mega level) |

---

## Egg & Hatch

| Keyword | Meaning |
|---------|---------|
| `egg` | Currently inside an egg |
| `eggsonly` | Show eggs only |
| `hatched` | Hatched from an egg |
| `2km` | From a 2 km egg |
| `5km` | From a 5 km egg |
| `7km` | From a 7 km (Gift) egg |
| `10km` | From a 10 km egg |
| `12km` | From a 12 km (Adventure Sync) egg |
| `distance<N>` | Walked `N` km with as buddy |
| `distance<N>-<M>` | Walked between N and M km |

---

## Buddy

| Keyword | Buddy Level |
|---------|-------------|
| `buddy` | Has any buddy history |
| `buddy0` | Neutral |
| `buddy1` | Good Buddy (1 heart) |
| `buddy2` | Great Buddy |
| `buddy3` | Ultra Buddy |
| `buddy4` | Best Buddy |

---

## Gender

| Keyword | Meaning |
|---------|---------|
| `male` | Male ♂ |
| `female` | Female ♀ |
| `genderunknown` | Gender unknown / genderless |

---

## Regional Forms

| Keyword | Form |
|---------|------|
| `alolan` | Alolan form |
| `galarian` | Galarian form |
| `hisuian` | Hisuian form |
| `paldean` | Paldean form |

---

## Tags & Favorites

| Keyword | Meaning |
|---------|---------|
| `favorite` | Marked as a favorite (★) |
| `#<tagname>` | Has a specific custom tag |

Example: `#raiders` matches Pokémon tagged "raiders".

---

## Encounter & Origin

| Keyword | Meaning |
|---------|---------|
| `raid` | Caught in a Raid |
| `remoteraid` | Caught in a Remote Raid |
| `megaraid` | Caught in a Mega Raid |
| `exraid` | Caught in an EX Raid |
| `primalraid` | Caught in a Primal Raid |
| `research` | Obtained from Field/Special Research |
| `gbl` | Obtained from Go Battle League |
| `rocket` | Caught from a Team Rocket encounter |
| `snapshot` | Encountered via GO Snapshot |
| `candyxl` | Eligible to receive XL Candy (level 31+) |

---

## Age & Time

| Syntax | Meaning |
|--------|---------|
| `age0` | Caught today |
| `age1-7` | Caught 1–7 days ago |
| `year2024` | Caught in 2024 |

---

## Complex Examples

```
# Pokémon for Great League that are shiny or lucky:
cp-1500&(shiny,lucky)

# Perfect IV or shadow Pokémon, excluding defenders:
(4*,shadow)&!defender

# Ralts family for evolving (enough candy, not mega, not already max evolved):
ralts,kirlia,gardevoir&evolve&!mega

# Your design example — complex nested logic:
((1*&4*),(!(3*)&shiny))
```
