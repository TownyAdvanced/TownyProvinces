# Towny Resources

## :information_source: Overview
- :world_map: An add-on plugin for *Towny*, which makes claiming more organized, reducing staff workload and server toxicity.

## :gift: Features
- :globe_with_meridians: **Divides The Map Automatically, Into Multiple 1-Town-Only Provinces.**
- :moneybag: **Applies Different Town Costs Depending On Province Location.**

## :hammer_and_wrench: Solves
- :money_with_wings: ***Overclaiming***: Each town has its own reserved area for claiming; There is no need to throw away money on overclaiming.
- :no_entry_sign: ***Claim Blocking***: No town can block the claiming plans of another town.
- :snake: ***Snake Claiming***: Snake claiming is irrelevant.
- :doughnut: ***Doughnut Claiming***: Doughnut claiming is irrelevant.
- :railway_track: ***Road Claiming***: Two adjacent towns can easily link up by roads/railways without anyone interfering.
- :tophat: ***Absentee mayors in popular areas***: Town upkeep can be raised in popular areas of the map, without being raised in other areas.
- :santa: ***Hermits***: Town costs can be set to low/zero in unpopular/harsh areas of the map, to support players with isolationist styles of play.

## :eye: Map View
- ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/b0778012-7023-4372-b599-b5de6a336d4f)

## :floppy_disk: Installation Guide
1. Ensure your server has *Towny 0.99.1.0* or newer.
2. If possible, ensure your server has *Dynmap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Stop your server
5. Start your server with plenty of memory (*especially for big maps*)
   - Example: With Spigot you might run: `java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot-1.19.4.jar nogui`.

## :keyboard: Admin Commands *(best run from console)*
- `tpra region [regenerate] [<Region Name>]` -> Regenerate a region.
- `tpra region [newtowncost] [<Region Name>] [amount]` -> Set the new-town-cost for a region.
- `tpra region [upkeeptowncost] [<Region Name>] [amount]` -> Set the upkeep-town-cost for a region.
- `tpra landvalidationjob [status|start|stop|restart|pause]` -> Control the land validation job.
  - *NOTE: The automatic validation is not perfect, so expect to convert a few provinces afterwards using the below command.* 
- `tpra province [sea|land] [<x>,<z>]` -> Set a province to sea/land.
  - Sea provinces cannot be settled.
 
## :fast_forward: Quick-Start Guide
1. Run `tpra region regenerate all`. This will generate 2 small sample regions.
2. Run `tpra landvalidationjob start`. This will automatically start identifying sea provinces.
3. To see the generated provinces, view your dynmap. 

## :eight_spoked_asterisk: Region Definitions Guide
1. Configure as many region definition files as you want, in /region_definitions.
2. Region definition files are evaluated in alpha-numeric order.
3. The first region definition file should be the size of the entire map.
4. To fully understand how to configure your region definition files, you must understand how provinces are generated:
   * **STEP 1:** "Claim Brushes" are created and placed in the given region
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/5b438195-0253-448c-9f0a-a7e9b85195e7)
   * **STEP 2:** Each claim brush then moves in a random direction
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/458bd31b-efaa-44d9-838f-4dcc9ff7b852)
   * **STEP 3**: Each claim brush then moves a few more times
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/8bbe9379-ab47-4bd0-9038-dce299545ac0)
   * **STEP 4**: The gaps between each province are filled in, except for a 1 chunk border
     * ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/570039a4-7a5b-4280-ad32-debc0f1110db)

## :scroll: Credits
- *TownyProvinces* was developed by Goosius in Summer 2023.
- Special thanks to Valoria Earth, who were very supportive and helpful during the development of the plugin.
- And many many thanks to Llama, for adopting *TownyProvinces* in the *Towny* family.

