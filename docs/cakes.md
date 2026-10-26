# Flavored vanilla cakes

Pomegranate, Coffee, Cherry, and Chocolate Cake use the supplied block/item textures and native Minecraft 26.1.2 cake geometry. Chocolate Cake is the mod creative-tab icon; all four cake items appear in that tab. There are no crafting or processing recipes for them.

The cake block subclasses vanilla `CakeBlock`: seven servings, 2 hunger and 0.4 saturation per serving, vanilla hunger checks/statistics/game events, shrinking collision/selection shape, support requirements, piston behavior, hardness, and comparator outputs 14, 12, 10, 8, 6, 4, 2. Breaking a cake drops no cake, even with Silk Touch. Items stack to one and are placed to eat, exactly as vanilla. Composter chance is 100%; the native panda ground-food tag also includes these items.

Every full cake accepts any of the seventeen vanilla candles. Internal candle-cake forms preserve flavor and use the native candle artwork, lit particles/sounds, projectile ignition and extinguishing behavior. Flint and steel/fire charges light them; empty-hand candle clicks extinguish them. Eating returns a one-bite version of the same cake and drops the matching candle. A bitten cake cannot accept a candle. The vanilla candle-cake lookup remains untouched, so normal cake still works normally. Candle forms have no separate items or creative entries.

The vanilla eating routine is reused for candle-cake eating as well. The small candle attachment/return adapter exists because vanilla hardcodes the ordinary cake during these transitions. The internal forms inherit `AbstractCandleBlock` rather than mutating `CandleCakeBlock`'s global candle lookup.

Texture paths/hashes and the source main commit are recorded in [cake-assets.json](cake-assets.json). Artwork is project-owner-supplied; no upstream artist or license is invented. Existing resource-prmud_kilnance notices continue to apply. The model JSON references vanilla geometry rather than redistributing new external models. No mod dependency is added.

CI-only checks compare all four flavors against vanilla eating, hunger/saturation, slice shapes, signals, support and loot behavior; exercise all 68 candle combinations, lighting/extinguishing, flavor preservation and vanilla candle-map integrity; and verify the creative-tab icon. Client checks bake every plain/sliced/lit/unlit model and render the four item textures. Verification code is excluded from the release JAR.
