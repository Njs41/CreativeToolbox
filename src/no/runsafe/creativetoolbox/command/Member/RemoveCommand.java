package no.runsafe.creativetoolbox.command.Member;

import no.runsafe.creativetoolbox.PlotFilter;
import no.runsafe.creativetoolbox.database.PlotMemberRepository;
import no.runsafe.creativetoolbox.event.PlotMembershipRevokedEvent;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.worldguardbridge.IRegionControl;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RemoveCommand extends PlayerAsyncCommand
{
	public RemoveCommand(IScheduler scheduler, PlotFilter filter, IRegionControl worldGuard, PlotMemberRepository memberRepository)
	{
		super("remove", "Remove a member from the plot you are standing in.", "runsafe.creative.member.remove", scheduler, new Player().require());
		plotFilter = filter;
		worldGuardInterface = worldGuard;
		this.memberRepository = memberRepository;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getValue("player");

		if (targetPlayer == null)
			return null;

		List<String> target = plotFilter.apply(worldGuardInterface.getRegionsAtLocation(executor.getLocation()));
		List<String> ownedRegions = worldGuardInterface.getOwnedRegions(executor, plotFilter.getWorld());
		if (target == null || target.isEmpty())
			return "No region defined at your location!";
		List<String> results = new ArrayList<String>();
		for (String region : target)
		{
			if (ownedRegions.contains(region) || executor.hasPermission("runsafe.creative.member.override"))
			{
				if (worldGuardInterface.removeMemberFromRegion(plotFilter.getWorld(), region, targetPlayer))
				{
					memberRepository.removeMember(region, targetPlayer);
					results.add(String.format("%s was successfully removed from the plot %s.", targetPlayer.getPrettyName(), region));
					new PlotMembershipRevokedEvent(targetPlayer, region).Fire();
				}
				else
					results.add(String.format("Could not remove %s from the plot %s.", targetPlayer.getPrettyName(), region));
			}
			else
				results.add(String.format("You do not appear to be an owner of %s.", region));
		}
		if (results.isEmpty())
			return null;
		return StringUtils.join(results, "\n");
	}

	private final IRegionControl worldGuardInterface;
	private final PlotFilter plotFilter;
	private final PlotMemberRepository memberRepository;
}
