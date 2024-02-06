<br/>
<p align="center">
  <h3 align="center">Survival Games - Studio Example</h3>

  <p align="center">
    <a href="https://studio.mineplex.com/docs">Studio Docs</a>
    .
    <a href="https://discord.gg/mineplex">Discord</a>
  </p>
</p>

## About The Project

This project is a basic Survival Games implementation for the Mineplex Studio platform with the purpose
of showcasing the capabilities of the Mineplex Studio environment.

Access to the close alpha is required to use the Mineplex SDK and Studio Client.
This project is not intended to be used as a standalone game,
but rather as a reference for developers to understand how to use the Mineplex SDK and Studio Client to
create games for the Mineplex Studio platform.

## Features

* Mechanics
    * Chest loot mechanic with different tiers and respawn times
    * Simple border mechanic that shrinks over time
    * Tracking compass that can be found in chests that points to the nearest player
    * Healing soups that can be found in the chests and heal the player for consumption
    * Damage glow mechanic that applies a glow effect to players that have taken damage


* Abilities
    * Invisibility ability that allows the player to become invisible for a short period of time
    * Weightless ability that allows the player to negate fall damage


* Modules
    * Custom chat renderer with player stats and custom prefixes (/prefix)
    * Demo world module that showcases the capabilities of the persistence module (/demoworld)

## Requirements

- Java 21+
- Mineplex SDK (Not available during closed alpha)
- Mineplex Studio Client (Not available during closed alpha)

## Getting Started

Run the following command on your local environment:

```shell
git clone --depth=1 https://github.com/Mineplex-LLC/Studio-Example-SurvivalGames.git project-name
cd project-name
gradlew clean buildPluginJar
```

Then, you need to search for `#REPLACE-ME` and follow those instructions.

Finally, follow the studio client instructions on how to start your dev server.

## Project Structure

```shell
.
├── README.md                       # README file
├── .github                         # GitHub folder
├── assets
│   ├── configs                     # Game specific configurations (Currently used for loot tables)
│   ├── internationalization        # Localization files
│   ├── mixin                       # Mixin jar location
│   └── world-templates             # World templates
├── config
│   ├── purchases                   # Purchasable item configurations
│   ├── subscriptions               # Subscription item configurations
│   └── game-properties.yaml        # Game configuration
├── src/main/java/com/mineplex/studio/example/survivalgames
│   ├── game                        # Game specific logic
│   ├── modules                     # Module specific logic
│   ├── SurvivalGamesI18nText.java  # Internationalization wrapper
│   └── SurvivalGamesPlugin.java    # Plugin entry point
├── build.gradle.kts                # Gradle build configuration
├── settings.gradle.kts             # Gradle settings file
└── .gitignore                      # GIT ignore configuration
```

## License

SOFTWARE USAGE LICENSE AGREEMENT

This Software Usage License Agreement (the "Agreement") is entered into between Mineplex Studios LLC ("Licensor"), and
the user of the software ("Licensee").

1. Definitions

1.1. "Software" refers to the Studio-Example-SurvivalGames repository provided by Licensor, including all associated
files, documentation, and any updates or modifications.

2. License Grant

2.1. Subject to the terms and conditions of this Agreement, Licensor hereby grants Licensee a non-exclusive,
non-transferable, revocable license to use the Software for the purposes outlined in this Agreement.

3. Permitted Use

3.1. Licensee may use the Software solely for the purpose of developing games on the Mineplex Studio.

4. Restrictions

4.1. Licensee may not:
a) Sell, rent, lease, sublicense, or otherwise transfer the Software to any third party.
b) Remove or alter any copyright, trademark, or other proprietary notices from the Software.

5. Ownership

5.1. Licensor retains all right, title, and interest in and to the Software, including all intellectual property rights.

6. Support and Updates

6.1. Licensor may provide support and updates for the Software at its discretion.

7. Term and Termination

7.1. This Agreement is effective upon Licensee's acceptance and shall continue until terminated.
7.2. Licensor may terminate this Agreement at any time for breach of its terms.
7.3. Upon termination, Licensee shall cease all use of the Software and destroy all copies in their possession.

8. Warranty and Disclaimer

8.1. The Software is provided "as is," without warranties of any kind, whether express or implied.
8.2. Licensor does not warrant that the Software will be error-free or meet Licensee's requirements.

9. Limitation of Liability

9.1. Licensor shall not be liable for any indirect, incidental, special, or consequential damages arising out of or in
connection with the Software.

10. Governing Law

10.1. This Agreement shall be governed by and construed in accordance with the laws of Texas.

11. Entire Agreement

11.1. This Agreement constitutes the entire agreement between the parties concerning the Software and supersedes all
prior agreements and understandings.

By accepting this Agreement, Licensee acknowledges that they have read, understood, and agree to be bound by its terms.

Mineplex Studios LLC
support@mineplex.com
