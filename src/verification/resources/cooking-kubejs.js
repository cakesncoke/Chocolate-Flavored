// Development compatibility fixture. Never installed by Chocolate Flavored itself.
ServerEvents.recipes(event => {
  event.remove({ id: 'chocolateflavored:oven/bread' })
  event.custom({
    type: 'chocolateflavored:oven',
    ingredient: 'minecraft:carrot',
    result: { id: 'minecraft:golden_carrot' },
    cookingtime: 4
  }).id('kubejs:verification_oven')
  event.custom({
    type: 'chocolateflavored:cooking_pot',
    ingredients: ['minecraft:apple', 'minecraft:carrot'],
    result: { id: 'minecraft:mushroom_stew' },
    container: { id: 'minecraft:bowl' },
    cookingtime: 4
  }).id('kubejs:verification_pot')
})
