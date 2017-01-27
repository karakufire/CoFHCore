package cofh.core.proxy;

import cofh.CoFHCore;
import cofh.core.CoFHProps;
import cofh.core.RegistrySocial;
import cofh.core.entity.EntityCoFHArrow;
import cofh.core.gui.client.GuiFriendsList;
import cofh.core.gui.element.*;
import cofh.core.init.CoreTextures;
import cofh.core.key.CoFHKeyHandler;
import cofh.core.render.CoFHFontRenderer;
import cofh.core.render.ShaderHelper;
import cofh.core.sided.IFunctionSided;
import cofh.core.sided.IRunnableClient;
import cofh.core.sided.IRunnableServer;
import cofh.core.util.KeyBindingMultiMode;
import cofh.lib.util.helpers.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@SideOnly (Side.CLIENT)
public class ProxyClient extends Proxy {

	public static CoFHFontRenderer fontRenderer;

	public static final KeyBind KEYBINDING_MULTIMODE = new KeyBind("key.cofh.multimode", Keyboard.KEY_V, "key.cofh.category");
	public static final KeyBind KEYBINDING_AUGMENTS = null; //new KeyBind("key.cofh.augments", Keyboard.KEY_C, "key.cofh.category");

	public static class KeyBind extends KeyBinding {

		public KeyBind(String name, int key, String category) {

			super(name, key, category);
		}

		public int cofh_conflictCode() {

			return 0;
		}

	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(new CoreTextures());

		Minecraft.memoryReserve = null;

		RenderingRegistry.registerEntityRenderingHandler(EntityCoFHArrow.class, new IRenderFactory<EntityCoFHArrow>() {
			@Override
			public Render<? super EntityCoFHArrow> createRenderFor(RenderManager manager) {

				return new RenderArrow<EntityCoFHArrow>(manager) {

					@Override
					protected ResourceLocation getEntityTexture(EntityCoFHArrow entity) {

						return new ResourceLocation("textures/entity/projectiles/arrow.png");//TODO
					}
				};
			}
		});

		/* GLOBAL */
		String category = "Global";
		CoFHCore.configClient.getCategory(category).setComment("The options in this section change core Minecraft behavior and are not limited to CoFH mods.");

		String comment = "Set to false to disable any particles from spawning in Minecraft.";
		if (!CoFHCore.configClient.get(category, "EnableParticles", true, comment)) {
			CoFHCore.log.info("Replacing EffectRenderer");
			Minecraft.getMinecraft().effectRenderer = new cofh.core.render.CustomEffectRenderer();
		}

		comment = "Set to false to disable chunk sorting during rendering.";
		if (!CoFHCore.configClient.get(category, "EnableRenderSorting", true, comment)) {
			CoFHProps.enableRenderSorting = false;
		}

		comment = "Set to false to disable all animated textures in Minecraft.";
		if (!CoFHCore.configClient.get(category, "EnableAnimatedTextures", true, comment)) {
			CoFHProps.enableAnimatedTextures = false;
		}

		/* GENERAL */
		category = "General";
		comment = "Set to false to disable shader effects in CoFH Mods.";
		if (!CoFHCore.configClient.get(category, "EnableShaderEffects", true, comment)) {
			CoFHProps.enableShaderEffects = false;
		}
		comment = "Set to true to use Color Blind Textures in CoFH Mods, where applicable.";
		if (CoFHCore.configClient.get(category, "EnableColorBlindTextures", false, comment)) {
			CoFHProps.enableColorBlindTextures = true;
		}

		/* INTERFACE */
		category = "Interface";
		comment = "Set to true to draw borders on GUI slots in CoFH Mods, where applicable.";
		if (!CoFHCore.configClient.get(category, "EnableGUISlotBorders", true, comment)) {
			CoFHProps.enableGUISlotBorders = false;
		}

		/* INTERFACE - TOOLTIPS */
		category = "Interface.Tooltips";
		comment = "Set to false to hide a tooltip prompting you to press Shift for more details on various items.";
		if (!CoFHCore.configClient.get(category, "DisplayHoldShiftForDetail", true, comment)) {
			StringHelper.displayShiftForDetail = false;
		}
		comment = "Set to true to display large item counts as stacks rather than a single quantity.";
		if (CoFHCore.configClient.get(category, "DisplayContainedItemsAsStackCount", false, comment)) {
			StringHelper.displayStackCount = true;
		}

		/* SECURITY */
		category = "Security";

		comment = "Set to false to disable warnings about Ops having access to 'secure' blocks upon logging on to a server.";
		if (!CoFHCore.configClient.get(category, "OpsCanAccessSecureBlocksWarning", true, comment)) {
			CoFHProps.enableOpSecureAccessWarning = false;
		}
		CoFHCore.configClient.save();

