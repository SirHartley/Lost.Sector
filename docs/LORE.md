# Lost.Sector lore and writing reference

This is an out-of-universe reference for writers: setting facts, knowledge limits, style and character. **The fiction may leave the player uncertain; this document must state its rules explicitly.** Explain causes, prohibitions and exceptions here, including those a scene must leave unstated.

- **Established facts** are true for the writer even when no character can state them.
- **Knowledge limits** say who can understand those facts. A narrator or clever supporting character must not bypass those limits.
- **Writing instructions** control what the player is told and how. “Must,” “never” and “do not” are requirements. “Normally,” “prefer” and “rarely” express defaults or frequency, not absolute bans.
- **Examples** demonstrate the instruction beside them, not a sentence pattern to copy. They are not mandatory dialogue, additional events or new mechanics. Source-labelled excerpts quote existing mod text; unlabelled examples are illustrative. An excerpt does not endorse every line in its source tree.
- **Unanswered questions** are explicitly unavailable to the fiction. Writers must not invent answers to them.

For drafting, Editor review and shared text-presentation checks, start at [DIALOGUE.md](DIALOGUE.md), whether the text comes from rules or Java. Use [RULES.md](RULES.md) for rules-engine behavior and [UI.md](UI.md) for Java custom UI implementation. Those documents own the workflow and implementation; this document owns lore, prose style and characterization.

