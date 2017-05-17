package no.runsafe.creativetoolbox.event;

import no.runsafe.creativetoolbox.PlotFilter;
import no.runsafe.creativetoolbox.PlotManager;
import no.runsafe.creativetoolbox.database.PlotLogRepository;
import no.runsafe.creativetoolbox.database.PlotTagRepository;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.IAsyncEvent;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.worldguardbridge.IRegionControl;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InteractEvents implements IPlayerRightClickBlock, IPlayerInteractEntityEvent, IConfigurationChanged, IAsyncEvent
{
	public InteractEvents(
		IScheduler scheduler,
		PlotFilter plotFilter,
		IRegionControl worldGuard,
		PlotManager manager,
		PlotTagRepository tagRepository, PlotLogRepository logRepository, IServer server)
	{
		this.worldGuardInterface = worldGuard;
		this.plotFilter = plotFilter;
		this.manager = manager;
		this.tagRepository = tagRepository;
		this.logRepository = logRepository;
		this.server = server;
		this.scheduler = scheduler;
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta itemInHand, IBlock block)
	{
		if (manager.isInWrongWorld(player))
			return true;
		if (extensions.containsKey(player.getName()))
		{
			String target = extensions.get(player.getName());
			extensions.remove(player.getName());
			manager.extendPlot(player, target, block.getLocation());
			return false;
		}

		if (itemInHand != null && itemInHand.getItemId() == listItem)
		{
			if (stickTimer.containsKey(player))
				return true;
			this.listPlotsByLocation(block.getLocation(), player);
			registerStickTimer(player);
			return false;
		}
		return true;
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();
		if (player == null || manager.isInWrongWorld(player))
			return;
		if (event.getRightClicked() instanceof IPlayer && player.hasPermission("runsafe.creative.list"))
		{
			if (player.getItemInHand() != null && player.getItemInHand().getItemId() == listItem)
			{
				if (stickTimer.containsKey(event.getPlayer()))
					return;
				this.listPlotsByPlayer((IPlayer) event.getRightClicked(), player);
				registerStickTimer(player);
				event.cancel();
			}
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		listItem = configuration.getConfigValueAsInt("list_item");
	}

	public void startPlotExtension(IPlayer player, String plot)
	{
		extensions.put(player.getName(), plot);
	}

	private void registerStickTimer(final IPlayer player)
	{
		if (stickTimer.containsKey(player))
			scheduler.cancelTask(stickTimer.get(player));

		stickTimer.put(player, scheduler.startSyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				if (stickTimer.containsKey(player))
					stickTimer.remove(player);
			}
		}, 1));
	}

	private void listPlotsByPlayer(IPlayer checkPlayer, IPlayer triggerPlayer)
	{
		if (!this.worldGuardInterface.serverHasWorldGuard())
		{
			triggerPlayer.sendColouredMessage("Error: No WorldGuard installed.");
			return;
		}

		List<String> regions = plotFilter.apply(worldGuardInterface.getOwnedRegions(checkPlayer, checkPlayer.getWorld()));

		if (!regions.isEmpty())
			triggerPlayer.sendColouredMessage(StringUtils.join(
				manager.tag(triggerPlayer, regions),
				"\n"
			));
		else
			triggerPlayer.sendColouredMessage("%s does not own any plots.", checkPlayer.getPrettyName());
	}

	private void listPlotsByLocation(ILocation location, IPlayer player)
	{
		if (!this.worldGuardInterface.serverHasWorldGuard())
		{
			player.sendColouredMessage("Error: No WorldGuard installed.");
			return;
		}

		List<String> regions = plotFilter.apply(worldGuardInterface.getRegionsAtLocation(location));

		if (regions != null && !regions.isEmpty())
			for (String regionName : regions)
			{
				player.sendColouredMessage("&6Plot: &l%s", manager.tag(player, regionName));
				listClaimInfo(player, regionName);
				listTags(player, regionName);
				listPlotMembers(player, regionName);
			}
		else
			player.sendColouredMessage("No plots found at this location.");
	}

	private void listClaimInfo(IPlayer player, String regionName)
	{
		if (player.hasPermission("runsafe.creative.claim.log"))
		{
			String claim = logRepository.getClaim(regionName);
			if (claim == null)
				return;

			player.sendColouredMessage("&bClaimed: %s", claim);
		}
	}

	private void listTags(IPlayer player, String regionName)
	{
		if (player.hasPermission("runsafe.creative.tag.read"))
		{
			List<String> tags = tagRepository.getTags(regionName);
			if (!tags.isEmpty())
				player.sendColouredMessage("&7Tags: &o%s&r", StringUtils.join(tags, " "));
		}
	}

	private void listPlotMembers(IPlayer player, String regionName)
	{
		// Display owners that have been converted to UUIDs.
		Set<IPlayer> owners = worldGuardInterface.getOwnerPlayers(manager.getWorld(), regionName);
		for (IPlayer owner : owners)
			listPlotMember(player, "&2Owner&r", owner.getName(), true);

		// Display owners that haven't been converted to UUIDs yet.
		Set<String> ownerNames = worldGuardInterface.getOwners(manager.getWorld(), regionName);
		for (String owner : ownerNames)
			listPlotMember(player, "&2Owner&r", owner, true);

		// Display members that have been converted to UUIDs.
		Set<IPlayer> members = worldGuardInterface.getMemberPlayers(manager.getWorld(), regionName);
		for (IPlayer member : members)
			listPlotMember(player, "&3Member&r", member.getName(), false);

		// Display owners that haven't been converted to UUIDs yet.
		Set<String> memberNames = worldGuardInterface.getMembers(manager.getWorld(), regionName);
		for (String member : memberNames)
			listPlotMember(player, "&2Owner&r", member, false);
	}

	private void listPlotMember(IPlayer player, String label, String member, boolean showSeen)
	{
		IPlayer plotMember = server.getPlayer(member);
		if (plotMember != null)
		{
			player.sendColouredMessage("   %s: %s", label, plotMember.getPrettyName());

			if (showSeen && player.hasPermission("runsafe.creative.list.seen"))
			{
				String seen = plotMember.getLastSeen(player);
				player.sendColouredMessage("     %s&r", (seen == null ? "Player never seen" : seen));
			}
		}
		else
		{
			if (member != null && !member.isEmpty())
				player.sendColouredMessage("   %s: %s", label, member);
			else
				player.sendColouredMessage("   %s: %s", label, "Invalid Player");
		}
	}

	private final IRegionControl worldGuardInterface;
	private int listItem;
	private final PlotManager manager;
	private final PlotFilter plotFilter;
	private final PlotTagRepository tagRepository;
	private final PlotLogRepository logRepository;
	private final IServer server;
	private final IScheduler scheduler;
	private final ConcurrentHashMap<String, String> extensions = new ConcurrentHashMap<String, String>();
	private final ConcurrentHashMap<IPlayer, Integer> stickTimer = new ConcurrentHashMap<IPlayer, Integer>();
}