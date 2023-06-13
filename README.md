# Towny Resources
- An add-on plugin for Towny, which makes town claiming more organized, reducing staff workload and server toxicity.

## :information_source: Features:
- :world_map: **Divides The Map Automatically, Into Multiple 1-Town-Only Provinces:**
  - Solves:
    - :money_with_wings: ***Overclaiming***: Each town has its own reserved area for claiming; There is no need to throw away money on overclaiming.
    - :stop_sign: ***Claim Blocking***: No town can block the claiming plans of another town.
    - :snake: ***Snake Claiming***: Snake claiming is irrelevant.
    - :doughnut: ***Doughnot Claiming***: Doughnut claiming is irrelevant.
    - :railway_track: ***Road Claiming***: Two adjacent towns can easily link up by roads/railways without anyone interfering.
  - Dynmap: ![image](https://github.com/Goosius1/TownyProvinces/assets/50219223/9eb5849a-4540-49ba-b71f-26c128c3fc56)

- :moneybag: **Applies Town Costs Depending On Province Location:**
  - Solves:
    - :tophat: ***Absentee mayors in popular areas***: Town upkeep can be raised in popular areas of the map, without being raised in other areas.
    - :santa: ***Hermits***: Town costs can be set to low/zero in unpopular/harsh areas of the map, to support players with isolationist styles of play.
  - Dynmap: *("Town Costs" Layer)* ![image](https://github.com/Goosius1/TownyProvinces/assets/50219223/044b7c32-71a8-49a3-a0fb-59f1e3af7a3e)

## :floppy_disk: Installation Guide:
1. Ensure your server has *Towny 0.99.1.0* or newer.
2. If at all possible, ensure your server has *Dynmap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Stop your server
5. Ensure your server with plenty of memory (*especially for big maps*)
   - Example: With Spigot you might run: `java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot-1.19.4.jar nogui`.
6. In the console or in-game, run `tpra region regenerate all`. 
   - This will generate 2 small sample regions.

## :book: Admin Guide:
1. Configure the region definition files you want, in /region_definitions.
   - Typically a server might have one region definition file for each continent, allowing province density and town-costs to be different for each continent.
   - The 1st region definition file should be the size of the entire map.
   - Region definition files are evaluated in alpha-numeric order.
   - You can have as many region definition files as you want.
   - Two sample region definiton files are provided.
2. When you have your region definition files configured, run `/tpra region regenerate all`.
   - This will regenerate all regions.
3. After regenerating all regions, run `/tpra landvalidationjob start`.
   - This will convert every 'mostly ocean biome' province to a be a "Sea Province". 
   - Sea provinces cannot be settled.
   - *Note: The automatic validation is not perfect, so expect to convert a few provinces afterwards using `/tpra province [sea|land] [<x>,<z>]`* 
   
## :keyboard: Admin Commands:
- `/tpra region [regenerate] [<Region Name>]`: Regenerate a region.
- `/tpra region [newtowncost] [<Region Name>] [amount]`: Set the new town cost for a region.
- `/tpra region [upkeeptowncost] [<Region Name>] [amount]`: Set the upkeep town cost for a region.
- `/tpra landvalidationjob [status|start|stop|restart|pause]`: Control the land validation job
- `/tpra province [sea|land] [<x>,<z>]`: Set a province to sea/land.
