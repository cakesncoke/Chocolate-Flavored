// Development compatibility fixture. Never installed by Chocolate Flavored itself.
ServerEvents.recipes(event => {
  event.remove({ id: 'chocolateflavored:mud_kiln/bread' })
  event.custom({
    type: 'chocolateflavored:mud_kiln',
    ingredient: 'minecraft:carrot',
    result: { id: 'minecraft:golden_carrot' },
    cookingtime: 4
  }).id('kubejs:verification_mud_kiln')
  event.custom({
    type: 'chocolateflavored:cooking_pot',
    ingredients: [{ ingredient: 'minecraft:apple', count: 2 }, { ingredient: 'minecraft:carrot' },
      { ingredient: 'minecraft:potato', count: 3 }, { ingredient: 'minecraft:beetroot', count: 4 },
      { ingredient: 'minecraft:wheat', count: 5 }, { ingredient: 'minecraft:sugar', count: 6 }],
    results: [{ id: 'minecraft:golden_carrot', count: 2 }, { id: 'minecraft:apple', count: 3 },
      { id: 'minecraft:bread', count: 4 }, { id: 'minecraft:potato', count: 5 },
      { id: 'minecraft:beetroot', count: 6 }, { id: 'minecraft:sugar', count: 7 }]
  }).id('kubejs:verification_pot')
})
