package com.cleanroommc.standalone.api.teleport;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneValues;
import com.cleanroommc.standalone.api.net.NetworkHandler;
import com.cleanroommc.standalone.api.net.packet.CPacketOpenAuthGui;
import com.cleanroommc.standalone.api.net.packet.CPacketTravelEvent;
import com.cleanroommc.standalone.api.util.BlockCoord;
import com.cleanroommc.standalone.api.util.StandaloneUtilities;
import com.cleanroommc.standalone.api.vectors.Vector3d;
import com.cleanroommc.standalone.api.vectors.VectorUtil;
import com.cleanroommc.standalone.common.StandaloneConfig;
import com.cleanroommc.standalone.utils.StandaloneLog;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Standalone.MODID, value = Side.CLIENT)
public class TravelController {

    private static boolean wasJumping = false;
    private static boolean wasSneaking = false;
    private static boolean showTargets = false;
    private static boolean selectionEnabled = true;

    private static BlockPos onBlockCoord;
    private static BlockPos selectedCoord;

    private static final HashSet<BlockPos> candidates = new HashSet<>();

    private static double fovRad;

    @SuppressWarnings("FieldMayBeFinal")
    private static List<Block> blacklistedBlocks = Arrays.stream(StandaloneConfig.travel.blockBlacklist)
            .map(ResourceLocation::new)
            .map(ForgeRegistries.BLOCKS::getValue)
            .filter(b -> {
                if (b == null) {
                    StandaloneLog.logger.info("Travel Block Blacklist found null block!");
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());

    static {
        for (String entry : StandaloneConfig.travel.blockBlacklist) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));
            if (block != null)
                blacklistedBlocks.add(block);
        }
    }

    private static boolean doesHandAllowTravel(@Nonnull EnumHand hand) {
        return StandaloneConfig.travel.enableOffHandTravel || hand == EnumHand.MAIN_HAND;
    }

    private static boolean doesHandAllowBlink(@Nonnull EnumHand hand) {
        return StandaloneConfig.travel.enableOffHandBlink || hand == EnumHand.MAIN_HAND;
    }

    public static boolean activateTravelAccessible(@Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull World world, @Nonnull EntityPlayer player, @Nonnull TravelSource source) {
        BlockPos target = selectedCoord;
        if (target == null) {
            return false;
        }
        TileEntity te = world.getTileEntity(target);
        if (te instanceof ITravelAccessible) {
            ITravelAccessible ta = (ITravelAccessible) te;
            if (ta.getRequiresPassword(player)) {
                NetworkHandler.channel.sendToServer(new CPacketOpenAuthGui(target).toFMLPacket());
                return true;
            }
        }
        if (doesHandAllowTravel(hand)) {
            travelToSelectedTarget(player, equipped, hand, source, false);
            return true;
        }
        return true;
    }

    public static boolean doBlink(@Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull EntityPlayer player) {
        if (!doesHandAllowBlink(hand))
            return false;

        Vector3d eye = StandaloneUtilities.getEyePositionStandalone(player);
        Vector3d look = StandaloneUtilities.getLookVecStandalone(player);

        Vector3d sample = new Vector3d(look);
        sample.scale(TravelSource.STAFF_BLINK.getMaxDistanceTravelled());
        sample.add(eye);
        Vec3d eye3 = new Vec3d(eye.x, eye.y, eye.z);
        Vec3d end = new Vec3d(sample.x, sample.y, sample.z);

        double playerHeight = player.getYOffset();

        // if you're looking at your feet, and your player height to the max distance, or part there of
        double lookComp = -look.y * playerHeight;
        double maxDistance = TravelSource.STAFF_BLINK.getMaxDistanceTravelled() + lookComp;

        RayTraceResult p = player.world.rayTraceBlocks(eye3, end, !StandaloneConfig.travel.enableBlinkNonSolidBlocks);
        if (p == null) {
            // go as far as possible
            for (double i = maxDistance; i > 1; i--) {
                sample.set(look);
                sample.scale(i);
                sample.add(eye);
                // we test against our feets location
                sample.y -= playerHeight;

                if (doBlinkAround(player, equipped, hand, sample, true))
                    return true;
            }
            return false;
        } else {
            List<RayTraceResult> res = StandaloneUtilities.raytraceAll(player.world, eye3, end, !StandaloneConfig.travel.enableBlinkNonSolidBlocks);
            for (RayTraceResult pos : res) {
                if (pos != null) {
                    IBlockState hitBlock = player.world.getBlockState(pos.getBlockPos());
                    if (isBlackListedBlock(player, pos, hitBlock)) {
                        BlockPos bp = pos.getBlockPos();
                        maxDistance = Math.min(maxDistance, VectorUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) - 1.5 - lookComp);
                    }
                }
            }

            Vector3d targetBc = new Vector3d(p.getBlockPos());
            double sampleDistance = 1.5;
            BlockPos bp = p.getBlockPos();
            double teleDistance = VectorUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) + sampleDistance;

            while (teleDistance < maxDistance) {
                sample.set(look);
                sample.scale(sampleDistance);
                sample.add(targetBc);
                // we test against our feets location
                sample.y -= playerHeight;

                if (doBlinkAround(player, equipped, hand, sample, false)) {
                    return true;
                }
                teleDistance++;
                sampleDistance++;
            }
            sampleDistance = -0.5;
            teleDistance = VectorUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) + sampleDistance;
            while (teleDistance > 1) {
                sample.set(look);
                sample.scale(sampleDistance);
                sample.add(targetBc);
                // we test against our feets location
                sample.y -= playerHeight;

                if (doBlinkAround(player, equipped, hand, sample, false)) {
                    return true;
                }
                sampleDistance--;
                teleDistance--;
            }
        }
        return false;
    }

    private static boolean isBlackListedBlock(@Nonnull EntityPlayer player, @Nonnull RayTraceResult pos, @Nonnull IBlockState hitBlock) {
        return blacklistedBlocks.contains(hitBlock.getBlock())
                || (hitBlock.getBlockHardness(player.world, pos.getBlockPos()) < 0 && !StandaloneConfig.travel.enableBlinkUnbreakableBlocks);
    }

    private static boolean doBlinkAround(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull Vector3d sample, boolean conserveMomentum) {
        if (doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y) - 1, (int) Math.floor(sample.z)), conserveMomentum))
            return true;
        if (doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y), (int) Math.floor(sample.z)), conserveMomentum))
            return true;
        return doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y) + 1, (int) Math.floor(sample.z)), conserveMomentum);
    }

    private static boolean doBlink(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull BlockPos coord, boolean conserveMomentum) {
        return travelToLocation(player, equipped, hand, TravelSource.STAFF_BLINK, coord, conserveMomentum);
    }

    public static boolean showTargets() {
        return showTargets && selectionEnabled;
    }

    public static void setSelectionEnabled(boolean isSelectionEnabled) {
        selectionEnabled = isSelectionEnabled;
        if (!selectionEnabled)
            candidates.clear();
    }

    public static boolean isBlockSelected(@Nonnull BlockPos pos) {
        return pos.equals(selectedCoord);
    }

    public static void addCandidate(@Nonnull BlockPos pos) {
        candidates.add(pos);
    }

    @SubscribeEvent
    public static void onRender(@Nonnull RenderWorldLastEvent event) {
        float fov = Minecraft.getMinecraft().gameSettings.fovSetting;
        fovRad = Math.toRadians(fov);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player == null)
                return;
            if (player.isSpectator()) {
                showTargets = false;
                candidates.clear();
                return;
            }

            Pair<BlockPos, ITravelAccessible> pair = getActiveTravelBlock(player);
            onBlockCoord = pair != null ? pair.getLeft() : null;
            boolean onBlock = onBlockCoord != null;
            showTargets = onBlock || isTravelItemActiveForSelecting(player);

            if (showTargets)
                updateSelectedTarget(player);
            else
                selectedCoord = null;

            MovementInput input = player.movementInput;
            if (input == null)
                return;

            boolean tempJump = input.jump;
            boolean tempSneak = input.sneak;

            // Handles teleportation if a target is selected
            if (input.jump && !wasJumping && StandaloneConfig.travel.allowJumping && onBlock && selectedCoord != null ||
                    input.sneak && !wasSneaking && StandaloneConfig.travel.allowSneaking && onBlock && selectedCoord != null) {

                onInput(player);
            }

            // Elevator: If there is no selected coordinate and the input is jump, go up
            if (input.jump && !wasJumping && onBlock && selectedCoord == null) {
                updateVerticalTarget(player, 1);
                onInput(player);
            }

            // Elevator: If there is no selected coordinate and the input is sneak, go down
            if (input.sneak && !wasSneaking && onBlock && selectedCoord == null) {
                updateVerticalTarget(player, -1);
                onInput(player);
            }

            wasJumping = tempJump;
            wasSneaking = tempSneak;
            candidates.clear();
        }
    }

    private static int getEnergyInTravelItem(@Nonnull ItemStack equipped) {
        if (!(equipped.getItem() instanceof ITravelItem)) {
            return 0;
        }
        return ((ITravelItem) equipped.getItem()).getEnergyStored(equipped);
    }

    public static boolean isTravelItemActiveForRendering(@Nonnull EntityPlayer ep) {
        return isTravelItemActive(ep, ep.getHeldItemMainhand()) || (StandaloneConfig.travel.enableOffHandTravel && isTravelItemActive(ep, ep.getHeldItemOffhand()));
    }

    private static boolean isTravelItemActiveForSelecting(@Nonnull EntityPlayer ep) {
        return isTravelItemActive(ep, ep.getHeldItemMainhand()) || isTravelItemActive(ep, ep.getHeldItemOffhand());
    }

    private static boolean isTravelItemActive(@Nonnull EntityPlayer ep, @Nonnull ItemStack equipped) {
        return equipped.getItem() instanceof ITravelItem && ((ITravelItem) equipped.getItem()).isActive(ep, equipped);
    }

    private static boolean travelToSelectedTarget(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull TravelSource source, boolean conserveMomentum) {
        if (selectedCoord == null)
            return false;
        return travelToLocation(player, equipped, hand, source, selectedCoord, conserveMomentum);
    }

    private static boolean travelToLocation(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull TravelSource source, @Nonnull BlockPos coord, boolean conserveMomentum) {
        if (source != TravelSource.STAFF_BLINK) {
            TileEntity te = player.world.getTileEntity(coord);
            if (te instanceof ITravelAccessible) {
                ITravelAccessible ta = (ITravelAccessible) te;
                if (!ta.canBlockBeAccessed(player)) {
                    player.sendMessage(new TextComponentTranslation("standalone.travel_accessible.unauthorized"));
                    return false;
                }
            }
        }

        int requiredPower = getRequiredPower(player, equipped, source, coord);
        if (requiredPower < 0)
            return false;

        if (!isInRangeTarget(player, coord, source.getMaxDistanceTravelledSq())) {
            if (source != TravelSource.STAFF_BLINK) {
                player.sendStatusMessage(new TextComponentTranslation("standalone.travel_accessible.outofrange"), true);
            }
            return false;
        }
        if (!isValidTarget(player, coord, source)) {
            if (source != TravelSource.STAFF_BLINK) {
                player.sendStatusMessage(new TextComponentTranslation("standalone.travel_accessible.invalidtarget"), true);
            }
            return false;
        }
        if (doClientTeleport(player, hand, coord, source, requiredPower, conserveMomentum) && StandaloneConfig.travel.createParticles) {
            for (int i = 0; i < 6; ++i) {
                player.world.spawnParticle(EnumParticleTypes.PORTAL, player.posX + (StandaloneValues.RNG.nextDouble() - 0.5D), player.posY + StandaloneValues.RNG.nextDouble() * player.height - 0.25D,
                        player.posZ + (StandaloneValues.RNG.nextDouble() - 0.5D), (StandaloneValues.RNG.nextDouble() - 0.5D) * 2.0D, -StandaloneValues.RNG.nextDouble(), (StandaloneValues.RNG.nextDouble() - 0.5D) * 2.0D);
            }
        }
        return true;
    }

    private static int getRequiredPower(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull TravelSource source, @Nonnull BlockPos coord) {
        if (!isTravelItemActive(player, equipped)) {
            return 0;
        }
        int requiredPower;
        requiredPower = (int) (getDistance(player, coord) * source.getPowerCostPerBlockTraveledRF());
        int canUsePower = getEnergyInTravelItem(equipped);
        if (requiredPower > canUsePower) {
            player.sendStatusMessage(new TextComponentTranslation("standalone.item_travel_staff.chat.notEnoughPower"), true);
            return -1;
        }
        return requiredPower;
    }

    private static boolean isInRangeTarget(@Nonnull EntityPlayer player, @Nonnull BlockPos bc, float maxSq) {
        return getDistanceSquared(player, bc) <= maxSq;
    }

    private static double getDistanceSquared(@Nonnull EntityPlayer player, @Nonnull BlockPos bc) {
        Vector3d eye = StandaloneUtilities.getEyePositionStandalone(player);
        Vector3d target = new Vector3d(bc.getX() + 0.5, bc.getY() + 0.5, bc.getZ() + 0.5);
        return eye.distanceSquared(target);
    }

    private static double getDistance(@Nonnull EntityPlayer player, @Nonnull BlockPos coord) {
        return Math.sqrt(getDistanceSquared(player, coord));
    }

    private static boolean isValidTarget(@Nonnull EntityPlayer player, @Nonnull BlockPos bc, @Nonnull TravelSource source) {
        World w = player.world;
        BlockPos baseLoc = bc;
        if (source != TravelSource.STAFF_BLINK) {
            // targeting a block so go one up
            baseLoc = bc.offset(EnumFacing.UP);
        }

        return canTeleportTo(player, source, baseLoc, w) && canTeleportTo(player, source, baseLoc.offset(EnumFacing.UP), w);
    }

    private static boolean canTeleportTo(@Nonnull EntityPlayer player, @Nonnull TravelSource source, @Nonnull BlockPos bc, @Nonnull World w) {
        if (bc.getY() < 1) {
            return false;
        }
        if (source == TravelSource.STAFF_BLINK && !StandaloneConfig.travel.enableBlinkSolidBlocks) {
            Vec3d start = StandaloneUtilities.getEyePosition(player);
            Vec3d target = new Vec3d(bc.getX() + 0.5f, bc.getY() + 0.5f, bc.getZ() + 0.5f);
            if (!canBlinkTo(bc, w, start, target)) {
                return false;
            }
        }

        IBlockState bs = w.getBlockState(bc);
        Block block = bs.getBlock();
        if (block.isAir(bs, w, bc)) {
            return true;
        }

        final AxisAlignedBB aabb = bs.getBoundingBox(w, bc);
        return aabb.getAverageEdgeLength() < 0.7;
    }

    private static boolean canBlinkTo(@Nonnull BlockPos bc, @Nonnull World w, @Nonnull Vec3d start, @Nonnull Vec3d target) {
        RayTraceResult p = w.rayTraceBlocks(start, target, !StandaloneConfig.travel.enableBlinkNonSolidBlocks);
        if (p != null) {
            if (!StandaloneConfig.travel.enableBlinkNonSolidBlocks)
                return false;

            IBlockState bs = w.getBlockState(p.getBlockPos());
            Block block = bs.getBlock();
            if (isClear(w, bs, block, p.getBlockPos())) {
                if (BlockCoord.get(p).equals(bc))
                    return true;

                // need to step
                Vector3d sv = new Vector3d(start.x, start.y, start.z);
                Vector3d rayDir = new Vector3d(target.x, target.y, target.z);
                rayDir.sub(sv);
                rayDir.normalize();
                rayDir.add(sv);
                return canBlinkTo(bc, w, new Vec3d(rayDir.x, rayDir.y, rayDir.z), target);

            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean isClear(@Nonnull World w, @Nonnull IBlockState bs, @Nonnull Block block, @Nonnull BlockPos bp) {
        if (block.isAir(bs, w, bp)) {
            return true;
        }
        final AxisAlignedBB aabb = bs.getBoundingBox(w, bp);
        if (aabb.getAverageEdgeLength() < 0.7) {
            return true;
        }

        return block.getLightOpacity(bs, w, bp) < 2;
    }

    @SideOnly(Side.CLIENT)
    private static void updateVerticalTarget(@Nonnull EntityPlayerSP player, int direction) {
        Pair<BlockPos, ITravelAccessible> pair = getActiveTravelBlock(player);
        if (pair == null)
            return;

        BlockPos currentBlock = pair.getKey();
        World world = Minecraft.getMinecraft().world;
        for (int i = 0, y = currentBlock.getY() + direction; i < pair.getValue().getTravelRangeDeparting() && y >= 0 && y <= 255; i++, y += direction) {

            // Circumvents the raytracing used to find candidates on the y axis
            TileEntity selectedBlock = world.getTileEntity(new BlockPos(currentBlock.getX(), y, currentBlock.getZ()));

            if (selectedBlock instanceof ITravelAccessible) {
                ITravelAccessible travelBlock = (ITravelAccessible) selectedBlock;
                BlockPos targetBlock = new BlockPos(currentBlock.getX(), y, currentBlock.getZ());

                if (travelBlock.canBlockBeAccessed(player) && isValidTarget(player, targetBlock, TravelSource.BLOCK)) {
                    selectedCoord = targetBlock;
                    return;
                } else if (travelBlock.getRequiresPassword(player)) {
                    player.sendStatusMessage(new TextComponentTranslation("standalone.travel_accessible.skip.locked"), true);
                } else if (travelBlock.getAccessMode() == ITravelAccessible.AccessMode.PRIVATE && !travelBlock.canUiBeAccessed(player)) {
                    player.sendStatusMessage(new TextComponentTranslation("standalone.travel_accessible.skip.private"), true);
                } else if (!isValidTarget(player, targetBlock, TravelSource.BLOCK)) {
                    player.sendStatusMessage(new TextComponentTranslation("standalone.travel_accessible.skip.obstructed"), true);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static void updateSelectedTarget(@Nonnull EntityPlayerSP player) {
        selectedCoord = null;
        if (candidates.isEmpty())
            return;

        double closestDistance = Double.MAX_VALUE;
        for (BlockPos bc : candidates) {
            // Exclude the block the player is standing on
            if (!bc.equals(onBlockCoord)) {
                Vector3d loc = new Vector3d(bc.getX() + 0.5, bc.getY() + 0.5, bc.getZ() + 0.5);
                double[] distanceAndAngle = getCandidateDistanceAndAngle(loc);
                double distance = distanceAndAngle[0];
                double angle = distanceAndAngle[1];

                if (distance < closestDistance && angle < getCandidateHitAngle()) {
                    // Valid hit, sorted by distance.
                    selectedCoord = bc;
                    closestDistance = distance;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static void onInput(@Nonnull EntityPlayerSP player) {
        MovementInput input = player.movementInput;
        BlockPos target = selectedCoord;
        if (target == null) {
            return;
        }

        TileEntity te = player.world.getTileEntity(target);
        if (te instanceof ITravelAccessible) {
            ITravelAccessible ta = (ITravelAccessible) te;
            if (ta.getRequiresPassword(player)) {
                NetworkHandler.channel.sendToServer(new CPacketOpenAuthGui(target).toFMLPacket());
                return;
            }
        }

        if (travelToSelectedTarget(player, ItemStack.EMPTY, EnumHand.MAIN_HAND, TravelSource.BLOCK, false)) {
            input.jump = false;
            try {
                // flyToggleTimer
                ObfuscationReflectionHelper.setPrivateValue(EntityPlayer.class, (EntityPlayer) player, 0, "field_71101_bC");
            } catch (Exception e) {
                // ignore
            }
        }

    }

    public static double getCandidateHitScale(double fullScreenScaling, double distance) {
        // Take 10% of the screen width per default as the maximum scale for hits (perfectly looking at block)
        return 0.10 * fullScreenScaling;
    }

    public static double getCandidateMissScale(double fullScreenScaling, double distance) {
        // At least 1.5 times the normal block size if the angle is not close to the block
        return 1.5;
    }

    public static double getCandidateHitAngle() {
        // CAREFUL: missAngle MUST BE >= hitAngle
        // Hit scale for blocks with an angle below this threshold
        return 0.087; // == ~5 degree
    }

    public static double getCandidateMissAngle() {
        // CAREFUL: missAngle MUST BE >= hitAngle
        // Miss scale for blocks with an angle above this threshold
        return Math.PI / 5; // == 36 degrees
    }

    @Nonnull
    public static double[] getCandidateDistanceAndAngle(@Nonnull Vector3d loc) {
        Vector3d eye = StandaloneUtilities.getEyePositionStandalone(Minecraft.getMinecraft().player);
        Vector3d look = StandaloneUtilities.getLookVecStandalone(Minecraft.getMinecraft().player);
        Vector3d relativeBlock = new Vector3d(loc);

        relativeBlock.sub(eye);
        double distance = relativeBlock.length();
        relativeBlock.normalize();

        // Angle in [0,pi]
        double angle = Math.acos(look.dot(relativeBlock));
        return new double[]{distance, angle};
    }

    public static double getScaleForCandidate(@Nonnull Vector3d loc, int maxDistanceSq) {
        // Retrieve the candidate distance and angle
        double[] distanceAndAngle = getCandidateDistanceAndAngle(loc);
        double distance = distanceAndAngle[0];
        double angle = distanceAndAngle[1];

        // To get screen relative scaling, normalize based on fov and
        // distance (this scaling factor would cause the block to be displayed
        // horizontally fitted to the screen)
        double fullScreenScaling = Math.tan(fovRad / 2) * 2 * distance;

        double scaleHit = getCandidateHitScale(fullScreenScaling, distance);
        double scaleMiss = getCandidateMissScale(fullScreenScaling, distance);

        double hitAngle = getCandidateHitAngle();
        double missAngle = getCandidateMissAngle();

        // Always apply configuration scaling factor
        double scale = (1 / .2) * StandaloneConfig.travel.visualScale;

        // Now we will scale according to the angle:
        // scaleHit for [0,hitAngle)
        // interpolate(scaleHit, scaleMiss) for [hitAngle,missAngle)
        // scaleMiss for [missAngle,pi]
        if (angle < hitAngle) {
            scale *= scaleHit;
        } else if (angle >= hitAngle && angle < missAngle) {
            double lerp = (angle - hitAngle) / (missAngle - hitAngle);
            scale *= scaleHit + lerp * (scaleMiss - scaleHit);
        } else {
            scale *= scaleMiss;
        }

        return scale;
    }

    // Note: This is restricted to the current player
    public static boolean doClientTeleport(@Nonnull Entity entity, @Nonnull EnumHand hand, @Nonnull BlockPos bc, @Nonnull TravelSource source, int powerUse, boolean conserveMomentum) {
        TeleportEntityEvent evt = new TeleportEntityEvent(entity, source, bc, entity.dimension);
        if (MinecraftForge.EVENT_BUS.post(evt)) {
            return false;
        }

        NetworkHandler.channel.sendToServer(new CPacketTravelEvent(evt.getTarget(), powerUse, conserveMomentum, source, hand).toFMLPacket());
        return true;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    private static Pair<BlockPos, ITravelAccessible> getActiveTravelBlock(@Nonnull EntityPlayerSP player) {
        World world = Minecraft.getMinecraft().world;
        if (world == null)
            return null;

        int x = MathHelper.floor(player.posX);
        int y = MathHelper.floor(player.getEntityBoundingBox().minY) - 1;
        int z = MathHelper.floor(player.posZ);
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ITravelAccessible) {
            if (((ITravelAccessible) tileEntity).isTravelSource())
                return Pair.of(new BlockPos(x, y, z), ((ITravelAccessible) tileEntity));
        }
        return null;
    }

    public static BlockPos getPosPlayerOn() {
        return onBlockCoord;
    }
}