        /*l://Forge has removed their forcing of stencil bits.
		if (!Boolean.parseBoolean(System.getProperty("forge.forceDisplayStencil", "false"))) {
            if (Boolean.parseBoolean(System.getProperty("forge.forceNoStencil", "false"))) {
                break l;
            }
            try {
                if (Loader.isModLoaded("OpenMods")) {
                    if (ReflectionHelper.findField(OpenGlHelper.class, "field_153212_w").getInt(null) == 2) {
                        break l;
                    }
                }
                int i = 8;
                ReflectionHelper.findField(ForgeHooksClient.class, "stencilBits").setInt(null, i);
                Framebuffer b = Minecraft.getMinecraft().getFramebuffer();
                b.createBindFramebuffer(b.framebufferWidth, b.framebufferHeight);
                switch (ReflectionHelper.findField(OpenGlHelper.class, "field_153212_w").getInt(null)) {
                    case 2:
                        switch (EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT)) {
                            case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
                                break;
                            default: // stencil buffer is not supported
                                ReflectionHelper.findField(ForgeHooksClient.class, "stencilBits").setInt(null, i = 0);
                                b.createBindFramebuffer(b.framebufferWidth, b.framebufferHeight);
                        }
                        break;
                    default:
                        break;
                }
                BitSet stencilBits = ReflectionHelper.getPrivateValue(MinecraftForgeClient.class, null, "stencilBits");
                stencilBits.set(0, i);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }*/
	}

	@Override
	public void registerKeyBinds() {

		super.registerKeyBinds();
		CoFHKeyHandler.addKeyBind(KeyBindingMultiMode.instance);
		// CoFHKeyHandler.addKeyBind(KeyBindingAugments.instance);

		ClientRegistry.registerKeyBinding(KEYBINDING_MULTIMODE);
		// ClientRegistry.registerKeyBinding(KEYBINDING_AUGMENTS);
	}

	@Override
	public void registerRenderInformation() {

		TabAugment.initialize();
		TabConfiguration.initialize();
		TabEnergy.initialize();
		TabInfo.initialize();
		TabRedstone.initialize();
		TabSecurity.initialize();
		TabTutorial.initialize();

		ShaderHelper.initShaders();

		fontRenderer = new CoFHFontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);

		if (Minecraft.getMinecraft().gameSettings.language != null) {
			fontRenderer.setUnicodeFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLocaleUnicode());
			fontRenderer.setBidiFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLanguageBidirectional());
		}
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(fontRenderer);

		fontRenderer.initSpecialCharacters();
	}

	@Override
	public void registerTickHandlers() {

		super.registerTickHandlers();
	}

	@Override
	public int getKeyBind(String key) {

		if (key.equalsIgnoreCase("cofh.multimode")) {
			return KEYBINDING_MULTIMODE.getKeyCode();
		} else if (key.equalsIgnoreCase("cofh.augment")) {
			//return KEYBINDING_AUGMENTS.getKeyCode();
		}
		return -1;
	}

	@Override
	public void addIndexedChatMessage(ITextComponent chat, int index) {

		if (chat == null) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().deleteChatLine(index);
		} else {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, index);
		}
	}

	/*@SubscribeEvent
	public void blockRenderBlockOverlay(RenderBlockOverlayEvent evt) {

		if (evt.overlayType == OverlayType.BLOCK && !evt.blockForOverlay.isNormalCube()) {
			// occasionally the overlay code screws up and tries to overlay a block that didn't pass the checks, this fixes that
			evt.setCanceled(true);
		}
	}*/

	/* SERVER UTILS */
	@Override
	public boolean isOp(String playerName) {

		return true;
	}

	@Override
	public boolean isClient() {

		return true;
	}

	@Override
	public boolean isServer() {

		return false;
	}

	@Override
	public World getClientWorld() {

		return Minecraft.getMinecraft().theWorld;
	}

	@Override
	public IThreadListener getClientListener() {

		return Minecraft.getMinecraft();
	}

	@Override
	public IThreadListener getServerListener() {

		return Minecraft.getMinecraft().getIntegratedServer();
	}

	/* PLAYER UTILS */
	@Override
	public EntityPlayer findPlayer(String playerName) {

		for (Object a : FMLClientHandler.instance().getClient().theWorld.playerEntities) {
			EntityPlayer player = (EntityPlayer) a;
			if (player.getName().toLowerCase(Locale.US).equals(playerName.toLowerCase(Locale.US))) {
				return player;
			}
		}
		return null;
	}

	@Override
	public EntityPlayer getClientPlayer() {

		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public List<EntityPlayer> getPlayerList() {

		return new LinkedList<EntityPlayer>();
	}

	@Override
	public void updateFriendListGui() {

		if (Minecraft.getMinecraft().currentScreen != null) {
			((GuiFriendsList) Minecraft.getMinecraft().currentScreen).taFriendsList.textLines = RegistrySocial.clientPlayerFriends;
		}
	}

	/* SOUND UTILS */
	@Override
	public float getSoundVolume(int category) {

		if (category > SoundCategory.values().length) {
			return 0;
		}
		return FMLClientHandler.instance().getClient().gameSettings.getSoundLevel(SoundCategory.values()[category]);
	}

	@Override
	public void runServer(IRunnableServer runnable) {

	}

	@Override
	public void runClient(IRunnableClient runnable) {

		runnable.runClient();
	}

	@Override
	public <F, T> T apply(IFunctionSided<F, T> function, F input) {

		return function.applyClient(input);
	}
}
