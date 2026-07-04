# ValorantCraft

A Fabric mod that brings Valorant-style tactical shooter mechanics into Minecraft: Java Edition —
hitscan weapons with recoil and reload, agent abilities on Q/E/C/Ultimate, spike plant/defuse, and a
full round-based competitive match with buy phases and an economy.

Targets **Minecraft 1.21.1** with **Fabric Loader** and **Fabric API**.

## Building / running

Requires JDK 21 and Gradle 8.10+ (no wrapper is checked in yet — run `gradle wrapper` once if you'd
like one, or use a local Gradle install).

```
gradle build        # produces build/libs/valorantcraft-<version>.jar
gradle runClient     # launch a dev client with the mod loaded
gradle runServer     # launch a dev dedicated server
```

Drop the built jar (and Fabric API) into a normal Fabric server/client's `mods/` folder to use it
outside the dev environment.

## Controls

| Action           | Default key       |
|------------------|--------------------|
| Fire             | Left mouse button  |
| Reload           | R                   |
| Ability (Q slot) | Q                   |
| Ability (E slot) | X *(E is Minecraft's inventory key, so the E-slot ability is bound to X by default)* |
| Ability (C slot) | C                   |
| Ultimate         | Z                   |
| Open buy menu    | B                   |

All bindings are rebindable in **Options > Controls > ValorantCraft**.

## Running a match

1. Get players on the server, then as an op run `/valorant start`. Players already connected are
   split evenly into Attackers/Defenders.
2. Define at least one bomb site so the spike can be planted:
   `/valorant site add <name> <x1> <y1> <z1> <x2> <y2> <z2>` (opposite corners of a cuboid region).
3. During the **buy phase** press `B` to open the shop and purchase weapons with match credits.
4. During the **round**, Attackers carry the Spike (auto-given if no one on the team has it) and can
   plant it by holding right-click while standing inside a bomb site. Defenders can defuse by holding
   right-click (the "use" key) anywhere within range of the planted spike.
5. `/valorant stop` ends the match at any time.

Rounds are won by elimination, spike detonation, spike defusal, or the round timer expiring. First
team to `MatchManager.ROUNDS_TO_WIN` (4 by default) wins the match; teams do not currently swap sides
at halftime — see Known limitations.

## What's implemented

- **Weapons** (`com.valorantcraft.weapon`): six weapons (Classic, Sheriff, Spectre, Vandal, Phantom,
  Operator) defined as data (`WeaponType`), server-authoritative hitscan raycasting with headshot/
  bodyshot/legshot multipliers, per-weapon recoil patterns, magazine + reserve ammo tracked in each
  stack's NBT, and reload timing. Adding a new gun is just a new `WeaponType` entry plus an `Item`
  registration in `ModItems`.
- **Agent abilities** (`com.valorantcraft.ability`): a slot-based `Ability` framework (Q/E/C/Ultimate)
  with per-round charges and an ultimate-point economy (gained over time and on kills). One full
  original agent kit ("Ranger") is implemented: a flash, a smoke wall (real, sightline-blocking
  blocks), a recon reveal, and a speed/resistance ultimate. More agents register the same way as
  `ModAgents.RANGER`.
- **Spike plant/defuse** (`com.valorantcraft.spike`, `MatchManager`): hold-to-plant inside a defined
  bomb site, a fuse timer, hold-to-defuse with progress that resets if interrupted, and detonation/
  defusal round-end handling. The planted spike is rendered as a floating item display (no custom
  block/model assets needed).
- **Round-based match** (`com.valorantcraft.match`): buy/round/end-round phases, attacker/defender
  teams, a credits economy (win/loss/plant bonuses, kill rewards), and elimination/timeout/spike win
  conditions, driven by `/valorant start|stop` and synced to clients over a dedicated network payload.
- **HUD + shop** (`com.valorantcraft.client`): ammo counter, round phase/timer, spike fuse countdown,
  score, credits, and team, plus a lightweight in-game buy menu (`B`) that talks directly to the
  server — no vanilla inventory GUI involved.

## Known limitations / good next steps

Given the scope of "build Valorant," this is a solid vertical slice rather than a complete port.
Notable simplifications, in rough priority order for follow-up work:

- **Placeholder art**: weapons/spike reuse vanilla item icons (crossbow, TNT, etc.) and vanilla sounds
  (crossbow shoot, explosion) — see `ModWeapons`/model JSONs under
  `assets/valorantcraft/models/item`. Swap in real textures + custom sound files when you have them.
  No custom weapon-in-hand 3D model/animation (viewmodel) yet either.
  Actual weapon shape is not important for the initial vertical slice; focus was on solid, extensible mechanics.
- Only **one agent** is implemented; the framework supports more (`ModAgents`), but you'll want to
  write additional `Ability` implementations and a way for players to pick an agent (currently
  everyone gets "Ranger").
- **No agent selection / loadout persistence** — add an agent-select screen/command if you want more
  than one playable kit.
- **No side swap at halftime** and no overtime rules — `MatchManager.advancePhase` just keeps buying
  into rounds until a team hits `ROUNDS_TO_WIN`.
- **No armor/shield economy** — only weapons are purchasable right now; add a shield item + price
  tiers to the shop the same way weapons are wired.
- **Bomb sites are in-memory only** (`MatchManager.bombSites`), lost on server restart. Persist them
  with a `PersistentState` if you want them to survive restarts.
- **Flash/smoke/recon abilities are instant-effect at a raycast point**, not physically thrown/arced
  projectiles like real Valorant abilities — a follow-up could add real projectile entities.
- **Explosion damage (spike detonation) is a flat damage check in a radius**, not a real
  `World#createExplosion`, specifically to avoid block griefing on whatever map you build.
