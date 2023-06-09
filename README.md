# Towny Resources
- An add-on plugin for Towny, which makes town claiming more organised/predictable, reducing staff workload and server toxicity.

## Features:
- :world_map: **Automatically Divides The Map Into Provinces:**
  - Each Provinces can have just one town.
  - Province density can be configured to be different in different areas of the map.
  - Province borders can be viewed on the "TownyProvinces - Borders" layer of the map:
  - PLACEHOLDER FOR SCREENGRAB
  
- :moneybag: **Provinces can have different Settlement/Upkeep Costs**
  - Each province has a "Province Settlement Price" which applies to new towns, and can be configured to vary by map location.
  - Each province has a "Province Upkeep Price" which applies to town upkeep, and can be configured to vary by map location.
  - Province prices can be viewed on the "TownyProvinces - Prices" layer of the map.
  - PLACEHOLDER FOR SCREENGRAB

## Admin Guide:
  
  
  
## Player Guide:


- 
  -   new towns to be different in different regions of the map.
  - Each region has its own Town Upkeep price, allowing town upkeep to cost more in different regions of the map.
  - 
  - Try it and see!

- :unlock: **Unlock Combat for Regular Players**

  - :guard: :guard: :guard: **Battlefield Roles**
    - Each player can type ```/tcm changerole``` to choose a Battlefield Role, giving them unique advantages & disadvantages:<br><br>
      - Light Infantry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/60b7232a-94c6-420f-8424-7bd236a37f91)
      - Light Cavalry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/2c91037a-7aa1-4e05-b555-8b4480996b1b)
      - Medium Infantry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/c87a2883-7707-4c97-a8db-7fa6df12328b)
      - Medium Cavalry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/a75ff032-2e3d-4186-b873-66d57e2a781c)
      - Heavy Infantry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/8fc4e848-f0de-4d8a-bd8f-00095cdd7a70)
      - Heavy Cavalry<br>
        ![image](https://github.com/TownyAdvanced/TownyCombat/assets/50219223/024478f5-631b-44df-b3b2-3474a9ec4d08) 
      <br>
    - *With this feature, players do not require excessively expensive armor and weapons to be competitive during battles.*
  
  - :sparkling_heart: :sparkling_heart: :sparkling_heart:  **Potion Transmuter**
    - Potions of Healing are automatically Transmuted into Potion of Regeneration.
    - The healing is +50% of the source potion, but delivered over 15 seconds rather than instantly.
    - *With this feature, players do not require elite-level inventory management skills to manage their healing during battles.*

- :horse: :star: **Cavalry Enhancements:**
  - Overview:
    - Mounted Horses (*aka cavalry*) play the role of "Tanks" on the battlefield: tough + mobile + powerful shot. 
  - Special Abilities:
    - Cavalry Strength Bonus: +3 PVP Strength Bonus for 1 hit. Cooldown for 10 seconds.
    - Missile Shield: Both horse and rider are immune to arrows fired from bows (but not crossbows).
    - Resistance to PVP Attack Damage: +70% .
    - Do not rear up on taking damage.
    - Teleport with player on /n or /t spawn.
  - Special Vulnerabilities:
    - Take +9 damage when hit by a spear in PVP.

- :crossed_swords: :new: **New Weapon**
  - Spear<br>
    ![image](https://user-images.githubusercontent.com/50219223/236872422-90922285-a49e-497a-9528-97a4581ca6db.png)    
  - *Note: The spear bonus can also be given to custom-model weapons. See config file for mode details.*

- :bust_in_silhouette: :footprints: **Tactical Invisibilty:**
  - Use stealth tactics by going invisible on the dynmap.
  - This feature allows players to use many stealth tactics, in exchange for showing less activity on the dynmap.
  - There are 2 Modes:
    - Automatic: You disappear from the map when in the wilderness.
    - Triggered: You disappear from the map when holding a certain combination of items in your hands (*e.g. double diamond swords*).  

- :coffin: :moneybag: **Keep Inventory on Death:**
  - If you die within 400 blocks of a town-homeblock, you keep your inventory and levels.
  - Any tools/weapons/armour in your inventory are subject to a 5% degrade.

## Admin Commands:
- ```/tcma reload```: Reload config and language settings.

## Player Commands:
- ```/resident```: View your Battlefield Role.
- ```/tcm changerole```: Change your Battlefield Role.

## Installation:
1. Download the latest ***TownyCombat.jar*** from [here](https://github.com/TownyAdvanced/TownyCombat/releases).
2. Drop the jar into your plugins folder.
3. Restart your server.
4. Edit the file /TownyCombat/config.yml, and configure (***By Default, all features will be configured to off***).
5. Restart your server.