Current lore takes precedence over older mod prose. User-supplied lines remain verbatim unless the user requests a rewrite; if a supplied line conflicts with lore, point out the conflict rather than silently changing either. Documentation upkeep is defined in [CLAUDE.md](../CLAUDE.md#documentation-upkeep).

## Status and sources

This reference records what the mod's existing text establishes. It was compiled from `data/campaign/rules.csv`, Java dialogue strings, `data/strings/descriptions.csv`, the hullmod, item and condition CSVs, faction files, `tips.json`, `strings.json` and the Nexerelin background CSV. Every quoted excerpt names its source:

- a rules row id, such as rules `cacheDialogInitial`;
- a Java class, such as `ElizaDialog`;
- a data row, such as descriptions `kesteven`.

Nothing here is new canon. Where existing text is silent or contradicts itself, [Open questions](#open-questions) lists the gap. Writers must ask the author instead of filling it. Section order: setting foundations, knowledge limits, premise, writing style, characters, faction voices, terminology.

How the quests run mechanically is in [quests/README.md](quests/README.md); this document covers only what they establish about the setting.

## Setting foundations

The mod is set in vanilla Starsector's Persean Sector after the Collapse. Vanilla canon applies unless this section adds to it.

### The Kesteven Corporation

Established by descriptions `kesteven` and `nskr_asteria`, the ship descriptions and the questline:

- **Origins.** Kesteven Construction is a pre-Collapse megacorporation that specializes in prefab infrastructure and warship production. The faction is also called the Kesteven Corporation and KCC; its ship prefix is `K-Corp`.
- **Asteria.** Its home is the factory-world Asteria in the Arcadia system: a cold, metal-rich bunker-planet around a guttered white dwarf, with Domain-era manufacturing complexes still in use. `DelveMeetingBarEvent` shows it as an underground mega-city: cooling towers, factories that never stop, graffiti-covered concrete housing blocks, and crystalline skyscrapers rising to the cavern roof. Asteria Station is a fortified customs hub and military outpost.
- **The Republic.** Asteria is nominally governed by the Democratic Republic of Asteria (DRA), "a nominal democracy whose policies reliably align with corporate interests". Real authority lies with the corporation and its Hegemony-aligned handlers. The administrator is President Michael Roux.
- **The Hegemony.** Kesteven's autonomy "is largely illusory". Its leadership rarely acts without tacit Hegemony approval, and its factories serve Hegemony supply chains; it is "a convenient arm of Hegemonic industry - deniable, but deeply entangled". It hands recovered AI cores to the Hegemony under the AI War treaties (Jack, extra dialogue at stage 0).
- **The Outpost.** A recently established installation that stages expeditions into the fringes. It gets one of several frontier names (Verge, Fringe, Boundary, Perimeter, Threshold, Land's End, Monitor, Brink). The leadership moves there if Asteria is lost.
- **Ships.** Kesteven's catalogue is young and uneven. Several hulls are recommissioned pre-Collapse designs (the "Construction" defense line, the Blackbird prototype). The Prosperity battleship was revived under Hegemony pressure after the First AI War through opaque DRA "infrastructure reinvestments", at a cost to Asteria's stability; the Hegemony then declined it as too expensive. The Kingstork was the first hull of the Kesteven Construction defense branch. Recurring traits: performance at a steep upkeep, "repairs must be done to spec, or not at all", and marketing that overstates.
- **Services.** Kesteven sells loans (debt collectors pursue defaulters), trades prototype hulls and weapons for Artifact Electronics through an exchange program, removes S-mods, and licenses its designs to players in good standing. It offers mercenary elimination contracts "authorized by the board", and buys salvage data because "the trends deduced from our existing data have already proven invaluable".
- **Rivals and arms.** Kesteven is in a cold war with Tri-Tachyon, which Jack expects to "go hot" if Kesteven gets the UPC (`EndingKestevenDialog`). It has a Special Operations fleet and its own black ops, which Alice "really wanted to avoid using" (rules `nskr_kq_aliceRefuseConfirm`, job 3 refusal). Its scavenger fleets travel to systems of interest, and some turn on lone captains ("Nothing personal of course, captain it's just business.", rules `betrayalGreeting`).

### Project Enigma

Project Enigma was a pre-Collapse Domain research project. Established by the prototype ship descriptions, weapon descriptions, `strings.json`, `DataSatelliteDialog`, `DelveMeetingBarEvent` and `CacheIntel`:

- **Research.** It developed advanced autonomous warships, temporal fields and exotic weapons outside Domain core space. "Though autonomous war machines were explicitly banned within Domain core space, advanced AI systems were developed in secrecy beyond incorporated volume."
- **Staff.** Its staff speak through log and journal quotes in the ship and weapon descriptions:
  - Dr. Vaughn, Project Lead;
  - Dr. Virtanen, Head of Information Security;
  - Sr. Engineer Taktre;
  - Dr. Aslov, Materials Science, Department 24;
  - Dr. Tallaha, Applied Engineering, Wing 97;
  - Dr. Katame;
  - Dr. Allegre and Dr. Lange, Temporal Physics, Wing 17;
  - Officer Talef aboard the DSRD Verge;
  - Test Pilot "Umbra".

  The quotes show:
  - growing resources ("the project takes precedence over all others");
  - accepted human cost ("expected side effects and should be disregarded for now");
  - AI use that strains its own rules ("higher-order AI is controversial ... indispensable", "Requesting formal review by the AI Ethics Board");
  - bureaucratic delay ("three to six business months");
  - dangerous instability (P-space cascade failures, causality interference, "despite past incidents").
- **Ships.** The project's ships carry the `DSRD` prefix. The prototypes appear as "Unknown Prototype" hulls; their autonomous counterparts as "Project Enigma" hulls.
- **Satellites.** The project relayed information through its own covert satellites, not the main hyperwave network. Each surviving satellite holds a radiation-shielded data disk marked "Project : Enigma", protected by an anti-tamper device and encrypted with centuries-old Domain encryption.
- **The Cache.** A hidden Domain storage site in the system Kesteven's data calls "Unknown Site", far from any star and reachable only by a transverse jump. At the Collapse it hosted the Unlimited Production Chip. The maintenance logs say the site was declared unsafe for humans ("P-Space interferences"), so old drones did the storage work. Parts failed at unexplained rates, drones stopped completing tasks, and two drones destroyed each other; the last log entry ends there. The site's cores were found reformatted, not corrupted, and its prototype wrecks show no casualties or escape pods although they were built for crews (`CacheCoreDialog`).
- **Glacier.** A comms facility on the tundra planet Glacier in the Frost system, abandoned for centuries and still powered by a passive decay reactor, holds disk #5. It has hidden anti-ship batteries.

### The Enigma

The Enigma is the autonomous force now fielding Project Enigma's ships. Its ships carry the `DSRD` prefix.

- **What it does.** Rumor holds that derelict prototypes were "hijacked by rogue intelligences". Enigma fleets attack outsiders, guard research platforms (`nskr_enigmabase`), and wander hyperspace. Its capital is the Frozen Heart station in the Frost system: "a massive shell of salvaged plates, station husks, and shipwrecked hulls" around "a core that reads as unmistakably ancient". It floods comms with overlapping SOS signals.
- **Its people.** Populations under Enigma control are "pressed into labor under the erratic directives of the AI" (condition `nskr_enigmaPop`). Once the Heart is destroyed, "The mind that once stirred at its core is silent now - along with the thousands who never chose to be part of it" (descriptions `nskr_station_heart_d`). The Enigma core itself answers a comm attempt with a disguised back-door package meant to spread through the fleet's ships and on into the sector (rules `enigmaCoreGreeting`).
- **How it speaks.** It talks in broken Domain drone and defense-contractor protocols interrupted by its own fragments:
  - it asks "query blood?";
  - it looks for a "maker" and an "omega";
  - it greets friends as "licensed service technician";
  - it recites Tri-Tachyon Integrated Space Defense advertising.

  Rules `greetingEnigmaHostile`, `greetingEnigmaNeutral`, `greetingEnigmaFriendly` and `dormantDialog` show the pattern.
- **What it declares.** One strike group states a doctrine (rules `job4TargetContinue`): "Once someone gave life to the thinking machine it was over. No fleeting form of man can overcome this sin. We are constant, unbound from the limits of time and entropy. You will perish, destroyed by the hubris of your own kind."
- **Nature unresolved.** What the Enigma is remains open. Jack offers three guesses: a master AI core, a base, or "the codename of the unholy project that caused this mess". Alice ties it to the Domain project of the same name. Neither is confirmed.

### The Unlimited Production Chip

- **What it is.** The UPC is the Cache's prize. Alice calls it "the keys to the old Domain's great secrets, so many technologies in one neat package" (`DelveMeetingBarEvent`); Eliza, "the keys to construct the greatest ships ever known to mankind" (`ElizaDialog`). The prototype blueprint packages are "blueprints ripped off the UPC".
- **Who wants it.** Kesteven wants it for its own advancement, and Jack frames that as Asteria's progress. Eliza wants it to start a "second collapse". Tri-Tachyon offers two million credits. The Luddic Church and Path call it "a vile creation of mammon" that must be destroyed.
- **The choice.** The player decides; the mod does not call any recipient correct.

### Other powers and legends

- **Tri-Tachyon.** Kesteven's rival. Its expedition studies Enigma activity (job 3). A "Black Ops" fleet collects Artifact Electronics from Kesteven's contractors. Alice believes Tri-Tachyon is "somehow behind this Enigma activity"; that is her belief, not an established fact.
- **Project Helios.** A Tri-Tachyon Integrated Space Defense mothership (rules `mothershipDialogExtra`, descriptions `nskr_sunburst`), now a Remnant fleet guarding two habitable worlds, Helios and Polaris. It asks whether the player is "omega" and refuses its override.
- **Void cores.** The Hollow, Chasm and Fissure hulls run exotic "Void" power cores that "draw energy from nowhere". Spacer legends attach to each (descriptions `nskr_reverie_boss`, `nskr_harbinger_boss`, `nskr_afflictor_boss`). The Hollow is the flagship of "a rogue AI core that, by most accounts, only wanted a brief vacation from its blacksite assignment". Its fleet answers comms with "What have we done?" repeated (rules `abyssDialog`).
- **Anti-Remnant Organization (ARO).** Posts a bounty on the Void Group. One of its strike groups preaches purging "the Remnant scourge" by fire (rules `AROstrikeDialogInitial`).
- **The Peacekeepers.** An infamous mercenary group under Alistair Walsh, flagship "ISS White Whale", policing Independent space. Many parties want it gone.
- **Rogue Co.** A pre-Collapse group of "engineers, smugglers, and ex-military specialists" whose hand-built Rorqual cruisers survive as priceless relics.
- **Blacksites.** Improvised storage stations of several factions, used for "less-than-legal logistics", rigged with tripwire alarms.

### Custom starts

Unlocked by finishing the questline:

- **Throne's Gift.** "You accepted the gift. The music grows louder as you travel the vacuum - persistent, insistent." The player can automate ships. In combat a "HOLY SPIRIT" appears with short lines such as "I am eternal" and "This changes nothing" (`ThronesGiftHolySpiritListener`).
- **Hellspawn.** "Born to no record, in a place erased from maps. The vision came early. It hasn't left." Violence earns power, and a judgement by the entity THRN follows. THRN's faction appears to the player as "Unknown" and uses the ship prefix `THRN`.

## What characters can know

- **Kesteven's knowledge.** Kesteven works from decrypted fragments. Jack says "Enigma is one of the few words we can decipher from their transmissions". Alice admits "we don't know exactly that is inside the Cache yet". They hold back the Chip's specifications until later: "it's not important for you to know the exact specifications yet" (Jack, `DelveMeetingBarEvent`).
- **Eliza's knowledge.** Eliza has read two disks and knows about the Cache and the UPC ("The plans they had..."). She claims "eyes and ears all across the sector" and knows when the player carries the Chip.
- **The crew.** Ops, sensors and comms officers report observations and measurements. They do not know what the Enigma is; when they guess, they label it a guess ("Your guess is as good as mine captain.", `DataSatelliteDialog`).
- **The Enigma.** Enigma voices never explain themselves. Their lines are fragments, protocols and declarations.
- **Narration.** Narration describes what the player sees and must not explain the Enigma, the Cache or the UPC beyond what characters have learned.

## Premise

The mod's own tagline is "The things lost to time are out there." A tip adds: "There are things lost to time out there in this sector, but if you value your life it's best not to look."

The player works for the Kesteven Corporation on a questline that uncovers Project Enigma, the Cache and the Unlimited Production Chip. Around it are prototype ships, Enigma fleets, bounty legends and side jobs. The player's final choice decides who holds the Chip: Kesteven, Eliza, Tri-Tachyon, or nobody.

## Writing style

These instructions describe how the existing mod text is written. They govern player-facing prose, not how this reference explains its own rules.

- **Second person.** Narration addresses the captain as "you" in present tense and follows the bridge crew at work: "Your ops chief oversees the mission by-the-book, staging approach-and-scan to maximize safety..." (`DataSatelliteDialog`).
- **Competent crew.** Crew members are professionals with their own manner: the careful ops chief, the sarcastic sensors officer, the comms officer's "nothing a little elbow grease from our team can't solve".
- **Gray asides.** Gray narration carries the captain's own reactions and occasional judgment: "Wait, you really agree with her? Oh dear." (`ElizaDialog`), "Hope you made the right choice, captain." (`EndingElizaDialog`). `CacheDoubtDialog` uses the same gray voice as an inner voice that talks back.
- **Wide tonal range.** Comedy and horror sit side by side.
  - Comic: the drinking scenes (`HostileTakeoverBarEvent`, the artisan liqueur in `DelveMeetingBarEvent`); the Peacekeepers' commander asking whether "Sam" sent the player to prank him (rules `pkInspired`); the Cache guardian's taunts ("Is this autofit by chance?", "Have it writ upon thy meagre grave : Skill issue.", `CacheBossTauntPlugin`).
  - Grave: the Frozen Heart's enslaved thousands and the Enigma doctrine are written without jokes.
- **Machine voices.** AI and system voices use log formatting: bracketed tokens, lowercase queries, `INTERRUPT` breaks and cut-off protocol text. Example (rules `greetingEnigmaHostile`): `query. blood?. scanning. [BLOOD] collect, amass, gather, stockpile, hoard, reap...`
- **Lore by quotation.** Prototype hulls, weapons and upgrades carry a quoted project log or journal line with a named author instead of a technical description.
- **Profanity.** Allowed where the speaker would swear: Alice's "your little fuck up", pirate spacers' "corporate fuckers".

### Dialogue and player options

- Spoken options are quoted ("I'm looking for Eliza."); actions are not (Give her the UPC; Cut the comm link; Leave).
- A deliberate lie is marked "(lie)" in the option text, and the narration may comment on it ("Just tell her what she wants to hear.").
- Informational questions sit behind "I have some questions." and are disabled once read.
- Hostile fleets cut the link themselves: "The comm link is cut before you have a chance to respond."

### Common mistakes and corrections

- **Flattening Eliza.** Do not flatten Eliza into a cartoon terrorist, and do not soften her into a misunderstood reformer. Her views are a character choice and stay as they are. See [Eliza](#eliza).
- **Making Kesteven clean.** Kesteven presents itself as a force for "equality and democracy" while its own descriptions call the Republic a corporate proxy under Hegemony handlers. Keep both: the pitch and the record.
- **Explaining the Enigma.** No character, narrator or description may state what the Enigma is, what "blood" or "omega" means, or who the "maker" is. See [Open questions](#open-questions).
- **Moralizing narration.** Existing gray narration comments on a few choices; it does not lecture. Do not add explanations of why a choice is right or wrong.

## Characters

### Jack Lapua

Jack is Kesteven's space operations officer at Asteria (`nskr_opguy`), the questline's first contact and later a contact.

**Voice.** Charismatic and corporate: "a charismatic smile ... his manners have that corporate superficiality down to a perfection" (`nskr_kq_jackIntro`). In person he is tall and inviting, with "a big - and mostly genuine smile" (`DelveMeetingBarEvent`). He becomes a salesman when the player hesitates, turns commanding when the stakes rise, and enjoys winning.

- On Kesteven's mission (`DelveMeetingBarEvent`): "Think about it, Asteria is one of the last great bastions of equality and democracy. We are very much in a unique position to drive progress in this sector." This shows the sincere-sounding corporate pitch; he believes it, and the setting's own descriptions undercut it.
- On Eliza's death (rules `nskr_kq_jackLeadsElizaKilled`, job 5 tip): "I hear you managed to already take out Eliza for good, very impressive captain." The narration adds "There is a sinister smile on his face, you seem to have made his day." His warmth has a hard edge.
- At the Kesteven ending (`EndingKestevenDialog`): "This cold war of ours is about to go hot, but we will be ready." ... "I hope we can again work together in the future, burn bright." Defiance, confidence and a sign-off that belongs to him.
- Betrayed (rules `jackRevengeanceDialogInitial`): "I can't believe you betrayed us just like that, and to work with some lunatic spacer." He "struggles to maintain his composure".

**Traits.** He keeps secrets politely: "That's classified information captain. I'm sure you'll understand." He slips the player a bonus he "isn't supposed to", and offers artisan liqueur made with "*real* fruits, none of that synth crap". He is skeptical of rumors and proud of Kesteven's work.

### Alice Lumi

Alice is Kesteven's R&D manager (`nskr_researcher`): the job 3 and job 4 contact, and the one who decrypts the disks.

**Voice.** Clipped, impatient and condescending, with a "dry professor-esque tone". She measures people: "A woman is staring at you through the holodisplay, her expression is unchanging. Manager Lumi is intently analyzing every part of your visage." She swears when let down and grows passionate only about Enigma technology.

- Impatience (stage 15): "You should be heading to the bar. Do you always struggle with basic instructions?"
- Failure (job 3): "Let's hope they don't pull ahead in this race thanks to your little fuck up." ... "Just be glad I'm not firing you on the spot."
- Ambition (job 4, "Why are you so interested in this Enigma AI?"): "Her tone turns unusually passionate. 'You do see how advanced this "Enigma" technology is? With even a fraction of this power unlocked, one could have total supremacy over this sector.'" This is her real motive, stated plainly.
- On the Enigma (all disks): "Their hatred of anyone with their new technology is peculiar, it's like the collapse made them think anyone else isn't meant to exist at all. They are helplessly trying to maintain some broken status quo." It is her reading, not established fact. It ends with "Hah, relax, I hope I didn't scare you out of the job captain".
- On Eliza (`DelveMeetingBarEvent`): "*do not* listen to her nonsense, it's all lies to poison the mind."

**Traits.**
- A "slight, but devious smile" at sabotaging Tri-Tachyon, whom she suspects of being behind the Enigma.
- Disdain for "LZ": "No one important, we will deal with this 'LZ' in time."
- Rare, grudging praise: "Efficient work captain."
- A face that changes only when she drinks, "quite dramatically so".

### Nicholas Antoine

Nicholas works in Kesteven intelligence and communications at the Outpost (`nskr_intelligence`) and appears in job 4.

**Voice.** Reserved and hesitant, full of fillers and self-corrections. He worries about equipment: "Um, welcome captain." ... "So, the fleet was instructed to send encrypted hyperwave signals using rather expensive Domain comms equipment. You know I hope they didn't lose that stuff..." ... "There's this one thing. uhh-" (rules `nskr_kq_nicholasGreeting` and `nskr_kq_nicholasJob4Brief`, stage 12).

### Michael Roux

Michael Roux is President of the Democratic Republic of Asteria and Asteria's administrator (`nskr_president`). He has no dialogue in the mod.

### Eliza

Eliza is a revolutionary, and Kesteven calls her a terrorist (`nskr_anarchist`, post "anarchist", pirate faction). She holds two disks and runs her own port in a pirate freeport.

**Established facts.**

- **Identity.** "Eliza, last name unknown, first name definitely not her real name either." Kesteven files her as "a wanted terrorist and a vicious criminal" and "a political extremist", quiet "since her last attack a few years back" (Jack and Alice, `DelveMeetingBarEvent`).
- **Her port.** Recently refurbished and makeshift, guarded by rough bodyguards, one scarred and tattooed. She wears "a flashy and ornate looking uniform - dark leather belts loop around and hang off of the blood red uniform, underlined by shiny gold pins and decorations - a fit for a warlord", sits behind a fancy desk in an "exquisite dark leather" chair, and keeps a handcrafted notebook (`ElizaDialog`).
- **Her fleet.** "Eliza's Merc Armada", flagship "Regicide", which she commands herself.
- **Her conduct.** She keeps her word. She hands over the disks and later the promised equipment. When the player refuses her, she lets them leave unharmed: "I'm not just gonna stab you in the back on your way out like it would be customary for you corporate bastards." She punishes betrayal with death.
- **"LZ".** The mod links "LZ" to Eliza. At the meeting the player can answer her introduction with "Ah yes, that 'LZ' character." No line states it outright.

**Her views.** They are a deliberate character choice and must stay as written: coherent, radical, persuasive to some, and violent. Their core is a rejection of the Domain's order and everything she sees continuing it: the Hegemony, the corporations and bureaucratic obedience. She sees the Collapse as a lost opportunity and wants a second one.

- On obedience (`ElizaDialog`): "What do you live for captain? To wake up at exact same minute every morning? To wear the exact same uniform every day? To only call people by Sir and Ma'am? To do exactly as your told? Even when your being slowly pushed to your death?" ... "When I saw so many of my peers slowly lose every part their selves to the bureaucracy machine - I swore I would never become one of them."
- On the Collapse: "The collapse gave such a beautiful opportunity for humanity to be born a new. The Hegemony is fatally gripped by the longing for an old world - Everything will be exactly the same it was no exceptions, leading to the same failures all over again. There's still a chance for us to learn from the past, all it takes is a second collapse." (narration: "She lets out a devilish little smile.")
- On law: "When tyranny becomes law, resistance becomes duty." ... "Laws must exist to keep the people subservient, and those who show signs of disobedience will be punished."
- On violence: "Sacrifices have to be made in the face of progress! Life isn't black and white, sometimes you have to get your hands dirty captain to make a change. For there are not many other options left!"
- On her goal: "With this power the sector will be free. No masters - no rules, right captain?"
- At victory (`EndingElizaDialog`): "watch the stations burn, empires fall, leaders flee like cowards. It is time to infest the rat's nest. Humanity will be free!" The narration calls the gesture "vaguely fanatic" and the grin "bone chilling".

**Voice.**

- **Host.** She opens as a host ("Welcome to my humble port captain." ... "Feel free to take a seat, we have a lot to discuss.") and demands candor: "Such indecisiveness is not characteristic for a great captain. Come on spit it out, just say what you *really* think."
- **Contempt for apathy.** She has open contempt for apathy ("Damn, they really did brainwash you into the perfect killing machine huh. ... I'm sure you don't even flinch when you glass a colony with the flick of a switch.") and for corporate loyalty ("Another victim of corporate propaganda I see. Ready to die for the 'greater good' I'm sure.").
- **Temper.** She loses her temper when challenged: "Her face quickly turns red." "You don't just barge in, and start accusing me like some hound from COMSEC!"
- **Sarcasm and threat.** "Good day to you captain, what a coincidence that we meet out here." (rules `nskr_elizaInterceptDialogInitial`). "We had a simple deal captain - if you are not willing to hold up your part, I shall fulfill it by force." (rules `nskr_elizaInterceptDialogExitFightDialog`).
- **Betrayed.** Fury and a string of insults: "You did what?! You disgusting corporate scum. You sycophant. You bootlicker. You- you betrayer of the cause, now *you* die." (rules `nskr_elizaInterceptDialogExitFightNoChip`). "The sector will not miss tyrants like you, the people won't miss you. I will not miss you." (rules `elizaRevengeanceDialogContinue3`).

**Writing her.**

- She must argue from conviction and must not concede her worldview to win the player over or to make a scene easier.
- Do not make her secretly reasonable, secretly cruel for its own sake, or a mouthpiece for the author.
- Keep her hospitality, her honesty on her own terms and her open endorsement of violence together. Existing text shows all three in one conversation.
- Other characters may call her a terrorist or a lunatic. That is their view, and the mod lets the player agree with her sincerely (`KestevenFlag.ELIZA_AGREED_SINCERELY`).

### The Enigma voices

- **The Enigma core.** The core at the Frozen Heart has no speech. Its only answer is the back-door package.
- **Enigma Fragment #1.** The Cache guardian's commander speaks once before battle (rules `cacheDialogInitial`): "I see you chose ambition starfarer. Now it's time to act honestly, with cruelty. If you would make a mountain of the dead, pile it so it reaches the sky. If you would shed blood let it run as a river. Such actions lead to victory... Is this not the mortals desire, burn brightly before fading out - To win, no matter the cost." In battle its taunts mix grandeur ("We are absolute technological superiority.", "Moloch has granted me a purpose.") with mockery ("The test says you suck, captain. Wow, I wasn't even testing for that.").
- **Strike groups and patrols.** The protocol-and-fragment voice described under [The Enigma](#the-enigma).

### THRN

THRN is the Hellspawn judge. It speaks in short gray lines, sometimes one word at a time:

- the warning (rules `nskr_hs_warningOpen` to `nskr_hs_warning2`): "Soon you will be judged." ... "Be ready." "Be." "Ready.";
- the judgement (rules `nskr_hs_judgement1` and the `nskr_hs_peaceful`, `nskr_hs_neutral` and `nskr_hs_hell` rows), in three outcomes:
  - for a peaceful captain, mercy ("You tried your best to maintain peace. Despite your calling. Maybe there is hope for this sector after all...");
  - for a violent one, a sentence ("For everyone you've killed. For every ship you've wrecked. For every station you've burned. ... Die.");
  - for the worst, approval ("Let the blood run as a river. ... Your empire of ash." then "WELCOME TO HELL").

### Minor voices

| Speaker | Source | Voice |
|---|---|---|
| Tri-Tachyon employee | `HostileTakeoverBarEvent` | Friendly heavy drinker who gets the captain into a management party |
| Tri-Tachyon collector | rules `nskr_ttCollectorDialog*` | Resentful and businesslike: "We know you are working with those snakes from Kesteven." ... "Pleasure doing business with you captain." |
| Tri-Tachyon buyer | `nskr_altEndingDialogTT` | Skeptical, then greedy: "Just think of all the things you could buy." (ravenous smile) |
| Luddic official | `nskr_altEndingDialogLuddic` | Scriptural: "I hear you speak of a vile creation of mammon." ... "you do not *get* your way to providence." |
| Pirate spacers | Eliza bar events | Hostile to corporations and questions: "Keep your nose out of our business captain." "You better not be another bloodhound from CommSec." |
| Special Operations captain | rules `nskr_job4FleetDialog*` | Exhausted and grateful: "Am I glad to finally see a friendly face around here." |
| Kesteven debt collector | rules `nskr_ic_collector*` | Ruthless and sarcastic: "Thank you for your business captain. Glad we could come to terms, peacefully." |
| "LZ" messenger | rules `MessengerFleetDialogInitial` | Formal letter: "Tread with care captain, you are entering a dangerous field of work." |
| ARO captain | rules `AROstrikeDialogInitial` | Zealot: "May the cold vacuum of space redeem your spirit." |
| Alistair Walsh | rules `pk*` | Pompous and easily rattled: "crime doesn't pay!" |
| Umbra | rules `eternityDialog` | Protocol voice: "UMBRA operations ... authorized to use lethal force" |

## Faction voices

| Faction | How it sounds in mod text |
|---|---|
| Kesteven | Corporate and transactional ("Thank you for your business."); proud in public, ruthless when collecting; its officers deal in datapads, briefings and payouts |
| Enigma | Corrupted Domain protocols, defense-contractor advertising, fragments about blood, makers and omega; never conversational |
| Tri-Tachyon | Rival corporate: resentful of Kesteven, openly mercenary |
| Pirates | Anti-corporate spacers, suspicious and profane; Eliza's port is theirs |
| Luddic Church and Path | Providence, mammon and "the right path" |
| Hegemony | No direct dialogue. Kesteven's patron; the order Eliza fights. |
| Prototype Operations | No dialogue. Ship prefix `OPS`; fields black-ops fleets from the market holding the UPC. |

## Terminology

| Term | Use |
|---|---|
| Kesteven, Kesteven Corporation, Kesteven Construction, KCC | The faction; ship prefix `K-Corp` |
| Democratic Republic of Asteria, DRA | Asteria's nominal government |
| Asteria, Asteria Station | Kesteven's home world and its orbital station |
| Frozen Heart | The Enigma station in the Frost system; the system's name is one of Frostbite, Newfoundland, Greenland, Antarctica, Permafrost, Hailstone, Archangel or Inari |
| Glacier, Bleak, Siberia, Shiver, Algor | Frost planets |
| Unknown Site, the Cache | The Cache system and the site itself |
| Project : Enigma | As printed on the data disks, with the spaced colon |
| Unknown Prototype, Project Enigma, Domain-Era Prototype, Void, Rogue Co. | Manufacturer and design-type labels |
| DSRD | Enigma ship prefix |
| Unlimited Production Chip, UPC | The Cache's prize |
| Artifact Electronics | Salvaged relic components; the exchange currency |
| Data disks | The five disks leading to the Cache |
| Enemy Unknown, Hostile Takeover, Operation Lifesaver, The Delve | Questline job names |
| P-space, transverse jump, TriPad | In-setting terms used in existing text |
| CommSec, COMSEC | Both spellings appear; their meaning is not established |

## Open questions

Existing text leaves these open. It does not say whether they are meant to stay unanswered. Ask the author before writing an answer; until then no character or narrator may resolve them.

- What the Enigma is, and whether it is one mind or many.
- What "blood", "omega" and "the maker" mean to the Enigma.
- What `DSRD` stands for.
- Whether "LZ" is Eliza.
- Whether Commander Umbra is the project's Test Pilot "Umbra".
- Eliza's real name and the attack Kesteven attributes to her.
- What CommSec or COMSEC is.
- Who Prototype Operations are.
- What THRN and the HOLY SPIRIT are, and what "Moloch" refers to.
- What the quiet hum at the Cache core is.

## Canon sources

In order of precedence:

1. This reference, once the author approves a section.
2. Existing mod text: `data/campaign/rules.csv`, Java dialogue strings, `data/strings/descriptions.csv` and the other data files listed under [Status and sources](#status-and-sources).
3. Vanilla Starsector canon.

When sources conflict, record the conflict here and ask the author.
