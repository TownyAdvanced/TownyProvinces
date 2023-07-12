# TownyProvinces

## :information_source: Overview
- :world_map: An add-on plugin for *Towny*, which makes claiming more organized, reducing staff workload and server toxicity.
  
## :gift: Features
- :globe_with_meridians: **Divides The Map Automatically into Provinces:**
  - :tophat: Civilised Provinces: 1 town only, no foreign outposts allowed.
  - :ocean: Sea Provinces: No towns, outposts allowed
  - :desert: Wasteland Provinces: No towns, outposts allowed

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

## :eye: Map View
- ![image](https://github.com/TownyAdvanced/TownyProvinces/assets/50219223/b0778012-7023-4372-b599-b5de6a336d4f)

## :floppy_disk: Installation Guide
1. Ensure your server has *Towny 0.99.1.0* or newer.
2. Ensure your server has a map-display plugin: Either *Pl3xmap* or *Dynmap*.
3. Download the *TownyProvinces* plugin jar file from [here](https://github.com/TownyAdvanced/TownyProvinces/releases), and drop it into your server plugins folder.
4. Stop your server.
5. Start your server with plenty of memory (*especially for big maps*).
   - Example: With Spigot you might run: `java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot-1.19.4.jar nogui`.

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

## :arrow_forward: Admin Full Guide
1. Prepare Playerbase
   - The main benefit of TownyProvinces is for you, the server owner/staff, because it will reduce both your workload relating to claiming/overclaiming, and the toxicity you have to deal with on your server.
   - If you have a mature playerbase, they will easily understand this, however if you have a lot of younger players, you can expect them to struggle to understand why the benefit of improving your life is worth the cost of slightly curtailing their freedoms.
   - Thus take care to keep players informed and bring them along, or you may face revolt!
2. Protect Historical Locations
   - If you are running a "Historical" map (e.g. "Earth"), make sure to protect important historical locations, to ensure border-lines don't run throught them.
   - Do this by including the co-ordinates of those locations in your region def files. (for an example, see the automatically generate "Europe.yml".
   - It may be approporiate to ask players which locations they want protected, to help ensure you don't forget any.
3. Be aware of known technical issues:
    - TownyProvinces does not yet support multiple Worlds (*e.g. you can't have provinces in the Nether*). A fix is ticketed.
    - The Hexagonal dynmap view does not look right. A fix is ticketed.
    - On very large maps. TownyProvinces tends to "sit" on lots of memory. A fix is in development.
    - On provinces bordering 2 regions, the price can sometimes be too high. A fix is in development. As a workaround, edit the affected province files then run `tpra reload`.
4. Configure
   - Configure as many region definition files as you want, in /region_definitions.
   - Region definition files are evaluated in alpha-numeric order.
   - The first region definition file should be the size of the entire map.
   - After you have generated your regions, run `tpra landvalidationjob start`. This will automatically identify the biome constituents in the province, and adjust prices accordingly. It will also set each province type, as either Civilized, Sea, or Wasteland . Expect this to take a while; you can adjust the milliseconds before lookup in `config.yml`.
   - After the Land Validation Job runs, expect to tweak a few provinces for type, and a few regions for pricing.
## :keyboard: Admin Commands *(best run from console)*
- `tpra region [regenerate] [<Region Name>]` -> Regenerate a region.
- `tpra region [newtowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk new-town-cost for a region.
- `tpra region [upkeeptowncostperchunk] [<Region Name>] [amount]` -> Set the per-chunk upkeep-town-cost for a region.
- `tpra landvalidationjob [status|start|stop|restart|pause]` -> Control the land validation job.
  - This Job assigns a type to each provinces, either Civilized, Sea, or Wasteland. It also Assesses and records the Biome proportions in each province. These proportions affect the new/upkeep prices.
  - *NOTE: The automatic validation is not perfect, so expect to convert a few provinces afterwards using the below command.* 
- `tpra province settype [civilized|sea|wasteland] [<x>,<z>]` -> Set the type of a province.
- `tpra province settype [civilized|sea|wasteland] [<x1>,<z1>] [<2x>,<z2>]` -> Set the type of all provinces in a rectangular area.

## :brain: Advanced Guide to Region Definitions
To fully understand how to configure your region definition files, you must understand how provinces are generated:
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

