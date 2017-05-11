package no.runsafe.creativetoolbox.event;

import no.runsafe.creativetoolbox.PlotManager;
import no.runsafe.framework.api.IOutput;
import no.runsafe.framework.api.event.player.IPlayerCustomEvent;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class CustomEvents implements IPlayerCustomEvent
{
	public CustomEvents(IOutput output, PlotManager manager)
	{
		this.output = output;
		this.manager = manager;
	}

	@Override
	public void OnPlayerCustomEvent(RunsafeCustomEvent event)
	{
		if (event instanceof PlotApprovedEvent)
			output.broadcastColoured("&6The creative plot &l%s&r&6 has been approved.", ((PlotApprovedEvent) event).getApproval().getName());

		if (event instanceof PlotMembershipRevokedEvent)
		{
			String plot = (String) event.getData();
			manager.memberRemoved(plot, event.getPlayer());
		}
	}

	private final IOutput output;
	private PlotManager manager;
}
