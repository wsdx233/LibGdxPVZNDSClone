# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WPvz is a Plants vs. Zombies clone built with libGDX and Kotlin. The project uses a multi-module Gradle structure with KTX utilities for enhanced Kotlin support.

## Build Commands

- **Run the game**: `gradlew lwjgl3:run` or `.\gradlew.bat lwjgl3:run` (Windows)
- **Build runnable JAR**: `gradlew lwjgl3:jar` (output: `lwjgl3/build/libs`)
- **Clean build**: `gradlew clean`
- **Build all**: `gradlew build`
- **Run tests**: `gradlew test`

## Project Structure

- **core**: Shared game logic across all platforms
  - `com.pvz.vidar.game.wsdx233.top.Main`: Entry point using KtxGame
  - `screen/`: Game screens (TitleScreen, GameScreen)
  - `actor/`: Game entities (Plant, Zombie, Sun, Pea, etc.)
  - `Assets.kt`: Centralized asset loading (textures, sounds, music)
- **lwjgl3**: Desktop platform launcher using LWJGL3

## Architecture

### Screen Management
The game uses KtxGame with screen-based architecture:
- `Main.kt` initializes screens and sets the initial screen
- `TitleScreen`: Main menu
- `GameScreen`: Core gameplay with Stage-based rendering

### Actor System
Built on libGDX Scene2D actors:
- **Plant** (base class): All plants inherit from this with HP, collision boxes, and death listeners
- **Zombie** (base class): Enemy entities with row-based movement
- **LawnGroup**: Container managing the 5x9 lawn grid, plant/zombie interactions, and collision detection
- **Card/Seed**: UI elements for plant selection with cooldown management

### Game Loop (GameScreen)
1. **Preview phase**: Camera pans to show incoming zombies
2. **Ready/Set/Plant sequence**: Countdown before gameplay starts
3. **Active gameplay**:
   - Progressive zombie spawning (10s → 0.01s intervals over 300s)
   - Sky-based sun drops every 5-10s
   - Plant placement via card selection
   - Collision detection between peas and zombies, zombies and plants
4. **Final wave**: Triggered at 30s remaining with audio/visual cues
5. **Game over**: When any zombie reaches x < 50f

### Asset Management
`Assets.kt` loads all resources at startup:
- TextureAtlas for animated sprites (zombies, plants)
- Music tracks (intro, grass, choose_your_seed)
- Sound effects (plant, groan, splat, etc.)
- UI skins (craftacular, dark theme)

## Key Technical Details

- **Java version**: 21 (sourceCompatibility in build.gradle)
- **Kotlin version**: 2.2.21
- **libGDX version**: 1.14.0
- **KTX version**: 1.13.1-rc1
- **Viewport**: ExtendViewport (640x360 base resolution) for GameScreen
- **Coordinate system**: LawnGroup defines LAWN_START_X/Y and CELL_WIDTH/HEIGHT for grid positioning
- **Asset generation**: `generateAssetList` task creates assets.txt listing all asset files

## Development Notes

- The game uses reflection to instantiate plants and zombies from seed/spawn configurations
- Death listeners handle cleanup when plants/zombies are destroyed
- Screen shake effects modify camera position temporarily
- Progress bar tracks game completion (0-300s)
- Chinese comments present in some files (e.g., "绘制椭圆形阴影" = "draw elliptical shadow")