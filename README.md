# TownyProvinces

## :information_source: Overview
- :world_map: An add-on plugin for *Towny*, which makes claiming more organized, & contains a Fast-Transport system.
  
## :gift: Features
- :globe_with_meridians: **Divides The Map Automatically, Into Multiple 1-Town-Only Provinces.**
- :moneybag: **Applies Different Town Costs Depending On Province location, size, and the type of terrain inside.**
- :bullettrain_front: **Installs with an inbuilt Fast-Transport system.**

## :hammer_and_wrench: Solves
- :money_with_wings: ***Overclaiming***: Each town has a reserved claiming area; No need to throw away money on overclaiming.
- :no_entry_sign: ***Claim Blocking***: No town can block the claiming plans of another town.
- :snake: ***Snake Claiming***: Snake claiming is irrelevant.
- :doughnut: ***Doughnut Claiming***: Doughnut claiming is irrelevant.
- :hamburger: ***Town Surrounding***: A town cannot surrounded itself with another town (*e.g. to become un-attackable*).
- :no_pedestrians: ***Travel Blocking***: Travel cannot be blocked by claims, because province borders are always wilderness.
- :tent: ***Inactive towns in popular areas***: Upkeep can be raised in popular regions, discouraging inactive towns.
- :merman: ***Sea Peoples***: Founding towns in the open sea can be prevented, if a server so chooses.
- :santa: ***Hermits***: Upkeep can be lowered in unpopular regions, supporting players with isolationist styles of play.
- :railway_track: ***Transport***:
    - Drives the emergence of a hub-node pattern of towns on the map.
    - Allows every town to become a Fast-Travel destination, if it so chooses.
    - Supports mayors who wish to detach their transport system from their politics.
    - Encourages the building of roads on the map.
    
## :eye: Map View
- ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/b0778012-7023-4372-b599-b5de6a336d4f)

## :floppy_disk: Installation Guide
1. Ensure your server has *Towny 0.99.1.0* or newer.
2. If possible, ensure your server has *Dynmap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Stop your server.
5. Start your server with plenty of memory (*especially for big maps*).
   - Example: With Spigot you might run: `java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot-1.19.4.jar nogui`.

## :football: Player Guide
- :world_map: Provinces
  - Borders are shown on the dynmap (Borders layer).
  - Town Costs are shown on the dynmap (Town Costs layer).
  - There can only be one town per province.
  - Each province has its own "New Town Cost", which applies when creating a new town in the province.
  - Each province has its own "Upkeep Town Cost", which applies each day to the town in the province.

- :fast_forward: Fast Transport:
  ### :ship: Port
    - Cost: $50, Upkeep: $5, must be on Ocean, Beach, or River biome.
    - Fast-Travel Range: 3000 blocks.
    - As a mayor, run `/plot set port` to convert one of your plots to a Port.
    - Pick a destination town, and create a **Fast-Travel Sign** at your Port, aimed at that town:![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/541b2b53-6501-401c-a46a-1bb493ed6e27)
    - Get the mayor of the destination Port, to create a "Return" sign at their Port: ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/91f52a8e-b027-4441-9d25-895d64a9ac02)
    - Now simply right-click the signs to travel from one town to the other.
  ### :rocket: Jump-Node
    - Cost: $200, Upkeep: $20.
    - Fast-Travel Range: Unlimited.
    - As a mayor, run `/plot set jump-node` to convert one of your plots to a Jump-Node.
    - Pick a destination town, and create a **Fast-Travel Sign** at your Jump-Node, aimed at that town:![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/541b2b53-6501-401c-a46a-1bb493ed6e27)
    - Get the mayor of the destination Jump-Node, to create a "Return" sign at their Jump-Node: ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/91f52a8e-b027-4441-9d25-895d64a9ac02)
    - Now simply right-click the signs to travel from one town to the other.

## :keyboard: Admin Commands *(best run from console)*
- `tpra region [regenerate] [<Region Name>]` -> Regenerate a region.
- `tpra region [newtowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk new-town-cost for a region.
- `tpra region [upkeeptowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk upkeep-town-cost for a region.
- `tpra landvalidationjob [status|start|stop|restart|pause]` -> Control the land validation job.
  - This Job does 2 things:
    - 1. Assesses and records the Biome proportions in each province. These proportions affect the new/upkeep prices.
    - 2. Assigns each province as either "land" or "Sea", depending on the biome results.
  - *NOTE: The automatic validation is not perfect, so expect to convert a few provinces afterwards using the below command.* 
- `tpra province [sea|land] [<x>,<z>]` -> Set a province to sea/land.
  - Sea provinces cannot be settled.
 
## :fast_forward: Quick-Start Guide
1. Run `tpra region regenerate all`. This will generate 2 small sample regions.
2. Run `tpra landvalidationjob start`. This will automatically identify the biome constituents in the province, and adjust prices accordingly. If the province is found to be all-water, the job will change it to a sea-province. Expect this to take a while, you can adjust the milliseconds before lookup in `config.yml`.
3. To see the generated provinces, view your dynmap. 

## :eight_spoked_asterisk: Region Definitions Guide
1. Configure as many region definition files as you want, in /region_definitions.
2. Region definition files are evaluated in alpha-numeric order.
3. The first region definition file should be the size of the entire map.
4. To fully understand how to configure your region definition files, you must understand how provinces are generated:
   * **STEP 1:** "Claim Brushes" are created and placed in the given region
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/1770c063-8cc2-453e-9b91-e169fd0bb5d5)
   * **STEP 2:** Each claim brush then moves in a random direction
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/ad00cc6e-573f-421e-80b7-0e8430e4065e)
   * **STEP 3**: Each claim brush then moves a few more times
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/ee7871f0-6c55-4050-beb7-10dd69b45306)
   * **STEP 4**: The gaps between each province are filled in, except for a 1 chunk border
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/570039a4-7a5b-4280-ad32-debc0f1110db)

## :scroll: Credits
- *TownyProvinces* was developed by Goosius in Summer 2023.
- Special thanks to Valoria Earth, who were very supportive and helpful during the development of the plugin.
- And many many thanks to Llama, for adopting *TownyProvinces* in the *Towny* family.

