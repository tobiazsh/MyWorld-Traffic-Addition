# MyWorld Traffic Addition

### **MyWorld Traffic Addition is still in development. Many features follow soon! <br /> <br /> Only Available for Fabric**<br><br>NEWEST MINECRAFT VERSION: 1.21.4<br>

### **Current features:**
 - Place signs and rotate
 - Apply different textures to sign
 - A variety of 4 different sign shapes (more to come)
 - Customizable signs, although not, but almost, fully functional yet
 - MyWorld Traffic Addition Sign Editor *
 - Sign poles
 - A few debug commands

\* **MyWorld Traffic Addition Sign Editor:** The software to edit the customizable signs and add your own textures to them;

My code may not be the most beautiful, but it works :D

### ATTENTION: Axiom is conflicting with my mod since both use ImGui but different versions of it. I don't know the solution yet.

## Demonstration
![2024-12-08_17 28 39](https://github.com/user-attachments/assets/9c297936-fa8a-42ac-ac0e-db318fc98575) Example Customizable Sign

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
For **Poland: *Dragowskaz*** by Emil Wojtacki<br>--<br>
For **Slovenia, Croatia, Serbia, Montenegro, Bulgaria, Romania: *Gliscor Gothic*** by Tom Oetken<br>(MODIFIED by me; I got the permission to from Tom Oetken)<br>--<br>
**For Your own made up country there are a few neutral fonts included: *Roboto (Mono), Funnel Sans and Deja Vu Sans* !**

You can always add more fonts yourself by just opening the .jar with a zipping program and adding the .ttf file under `assets/myworld_traffic_addition/textures/imgui/fonts/` **BUT**
if you run it on a server, both the server AND the client have to have the modified version installed and if you share your world, the other user also has to have the same mod file as you do!

I try to add a good selection of fonts that fit for many countries, but I can't include every single one, because 1) It would take too much space and 2) I cannot obtain a legal copy for my
mod for every font that's being used on road signage world wide (Italy as example; they have privated their font used in road signage, but it's similar to Arial)

## License
This project is licensed under the **GNU LESSER GENERAL PUBLIC LICENSE v3.0** - see the [LICENSE](LICENSE) file for details

### In short:
**YOU MAY:**
- Use the software for any purpose, including commercial use
- Modify the software to suit your needs
- Distribute the software (either original or modified) to others
- Link the software to proprietary software, as long as you follow the rules of the license
- Combine LGPL software with other code without making the entire program open-source

**BUT, YOU MUST:**
- Release the modified source code if you change the software, and make it available to others
- Provide a copy of the LGPL license when distributing the software (modified or not)
- Allow users to replace or modify the LGPL library if you're using it in your program (especially if you statically link it)
- Share the source code or provide a way for users to replace the LGPL library with a modified version
- Follow the LGPL rules for the LGPL part of your software, even if you combine it with proprietary software

**YOU MAY NOT:**
- Use the software in a way that violates the license, such as not sharing modified versions when required
- Distribute the software in a way that prevents users from modifying the LGPL library, especially if it's statically linked
- Use the software and claim ownership or remove the original author’s credits

<br /><br />
***Made with ❤️ in Austria***
