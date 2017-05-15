package no.runsafe.creativetoolbox;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerLeftClickBlockEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerClickEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.worldguardbridge.IRegionControl;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PaintbrushManager implements IPlayerLeftClickBlockEvent, IPlayerRightClickBlock, IConfigurationChanged
{
	public PaintbrushManager(IRegionControl regionControl)
	{
		this.regionControl = regionControl;
	}

	@Override
	public void OnPlayerLeftClick(RunsafePlayerClickEvent event)
	{
		if (isPaintbrush(event.getPlayer().getItemInHand()))
		{
			IBlock block = event.getBlock();
			setPaintbrushBlock(event.getPlayer(),  block == null ? Item.Unavailable.Air : block.getMaterial());
			event.cancel();
		}
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		if (!player.getWorldName().equals(creativeWorldName))
			return true;

		Item blockType = getPaintbrushBlock(player);
		if (blockType != null && usingItem != null && isPaintbrush(usingItem))
		{
			if (regionControl.playerCanBuildHere(player, targetBlock.getLocation()))
			{
				targetBlock.set(blockType);
				return false;
			}
			player.sendColouredMessage("&cYou do not have permission to paint here.");
		}
		return true;
	}

	private boolean isPaintbrush(RunsafeMeta item)
	{
		if (item == null)
			return false;

		List<String> lore = item.getLore();
		return lore != null && lore.size() > 0 && lore.get(0).equals("Tool: Paintbrush");
	}

	private Item getPaintbrushBlock(IPlayer player)
	{
		UUID playerID = player.getUniqueId();
		return paintbrushes.containsKey(playerID) ? paintbrushes.get(playerID) : Item.Unavailable.Air;
	}

	public void setPaintbrushBlock(IPlayer player, Item setItem)
	{
		paintbrushes.put(player.getUniqueId(), setItem);
		player.sendColouredMessage("&ePaintbrush block changed: " + setItem.getName() + " [" + setItem.getData() + "]");
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		creativeWorldName = configuration.getConfigValueAsString("world");
	}

	private final HashMap<UUID, Item> paintbrushes = new HashMap<UUID, Item>(0);
	private final IRegionControl regionControl;
	private String creativeWorldName;
}
