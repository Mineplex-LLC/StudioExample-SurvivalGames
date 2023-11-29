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

Text

## Features

Explain in bullet points what is implemented atm

## Requirements

- Java 17+
- Mineplex SDK (Not available during closed alpha)
- Mineplex Studio Client (Not available during closed alpha)

## Getting Started

Run the following command on your local environment:

```shell
git clone --depth=1 https://github.com/Mineplex-LLC/Studio-Example-SurvivalGames.git project-name
cd project-name
mvn clean install -U -s settings.xml
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
│   ├── internationalization        # Localization files (ADD FILE PATTERN)
│   ├── mixin                       # Mixin jar location
│   └── world-templates             # World templates
├── config
│   ├── purchases                   # #ALEX-FILL-THIS
│   ├── subscriptions               # #ALEX-FILL-THIS
│   └── game-properties.yaml        # Game configuration
├── src/main/java/com/mineplex/studio/example/survivalgames
│   ├── game                        # Game specific logic
│   ├── modules                     # Module specific logic
│   ├── SurvivalGamesI18nText.java  # Internationalization wrapper
│   └── SurvivalGamesPlugin.java    # Plugin entry point
├── pom.xml                         # Maven configuration
└── .gitignore                      # GIT ignore configuration
```

## License

ADD LICENSE MESSAGE AND LICENSE ITSELF TO THE PROJECT
