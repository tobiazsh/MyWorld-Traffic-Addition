# MyWorld Traffic Addition

### **MyWorld Traffic Addition is still in development. Many features follow soon! <br /> <br /> Only Available for Fabric**<br><br>CURRENTLY SUPPORTED MINECRAFT VERSION: 1.21.6, 1.21.7 and 1.21.8<br>

### **Current features:**
 - Place signs and rotate
 - Apply different textures to sign
 - A variety of 4 different sign shapes (more to come)
 - Customizable signs
 - MyWorld Traffic Addition Sign Editor *
 - Sign poles
 - A few debug commands

\* **MyWorld Traffic Addition Sign Editor:** The software to edit the customizable signs and add your own textures to them;

My code may not be the most beautiful, but it works :D

### ATTENTION: Axiom is conflicting with my mod since both use ImGui but different versions of it. I don't know the solution yet.

### WARNINGS:
- **User experience is slightly restricted when used on a cracked Minecraft Account/Server. This is NOT by choice of the developer, but rather a technical issue (although you should buy the actual game since it's totally worth it!). If you want to, you can read more about it in the docs: PLACEHOLDER**

## Demonstration
![2024-12-08_17 28 39](https://github.com/user-attachments/assets/9c297936-fa8a-42ac-ac0e-db318fc98575)
![2025-01-05_21 27 48](https://github.com/user-attachments/assets/d5d62c3b-828a-4a4a-895a-b7e0d6ed76bb)
![2025-01-05_21 34 37](https://github.com/user-attachments/assets/268a0ed7-486e-4ed1-afc3-c9dbd7949985)
Example Customizable Sign

<br />

## Building
### 1. Clone the repository: `git clone https://github.com/tobiazsh/myworld_traffic_addition`
### 2. Build: `./gradlew build`

<br />

## Development
### 1. Clone the repository
### 2. (Recommended): Open Project in IntelliJ IDEA (Community Edition)
### 3. (Not Recommended) Run project using either `./gradlew runClient` for the client environment or `./gradlew runServer` for the server environment
Pros:
- no need for an IDE

Cons:
- No good debugging environment

### 4. (Recommended): Run either configuration of client or server directly in IntelliJ IDEA ([Fabric Docs](https://docs.fabricmc.net/develop/getting-started/launching-the-game#launch-profiles))
Pros:
- Proper debugging environment

Cons:
- Need for an IDE

## Fonts
There are a few fonts included in the mod that can be used for sign editing and more inside the game, so users do not have to include
fonts themselves.

**Here's what's recommended for specific countries when building roads and signs:**<br><br>
For **Spain, Mexico, USA, Portugal: *Highway Gothic*** by Tom Oetken<br>--<br>
For **Germany, Czech Republic: *DIN 1451 Mittelschrift*** by Peter Wiegel (SIL OFL)<br>--<br>
For **Austria, Slovakia: *Paneuropa*** by Peter Wiegel (SIL OFL)<br>--<br>
For **Poland: *Drogowskaz*** by Emil Wojtacki<br>--<br>
~~For **Slovenia, Croatia, Serbia, Montenegro, Bulgaria, Romania: *Gliscor Gothic*** by Tom Oetken~~ **Removed for now** ~~<br>(MODIFIED by me; I got the permission to from Tom Oetken)~~<br>--<br>
**For Your own made up country there are a few neutral fonts included: *Roboto (Mono), Funnel Sans and Deja Vu Sans* !**

You can always add more fonts yourself by just opening the .jar with a zipping program and adding the .ttf file under `assets/myworld_traffic_addition/textures/imgui/fonts/` **BUT**
if you run it on a server, both the server AND the client have to have the modified version installed and if you share your world, the other user also has to have the same mod file as you do!

I try to add a good selection of fonts that fit for many countries, but I can't include every single one, because 1) It would take too much space and 2) I cannot obtain a legal copy for my
mod for every font that's being used on road signage world wide (Italy as example; they have privated their font used in road signage, but it's similar to Arial)

## License
This project is licensed under the **Attribution-NonCommercial 4.0 International** - see the [LICENSE](LICENSE) file for details

### In short:
**You are free to:**<br>
✅ Use, modify, and share this mod for non-commercial purposes.<br>
✅ Redistribute it as long as you credit "Tobias S." or "Tobiazsh" somewhere, even in small text. (See below)*<br>
✅ Modify and distribute your own versions under the same non-commercial terms.<br>

**You may NOT:**<br>
❌ Use this mod for commercial purposes (including selling, paywalls, or monetization).<br>
❌ Redistribute it without proper credit.<br>

*While not required, I’d really appreciate it if you included a link to any of my social media when sharing or redistributing this mod. Links are on [my GitHub Profile](https://github.com/tobiazsh).

<br><br>
***Made with ❤️ in Austria***
