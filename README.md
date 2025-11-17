# TownyProvinces

## :information_source: Overview
- :globe_with_meridians: An add-on plugin for *Towny*, which automatically divides the game-world into **Provinces**.
- :european_castle: Each Province can have just 1 town.
- :cake: Thus, every town gets a fair slice of land, and player conflicts caused by "overclaiming" are reduced.

## :gift: Features
- :tophat: Civilised Provinces: 1 town only, no foreign outposts allowed.
- :ocean: Sea Provinces: No towns, outposts allowed
- :desert: Wasteland Provinces: No towns, outposts allowed

## :hammer_and_wrench: Solves
- :money_with_wings: ***Overclaiming***: Each town has a reserved claiming area; No need to throw away money on overclaiming.
- :no_entry_sign: ***Claim Blocking***: No town can block the claiming plans of another town.
- :snake: ***Snake Claiming***: Snake claiming is irrelevant.
- :hamburger: ***Town Surrounding***: A town cannot surrounded itself with another town (*e.g. to become un-attackable*).
- :no_pedestrians: ***Travel Blocking***: Travel cannot be blocked by claims, because province borders are wilderness.
- :tent: ***Inactive towns in popular areas***: Upkeep can be raised in popular regions, discouraging inactive towns.
- :merman: ***Sea Peoples***: Founding towns in the open sea can be prevented, if a server so chooses.
- :santa: ***Hermits***: Upkeep can be lowered in unpopular regions, supporting players with isolationist styles of play.

## :eye: Map View
- ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/7570eb88-6ea9-487a-9045-2c85710ecc76)

## :floppy_disk: Installation Guide
1. Ensure your server has *Towny 0.102.0.0* or newer.
2. Ensure your server has a map-display plugin: Either *Dynmap*, *Pl3xMap*, *BlueMap* or *squaremap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Stop your server.
5. Start your server with plenty of memory (*especially for big maps*).
   - Example: With Spigot you might run: `java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot-1.20.4.jar nogui`.

## :football: Player Guide
- :cityscape: Towns:
  - 1 Town can be active in each Civilized province.
  - Each civilized province has a "New Town Cost", which applies when creating a new town.
  - Each civilized province has an "Upkeep Town Cost", which applies to existing towns.
- :tent: Outposts:
  - Outposts can be placed in Sea and Wasteland provinces.
  - Outposts can be expanded, however each town can have a maximum of 8 townblocks per foreign province.

## :fast_forward: Admin Quick-Start Guide
1. Run `tpra region regenerate all`. This will generate 2 small sample regions.
2. To see the generated provinces, view your website-map.

## :arrow_forward: Admin Guide
1. Configure Regions
   - Configure as many region files as you want, in /region_definitions.
   - Region definition files are evaluated in alpha-numeric order.
   - The first region definition file should be a "catch-all", the size of the entire map.
2. Protect Historical Town Locations
   - If you are running a "Historical" map (e.g. "Earth"), make sure to protect important historical town locations, to ensure border-lines don't cut throught them.
   - To do this, add the co-ordinates of those locations to your region def files. (for an example, see the automatically generated "Europe.yml".
   - It may help to ask players which locations they want protected, to ensure you don't forget any, and to get them involved in using the plugin.
3. Generate Provinces
   - Run '/tpra region regenerate all' to regenerate all the regions you have specified.
   - After the Regeneration Job is complete, run `/tpra landvalidationjob start`. This will automatically identify the biome constituents in the province, then will adjust the province type and prices accordingly. Expect this to take a few minutes; you can adjust the milliseconds before lookup in `config.yml`.
   - After the Land Validation Job runs, expect to tweak a few provinces for type, and a few regions for pricing.
4. Be aware of known technical issues:
  - The Hexagonal dynmap view does not look right. A fix is ticketed.
  - When making a new town, the 'confirmation' message does not show the correct amount. But the correct amount is charged.
  - On very large maps (e.g. 1:500). TownyProvinces tends to "sit" on lots of memory. A fix is in development.
  
## :keyboard: Admin Commands *(best run from console)*
- `tpra region [regenerate] [<Region Name>]` -> Regenerate a region.
- `tpra landvalidationjob [status|start|stop|restart|pause]` -> Control the land validation job.
  - This Job assigns a type to each provinces, either Civilized, Sea, or Wasteland. It also Assesses and records the Biome proportions in each province. These proportions affect the new/upkeep prices.
  - *NOTE: The automatic validation is not perfect, so expect to convert a few provinces afterwards using the below commands.*
- `tpra province settype [civilized|sea|wasteland] [<x>,<z>]` -> Set the type of a province.
- `tpra province settype [civilized|sea|wasteland] [<x1>,<z1>] [<2x>,<z2>]` -> Set the type of all provinces in a rectangular area.
- `tpra region [newtowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk new-town-cost for a region.
- `tpra region [upkeeptowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk upkeep-town-cost for a region.
- `tpra reload` -> Reload Config and Language files, and refresh map.

## :brain: Advanced Guide to Region Definition Files
Here is a technical explanation of how provinces are generated from region definition files:
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

