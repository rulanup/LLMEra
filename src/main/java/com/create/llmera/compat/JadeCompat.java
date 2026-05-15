package com.create.llmera.compat;

import com.create.llmera.LLMEraMod;
import com.create.llmera.block.IntelligentTransmitterBlock;
import com.create.llmera.block.SkillBoardBlock;
import com.create.llmera.block.ToolLinkStationBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class JadeCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger(LLMEraMod.MODID + ":JadeCompat");
    private static boolean initialized;

    private JadeCompat() {
    }

    public static void init(FMLCommonSetupEvent event) {
        if (initialized) {
            return;
        }
        if (!ModList.get().isLoaded("jade")) {
            return;
        }
        try {
            register(event);
            initialized = true;
            LOGGER.info("Jade compatibility registered");
        } catch (Exception e) {
            LOGGER.warn("Failed to register Jade compatibility: {}", e.toString());
        }
    }

    private static void register(FMLCommonSetupEvent event) throws Exception {
        Class<?> wailaCommonReg = Class.forName("snownee.jade.impl.WailaCommonRegistration");
        Class<?> wailaClientReg = Class.forName("snownee.jade.impl.WailaClientRegistration");

        Class<?> blockAccessorClass = Class.forName("snownee.jade.api.BlockAccessor");
        Class<?> blockCompProviderClass = Class.forName("snownee.jade.api.IBlockComponentProvider");
        Class<?> componentProviderClass = Class.forName("snownee.jade.api.IComponentProvider");
        Class<?> serverDataProviderClass = Class.forName("snownee.jade.api.IServerDataProvider");
        Class<?> iTooltipClass = Class.forName("snownee.jade.api.ITooltip");
        Class<?> iPluginConfigClass = Class.forName("snownee.jade.api.config.IPluginConfig");

        Object proxyInstance = Proxy.newProxyInstance(
                JadeCompat.class.getClassLoader(),
                new Class<?>[]{blockCompProviderClass, serverDataProviderClass},
                new JadeProviderHandler()
        );

        // Common registration
        Method getCommonInstance = wailaCommonReg.getMethod("instance");
        Object commonReg = getCommonInstance.invoke(null);
        Method commonStart = wailaCommonReg.getMethod("startSession");
        Method commonEnd = wailaCommonReg.getMethod("endSession");
        commonStart.invoke(commonReg);

        Method registerBlockData = wailaCommonReg.getMethod("registerBlockDataProvider",
                serverDataProviderClass, Class.class);
        registerBlockData.invoke(commonReg, proxyInstance, IntelligentTransmitterBlock.class);
        registerBlockData.invoke(commonReg, proxyInstance, ToolLinkStationBlock.class);
        registerBlockData.invoke(commonReg, proxyInstance, SkillBoardBlock.class);

        commonEnd.invoke(commonReg);

        // Client registration (only on physical client)
        if (FMLLoader.getDist().isClient()) {
            Method getClientInstance = wailaClientReg.getMethod("instance");
            Object clientReg = getClientInstance.invoke(null);
            Method clientStart = wailaClientReg.getMethod("startSession");
            Method clientEnd = wailaClientReg.getMethod("endSession");
            clientStart.invoke(clientReg);

            Method registerBlockComp = wailaClientReg.getMethod("registerBlockComponent",
                    componentProviderClass, Class.class);
            registerBlockComp.invoke(clientReg, proxyInstance, IntelligentTransmitterBlock.class);
            registerBlockComp.invoke(clientReg, proxyInstance, ToolLinkStationBlock.class);
            registerBlockComp.invoke(clientReg, proxyInstance, SkillBoardBlock.class);

            clientEnd.invoke(clientReg);
        }
    }

    private static class JadeProviderHandler implements InvocationHandler {

        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(LLMEraMod.MODID, "jade_provider");

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "getUid" -> UID;
                case "shouldRequestData" -> true;
                case "getDefaultPriority" -> 5000;
                case "appendServerData" -> {
                    appendServerData((CompoundTag) args[0], args[1]);
                    yield null;
                }
                case "appendTooltip" -> {
                    appendTooltip(args[0], args[1], args[2]);
                    yield null;
                }
                default -> null;
            };
        }

        private void appendServerData(CompoundTag data, Object accessor) throws Exception {
            BlockEntity blockEntity = getBlockEntity(accessor);
            if (blockEntity == null) {
                return;
            }
            String className = blockEntity.getClass().getName();
            if (className.equals("com.create.llmera.blockentity.IntelligentTransmitterBlockEntity")) {
                data.putString("kind", "transmitter");
                data.putString("ModelName", invokeStringGetter(blockEntity, "getModelUrl") + "|" + invokeStringGetter(blockEntity, "getModelName"));
                data.putString("AiName", invokeStringGetter(blockEntity, "getAiName"));
                data.putBoolean("Online", (Boolean) blockEntity.getClass().getMethod("isOnline").invoke(blockEntity));
                data.putString("NetworkId", invokeStringGetter(blockEntity, "getNetworkId"));
            } else if (className.equals("com.create.llmera.blockentity.ToolLinkStationBlockEntity")) {
                data.putString("kind", "tool");
                data.putString("DisplayName", invokeStringGetter(blockEntity, "getDisplayNameForNetwork"));
                data.putString("ToolType", invokeStringGetter(blockEntity, "getToolType"));
                data.putString("Target", invokeStringGetter(blockEntity, "getTargetDescription"));
                data.putString("TargetKind", invokeStringGetter(blockEntity, "getTargetKind"));
                data.putInt("RedstoneSignal", (Integer) blockEntity.getClass().getMethod("getRedstoneSignal").invoke(blockEntity));
                data.putBoolean("Enabled", (Boolean) blockEntity.getClass().getMethod("isEnabled").invoke(blockEntity));
            } else if (className.equals("com.create.llmera.blockentity.SkillBoardBlockEntity")) {
                data.putString("kind", "skill");
                data.putString("DisplayName", invokeStringGetter(blockEntity, "getDisplayNameForNetwork"));
                data.putBoolean("Enabled", (Boolean) blockEntity.getClass().getMethod("isEnabled").invoke(blockEntity));
            }
        }

        @SuppressWarnings("unchecked")
        private void appendTooltip(Object tooltipObj, Object accessor, Object config) throws Exception {
            Object serverDataObj = accessor.getClass().getMethod("getServerData").invoke(accessor);
            CompoundTag data = (CompoundTag) serverDataObj;
            String kind = data.getString("kind");

            Method addMethod = tooltipObj.getClass().getMethod("add", Component.class);

            switch (kind) {
                case "transmitter" -> {
                    String models = data.getString("ModelName");
                    String ai = data.getString("AiName");
                    String network = data.getString("NetworkId");
                    boolean online = data.getBoolean("Online");
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.transmitter.model", models));
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.transmitter.ai", ai));
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.transmitter.network", network));
                    addMethod.invoke(tooltipObj, online
                            ? Component.translatable("jade.llmera.transmitter.online")
                            : Component.translatable("jade.llmera.transmitter.offline"));
                }
                case "tool" -> {
                    String name = data.getString("DisplayName");
                    String type = data.getString("ToolType");
                    String target = data.getString("Target");
                    String targetKind = data.getString("TargetKind");
                    int signal = data.getInt("RedstoneSignal");
                    boolean enabled = data.getBoolean("Enabled");
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.tool.name", name));
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.tool.type", type));
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.tool.target", target, targetKind));
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.tool.signal", signal));
                    addMethod.invoke(tooltipObj, enabled
                            ? Component.translatable("jade.llmera.enabled")
                            : Component.translatable("jade.llmera.disabled"));
                }
                case "skill" -> {
                    String name = data.getString("DisplayName");
                    boolean enabled = data.getBoolean("Enabled");
                    addMethod.invoke(tooltipObj, Component.translatable("jade.llmera.skill.name", name));
                    addMethod.invoke(tooltipObj, enabled
                            ? Component.translatable("jade.llmera.enabled")
                            : Component.translatable("jade.llmera.disabled"));
                }
            }
        }

        private BlockEntity getBlockEntity(Object accessor) throws Exception {
            return (BlockEntity) accessor.getClass().getMethod("getBlockEntity").invoke(accessor);
        }

        private String invokeStringGetter(Object obj, String methodName) throws Exception {
            Object result = obj.getClass().getMethod(methodName).invoke(obj);
            return result == null ? "" : result.toString();
        }
    }
}
