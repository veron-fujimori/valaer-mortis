# Valaer Mortis

Valaer Mortis is a Java strategy game you play in the terminal. Upgrade your town hall and barracks, train troops, and send them to mine resources or fight creatures. Training and missions take time, so you can check their progress from the main menu.

## Run locally

With Java 11 or later, run this from the repository root.

```sh
java -cp "jar/*" valaermortis.Main
```

The command opens the login and registration menu using the bundled game and JDBC driver JARs. To play, supply a MySQL schema for the `valaer_mortis` database. The connection settings are in [DB.java](src/valaermortis/util/DB.java), and the table queries are in [the DAO classes](src/valaermortis/dao/).

The Java source is in [src/valaermortis](src/valaermortis/), and the terminal menus are in [Menu.java](src/valaermortis/cli/Menu.java).
