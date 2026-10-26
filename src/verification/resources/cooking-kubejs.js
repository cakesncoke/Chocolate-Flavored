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
    ingredients: ['minecraft:apple', 'minecraft:carrot'],
    result: { id: 'minecraft:mushroom_stew' },
    container: { id: 'minecraft:bowl' },
    cookingtime: 4
  }).id('kubejs:verification_pot')
})
