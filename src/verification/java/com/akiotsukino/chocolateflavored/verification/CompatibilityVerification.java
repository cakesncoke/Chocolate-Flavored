package com.akiotsukino.chocolateflavored.verification;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractBedBlock;
final class CompatibilityVerification {
 static Either<Player.BedSleepingProblem, Unit> startSleep(ServerPlayer player, BlockPos pos) {
  var state=player.level().getBlockState(pos); var bed=(AbstractBedBlock)state.getBlock();
  return player.startSleepInBed(bed,state,bed.getBedRule(player.level(),pos),pos);
 }
 private CompatibilityVerification() {}
}
